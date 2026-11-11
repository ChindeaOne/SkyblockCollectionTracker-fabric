package io.github.chindeaone.collectiontracker.utils.world

import net.minecraft.world.level.block.Blocks

object MiningMapping {

    // Areas where you can mine
    val miningAreas = setOf(
        // actual mining ares
        "Dwarven Mines",
        "Crystal Hollows",
        "Mineshaft",
        "Gold Mine",
        "Deep Caverns",
        // other areas where you can mine
        "Hub",
        "The End",
        "Crimson Isle",
        "Spider's Den",
        "The Farming Islands",
        "Jerry's Workshop",
        "Torrhus Canyon", // i guess for anthills
    )

    // Mining specific islands
    val miningIslands = setOf(
        "Dwarven Mines",
        "Crystal Hollows",
        "Mineshaft",
        "Gold Mine",
        "Deep Caverns"
    )

    val miningStats = setOf(
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

    val miningBlocksPerArea: Map<String, Set<String>> = mapOf(
        "ores" to setOf(
            "Dwarven Mines",
            "Crystal Hollows",
            "Mineshaft",
            "Gold Mine",
            "Deep Caverns",
            "Crimson Isle", // for quartz and sulphur
            "Hub"
        ),
        "pure_ores" to setOf(
            "Dwarven Mines",
            "Crystal Hollows", // in MOD
        ),
        "blocks" to setOf(
            "Dwarven Mines", // stone, cobblestone
            "Crystal Hollows", // hard stone
            "Mineshaft", // hard stone
            "Gold Mine", // stone, cobblestone
            "Deep Caverns", // stone, cobblestone
            "The End", // for end stone and obsidian
            "Crimson Isle", // for red sand, mycelium, netherrack, glowstone
            "Spider's Den", // for gravel
            "Jerry's Workshop", // for ice
            "The Farming Islands", // for sand
            "Hub",
            "Torrhus Canyon" // for anthills
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

    val miningBlockTypePerBlock = mapOf(
        Blocks.EMERALD_ORE to "ores",
        Blocks.DIAMOND_ORE to "ores",
        Blocks.GOLD_ORE to "ores",
        Blocks.IRON_ORE to "ores",
        Blocks.COAL_ORE to "ores",
        Blocks.REDSTONE_ORE to "ores",
        Blocks.LAPIS_ORE to "ores",
        Blocks.NETHER_QUARTZ_ORE to "ores",
        Blocks.SPONGE to "ores", // sulphur

        Blocks.EMERALD_BLOCK to "pure_ores",
        Blocks.DIAMOND_BLOCK to "pure_ores",
        Blocks.GOLD_BLOCK to "pure_ores",
        Blocks.IRON_BLOCK to "pure_ores",
        Blocks.COAL_BLOCK to "pure_ores",
        Blocks.REDSTONE_BLOCK to "pure_ores",
        Blocks.LAPIS_BLOCK to "pure_ores",
        Blocks.QUARTZ_BLOCK to "pure_ores",

        Blocks.STONE to "blocks", // stone and hard stone
        Blocks.COBBLESTONE to "blocks",
        Blocks.END_STONE to "blocks",
        Blocks.OBSIDIAN to "blocks",
        Blocks.NETHERRACK to "blocks",
        Blocks.GLOWSTONE to "blocks",
        Blocks.RED_SAND to "blocks",
        Blocks.MYCELIUM to "blocks",
        Blocks.GRAVEL to "blocks",
        Blocks.SAND to "blocks",
        Blocks.ICE to "blocks",
        Blocks.POINTED_DRIPSTONE to "blocks", // anthills

        Blocks.PRISMARINE to "dwarven_metals", // mithril
        Blocks.PRISMARINE_BRICKS to "dwarven_metals", // mithril
        Blocks.DARK_PRISMARINE to "dwarven_metals", // mithril
        Blocks.WOOL.lightBlue to "dwarven_metals", // mithril
        Blocks.WOOL.gray to "dwarven_metals", // mithril
        Blocks.POLISHED_DIORITE to "dwarven_metals", // titanium
        Blocks.PACKED_ICE to "dwarven_metals", // glacite
        Blocks.DYED_TERRACOTTA.brown to "dwarven_metals", // umber
        Blocks.SMOOTH_RED_SANDSTONE to "dwarven_metals", // umber
        Blocks.TERRACOTTA to "dwarven_metals", // umber
        Blocks.INFESTED_COBBLESTONE to "dwarven_metals", // tungsten
        Blocks.CLAY to "dwarven_metals", // tungsten

        Blocks.STAINED_GLASS.orange to "gemstones", // amber
        Blocks.STAINED_GLASS_PANE.orange to "gemstones", // amber
        Blocks.STAINED_GLASS.lightBlue to "gemstones", // sapphire
        Blocks.STAINED_GLASS_PANE.lightBlue to "gemstones", // sapphire
        Blocks.STAINED_GLASS.purple to "gemstones", // amethyst
        Blocks.STAINED_GLASS_PANE.purple to "gemstones", // amethyst
        Blocks.STAINED_GLASS.red to "gemstones", // ruby
        Blocks.STAINED_GLASS_PANE.red to "gemstones", // ruby
        Blocks.STAINED_GLASS.lime to "gemstones", // jade
        Blocks.STAINED_GLASS_PANE.lime to "gemstones", // jade
        Blocks.STAINED_GLASS.yellow to "gemstones", // topaz
        Blocks.STAINED_GLASS_PANE.yellow to "gemstones", // topaz
        Blocks.STAINED_GLASS.white to "gemstones", // opal
        Blocks.STAINED_GLASS_PANE.white to "gemstones", // opal
        Blocks.STAINED_GLASS.magenta to "gemstones", // jasper
        Blocks.STAINED_GLASS_PANE.magenta to "gemstones", // jasper
        Blocks.STAINED_GLASS.blue to "gemstones", // aquamarine
        Blocks.STAINED_GLASS_PANE.blue to "gemstones", // aquamarine
        Blocks.STAINED_GLASS.green to "gemstones", // peridot
        Blocks.STAINED_GLASS_PANE.green to "gemstones", // peridot
        Blocks.STAINED_GLASS.black to "gemstones", // onyx
        Blocks.STAINED_GLASS_PANE.black to "gemstones", // onyx
        Blocks.STAINED_GLASS.brown to "gemstones", // citrine
        Blocks.STAINED_GLASS_PANE.brown to "gemstones" // citrine
    )

    // In mineshafts, they are changed from infested cobble to regular cobble
    val mineshaftTungstenBlocks = setOf(
        Blocks.COBBLESTONE,
        Blocks.COBBLESTONE_SLAB,
        Blocks.COBBLESTONE_STAIRS
    )

    val precisionMiningBlockTypes = setOf(
        "ores",
        "pure_ores",
        "dwarven_metals"
    )
}