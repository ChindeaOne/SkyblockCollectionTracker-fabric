package io.github.chindeaone.collectiontracker.util.world

object MiningMapping {

    // Areas where you can mine
    val miningAreas = listOf(
        // actual mining ares
        "Dwarven Mines",
        "Crystal Hollows",
        "Mineshaft",
        "Gold Mines",
        "Deep Caverns",
        // other areas where you can mine
        "The End",
        "Crimson Isle",
        "Spider's Den",
        "The Farming Islands",
        "Jerry's Workshop"
    )

    // Mining specific islands
    val miningIslands = listOf(
        "Dwarven Mines",
        "Crystal Hollows",
        "Mineshaft",
        "Gold Mines",
        "Deep Caverns"
    )

    val miningStats = listOf(
        "Mining Speed",
        "Mining Fortune",
        "Dwarven Metal Fortune",
        "Gemstone Fortune",
        "Ore Fortune",
        "Block Fortune",
        "Mining Wisdom",
        "Mining Spread",
        "Gemstone Spread",
        "Pristine",
        // Location specific stats
        "Cold Resistance",
        "Heat Resistance",
        // why not
        "Breaking Power"
    )

    @JvmStatic
    val miningBlocksPerArea: Map<String, Set<String>> = mapOf(
        "ores" to setOf(
            "Dwarven Mines",
            "Crystal Hollows",
            "Mineshaft",
            "Gold Mines",
            "Deep Caverns",
            "Crimson Isle", // for quartz and sulphur
        ),
        "pure_ores" to setOf(
            "Dwarven Mines",
            "Crystal Hollows", // in MOD
        ),
        "blocks" to setOf(
            "Dwarven Mines", // stone, cobblestone
            "Crystal Hollows", // hard stone
            "Mineshaft", // hard stone
            "Gold Mines", // stone, cobblestone
            "Deep Caverns", // stone, cobblestone
            "The End", // for end stone and obsidian
            "Crimson Isle", // for red sand, mycelium, netherrack, glowstone
            "Spider's Den", // for gravel
            "Jerry's Workshop", // for ice
            "The Farming Islands" // for sand
        ),
        "dwarven_metals" to setOf(
            "Dwarven Mines",
            "Crystal Hollows",
            "Mineshaft"
        ),
        "gemstones" to setOf(
            "Dwarven Mines",
            "Crystal Hollows",
            "Mineshaft",
            "Crimson Isle" // for opal
        )
    )

    val miningBlockPerType = mapOf(
        "ores" to setOf(
            "minecraft:emerald_ore",
            "minecraft:diamond_ore",
            "minecraft:gold_ore",
            "minecraft:iron_ore",
            "minecraft:coal_ore",
            "minecraft:redstone_ore",
            "minecraft:lapis_ore",
            "minecraft:quartz_ore",
            "minecraft:sponge" // sulphur
        ),
        "pure_ores" to setOf(
            "minecraft:emerald_block",
            "minecraft:diamond_block",
            "minecraft:gold_block",
            "minecraft:iron_block",
            "minecraft:coal_block",
            "minecraft:redstone_block",
            "minecraft:lapis_block",
            "minecraft:quartz_block"
        ),
        "blocks" to setOf(
            "minecraft:stone", // stone and hard stone
            "minecraft:cobblestone",
            "minecraft:end_stone",
            "minecraft:obsidian",
            "minecraft:netherrack",
            "minecraft:glowstone",
            "minecraft:red_sand",
            "minecraft:mycelium",
            "minecraft:gravel",
            "minecraft:sand",
            "minecraft:ice"
        ),
        "dwarven_metals" to setOf(
            "minecraft:prismarine", // mithril
            "minecraft:prismarine_bricks", // mithril
            "minecraft:dark_prismarine", // mithril
            "minecraft:light_blue_wool", // mithril
            "minecraft:gray_wool", // mithril
            "minecraft:polished_diorite", // titanium
            "minecraft:packed_ice", // glacite
            "minecraft:brown_terracotta", // umber
            "minecraft:smooth_red_sandstone", // umber
            "minecraft:terracotta", // umber
            "minecraft:infested_cobblestone", // tungsten
            "minecraft:clay" // tungsten
        ),
        "gemstones" to setOf(
            "minecraft:orange_stained_glass", // amber
            "minecraft:orange_stained_glass_pane", // amber
            "minecraft:light_blue_stained_glass", // sapphire
            "minecraft:light_blue_stained_glass_pane", // sapphire
            "minecraft:purple_stained_glass", // amethyst
            "minecraft:purple_stained_glass_pane", // amethyst
            "minecraft:red_stained_glass", // ruby
            "minecraft:red_stained_glass_pane", // ruby
            "minecraft:lime_stained_glass", // jade
            "minecraft:lime_stained_glass_pane", // jade
            "minecraft:yellow_stained_glass", // topaz
            "minecraft:yellow_stained_glass_pane", // topaz
            "minecraft:white_stained_glass", // opal
            "minecraft:white_stained_glass_pane", // opal
            "minecraft:magenta_stained_glass", // jasper
            "minecraft:magenta_stained_glass_pane", // jasper
            "minecraft:blue_stained_glass", // aquamarine
            "minecraft:blue_stained_glass_pane", // aquamarine
            "minecraft:green_stained_glass", // peridot
            "minecraft:green_stained_glass_pane", // peridot
            "minecraft:black_stained_glass", // onyx
            "minecraft:black_stained_glass_pane", // onyx
            "minecraft:brown_stained_glass", // citrine
            "minecraft:brown_stained_glass_pane" // citrine
        )
    )
}