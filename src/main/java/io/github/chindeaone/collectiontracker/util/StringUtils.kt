package io.github.chindeaone.collectiontracker.util

import kotlin.collections.contains

object StringUtils {

    private val formattingChars = "kmolnrKMOLNR".toSet()
    private val colorChars = "abcdefABCDEF0123456789".toSet()

    // Method taken from Skyhanni
    fun CharSequence.removeColor(keepFormatting: Boolean = false): String {
        // Glossary:
        // Formatting indicator: The '§' character indicating the beginning of a formatting sequence
        // Formatting code: The character following a formatting indicator which specifies what color or text style this sequence corresponds to
        // Formatting sequence: The combination of a formatting indicator and code that changes the color or format of a string

        // Flag for whether there is a text style (non-color and non-reset formatting code) currently being applied
        var isFormatted = false

        // Find the first formatting indicator
        var nextFormattingSequence = indexOf('§')

        // If this string does not contain any formatting indicators, just return this string directly
        if (nextFormattingSequence < 0) return this.toString()

        // Let's create a new string, and pre-allocate enough space to store this entire string
        val cleanedString = StringBuilder(this.length)

        // Read index stores the position in `this` which we have written up until now
        // a/k/a where we need to start reading from
        var readIndex = 0

        // As long as there still is a formatting indicator left in our string
        while (nextFormattingSequence >= 0) {

            // Write everything from the read index up to the next formatting indicator into our clean string
            cleanedString.append(this, readIndex, nextFormattingSequence)

            // Get the formatting code (note: this may not be a valid formatting code)
            val formattingCode = this.getOrNull(nextFormattingSequence + 1)

            // If the next formatting sequence's code indicates a non-color format and we should keep those
            if (keepFormatting && formattingCode in formattingChars) {
                // Update formatted flag based on whether this is a reset or a style format code
                isFormatted = formattingCode?.lowercaseChar() != 'r'

                // Set the readIndex to the formatting indicator, so that the next loop will start writing from that paragraph symbol
                readIndex = nextFormattingSequence
                // Find the next § symbol after the formatting sequence
                nextFormattingSequence = indexOf('§', startIndex = readIndex + 1)
            } else {
                // If this formatting sequence should be skipped (either a color code, or !keepFormatting or an incomplete formatting sequence without a code)

                // If being formatted and color code encountered, reset the current formatting code
                if (isFormatted && formattingCode in colorChars) {
                    cleanedString.append("§r")
                    isFormatted = false
                }

                // Set the readIndex to after this formatting sequence, so that the next loop will skip over it before writing the string
                readIndex = nextFormattingSequence + 2
                // Find the next § symbol after the formatting sequence
                nextFormattingSequence = indexOf('§', startIndex = readIndex)

                // If the next read would be out of bound, reset the readIndex to the very end of the string, resulting in a "" string to be appended
                readIndex = readIndex.coerceAtMost(this.length)
            }
        }
        // Finally, after the last formatting sequence was processed, copy over the last sequence of the string
        cleanedString.append(this, readIndex, this.length)

        // And turn the string builder into a string
        return cleanedString.toString()
    }
}