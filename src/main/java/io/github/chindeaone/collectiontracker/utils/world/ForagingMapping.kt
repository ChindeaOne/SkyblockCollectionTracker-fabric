package io.github.chindeaone.collectiontracker.utils.world

import net.minecraft.world.level.block.Blocks

object ForagingMapping {

    val foragingAreas = setOf(
        "Hub",
        "The Park",
        "Moonglade Marsh",
        "Torrhus Canyon"
    )

    val foragingIslands = setOf(
        "The Park",
        "Moonglade Marsh",
        "Torrhus Canyon"
    )

    val foragingStats = setOf(
        "Foraging Fortune",
        "Fig Fortune",
        "Helix Fortune",
        "Mangrove Fortune",
        "Sweep",
        "Foraging Wisdom",
        "Timber"
    )

    val foragingBlockTypePerBlock = mapOf(
        Blocks.STRIPPED_SPRUCE_WOOD to "fig",
        Blocks.MANGROVE_WOOD to "mangrove",
        Blocks.STRIPPED_MANGROVE_WOOD to "helix",
        Blocks.STRIPPED_BIRCH_WOOD to "helix"
    )
}