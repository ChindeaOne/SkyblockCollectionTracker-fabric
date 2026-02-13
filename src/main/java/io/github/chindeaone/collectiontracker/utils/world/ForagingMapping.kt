package io.github.chindeaone.collectiontracker.utils.world

object ForagingMapping {

    @JvmStatic
    val foragingStats = listOf(
        "Foraging Fortune",
        "Fig Fortune",
        "Mangrove Fortune",
        "Sweep",
        "Foraging Wisdom"
    )

    val foragingBlockPerType = mapOf(
        "fig" to "minecraft:stripped_spruce_wood",
        "mangrove" to "minecraft:mangrove_wood",
    )
}