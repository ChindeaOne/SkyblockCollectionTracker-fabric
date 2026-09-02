package io.github.chindeaone.collectiontracker.collections


object GemstonesManager {
    var gemstones: MutableList<String> = mutableListOf()

    fun checkIfGemstone(collectionName: String): Boolean {
        for (gemstone in gemstones) {
            if (collectionName.equals(gemstone, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}