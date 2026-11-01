package io.github.chindeaone.collectiontracker.collections


object GemstonesManager {

    enum class GemstoneTier(val displayName: String, val recipeAmount: Int) {
        ROUGH("Rough", 1),
        FLAWED("Flawed", 80),
        FINE("Fine", 6_400),
        FLAWLESS("Flawless", 512_000),
        PERFECT("Perfect", 2_560_000);

        override fun toString(): String = displayName
    }

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