package io.github.chindeaone.collectiontracker.utils.world

object ForagingMapping {

    val foragingAreas = listOf(
        "Hub",
        "The Park",
        "Moonglade Marsh",
        "Torrhus Canyon"
    )

    val foragingIslands = listOf(
        "The Park",
        "Moonglade Marsh",
        "Torrhus Canyon"
    )

    val foragingStats = listOf(
        "Foraging Fortune",
        "Fig Fortune",
        "Helix Fortune",
        "Mangrove Fortune",
        "Sweep",
        "Foraging Wisdom",
        "Timber"
    )

    val foragingBlockPerType = mapOf(
        "fig" to setOf("minecraft:stripped_spruce_wood"),
        "mangrove" to setOf("minecraft:mangrove_wood"),
        "helix" to setOf(
            "minecraft:stripped_mangrove_wood",
            "minecraft:stripped_birch_wood"
        )
    )
}