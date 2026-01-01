package io.github.chindeaone.collectiontracker.util

object CollectionColors {
    val colors: Map<String, Int> = HashMap<String, Int>().apply {
        // Farming
        put("cocoa beans", 0xFFD2691E.toInt()) // Chocolate Brown
        put("carrot", 0xFFFFA500.toInt()) // Orange
        put("cactus", 0xFF228B22.toInt()) // Forest Green
        put("raw chicken", 0xFFF5DEB3.toInt()) // Wheat
        put("sugar cane", 0xFF32CD32.toInt()) // Lime Green
        put("pumpkin", 0xFFFF8C00.toInt()) // Dark Orange
        put("wheat", 0xFFDAA520.toInt()) // Goldenrod
        put("seeds", 0xFF80DE09.toInt()) // Light green
        put("mushroom", 0xFFE33948.toInt()) // Pinkish Red
        put("raw rabbit", 0xFFF5F5DC.toInt()) // Beige
        put("nether wart", 0xFF8B0000.toInt()) // Dark Red
        put("mutton", 0xFFDA3E3E.toInt()) // Reddish
        put("melon", 0xFF69DF32.toInt()) // Lime green
        put("potato", 0xFFD2B48C.toInt()) // Tan
        put("leather", 0xFF8B4513.toInt()) // Brown
        put("porkchop", 0xFFFFC0CB.toInt()) // Pink
        put("feather", 0xFFFFFFFF.toInt()) // White

        // Mining
        put("lapis lazuli", 0xFF2F4FEC.toInt()) // Royal Blue
        put("redstone", 0xFFDC143C.toInt()) // Crimson
        put("umber", 0xFFD8A71A.toInt()) // Peru
        put("coal", 0xFF12110E.toInt()) // Dark Gray
        put("mycelium", 0xFF42354F.toInt()) // Dark Purple
        put("end stone", 0xFFEEE8AA.toInt()) // Pale Goldenrod
        put("quartz", 0xFFFAF0E6.toInt()) // Linen
        put("sand", 0xFFEDC9AF.toInt()) // Desert Sand
        put("iron", 0xFFD3D3D3.toInt()) // Light Gray
        put("amber", 0xFFE59D16.toInt()) // Amber
        put("topaz", 0xFFEAE227.toInt()) // Topaz
        put("sapphire", 0xFF5378D5.toInt()) // Sapphire Blue
        put("amethyst", 0xFF9966CC.toInt()) // Amethyst
        put("jasper", 0xFFDB27EA.toInt()) // Jasper Pink
        put("ruby", 0xFFEA2727.toInt()) // Ruby Red
        put("jade", 0xFF00A86B.toInt()) // Jade Green
        put("opal", 0xFFA8C3BC.toInt()) // Opal
        put("aquamarine", 0xFF0D44CE.toInt()) // Aquamarine
        put("citrine", 0xFF815A08.toInt()) // Citrine
        put("onyx", 0xFF353839.toInt()) // Onyx Black
        put("peridot", 0xFF28DE19.toInt()) // Peridot
        put("tungsten", 0xFFBEBEBE.toInt()) // Grayish
        put("obsidian", 0xFF4B0082.toInt()) // Indigo
        put("diamond", 0xFF00FFFF.toInt()) // Cyan
        put("cobblestone", 0xFF808080.toInt()) // Gray
        put("glowstone", 0xFFFFD700.toInt()) // Gold
        put("gold", 0xFFFFD700.toInt()) // Gold
        put("gravel", 0xFFA9A9A9.toInt()) // Dark Gray
        put("hard stone", 0xFF708090.toInt()) // Slate Gray
        put("mithril", 0xFF41DAA4.toInt()) // Light Blue
        put("emerald", 0xFF50C878.toInt()) // Emerald Green
        put("red sand", 0xFFE97451.toInt()) // Burnt Orange
        put("ice", 0xFFADD8E6.toInt()) // Light Blue
        put("glacite", 0xFFAFEEEE.toInt()) // Pale Turquoise
        put("sulphur", 0xFFDEEE12.toInt()) // Yellow Gold
        put("netherrack", 0xFF8B0000.toInt()) // Dark Red

        // Combat
        put("ender pearl", 0xFF462BF3.toInt()) // Blue Violet
        put("chili pepper", 0xFF61E429.toInt()) // Light Green
        put("slimeball", 0xFF7FFF00.toInt()) // Chartreuse
        put("magma cream", 0xFFFF4500.toInt()) // Orange Red
        put("ghast tear", 0xFFF8F8FF.toInt()) // Ghost White
        put("gunpowder", 0xFF696969.toInt()) // Nice
        put("rotten flesh", 0xFF8B0000.toInt()) // Dark Red
        put("spider eye", 0xFFA52A2A.toInt()) // Brown
        put("bone", 0xFFFFFFFF.toInt()) // White
        put("blaze rod", 0xFFE1B319.toInt()) // Gold
        put("string", 0xFFD3D3D3.toInt()) // Light Gray

        // Foraging
        put("oak", 0xFFD69243.toInt()) // Wood Brown

        // Fishing
        put("lily pad", 0xFF008000.toInt()) // Green
        put("prismarine shard", 0xFF4682B4.toInt()) // Steel Blue
        put("ink sac", 0xFF000000.toInt()) // Black
        put("raw fish", 0xFF82BAF0.toInt()) // Dodger Blue
        put("pufferfish", 0xFFFFD700.toInt()) // Gold
        put("clownfish", 0xFFFFA500.toInt()) // Orange
        put("raw salmon", 0xFFFA8072.toInt()) // Salmon
        put("magmafish", 0xFFFF8F00.toInt()) // Orange Red
        put("prismarine crystals", 0xFFAFEEEE.toInt()) // Pale Turquoise
        put("clay", 0xFFB0C4DE.toInt()) // Light Steel Blue
        put("sponge", 0xFFFFFF00.toInt()) // Yellow

        // Rift
        put("wilted berberis", 0xFF6F5A28.toInt()) // Dead Bush Brown
        put("living metal heart", 0xFF0785EF.toInt()) // Dodger Blue
        put("caducous stem", 0xFFEC5959.toInt()) // Light Red
        put("agaricus cap", 0xFFDA3030.toInt()) // Red
        put("hemovibe", 0xFFDC1414.toInt()) // Crimson
        put("half-eaten carrot", 0xFFFFA500.toInt()) // Orange
        put("timite", 0xFF4682B4.toInt()) // Steel Blue

        // Sacks
        put("cropie", 0xFFD36827.toInt()) // Wheat Brown
        put("squash", 0xFF288114.toInt()) // Green
        put("rabbit foot", 0xFFD8BD25.toInt() ) // Light Yellow
        put("rabbit hide", 0xFFE6D8B7.toInt()) // Beige
        put("titanium", 0xFFC8C7BF.toInt()) // Light Gray
        put("refined mineral", 0xFF25B0D8.toInt()) // Light Blue
        put("glossy gemstone", 0xFFAD25D8.toInt()) // Purple
        put("sludge juice", 0xFF44EB4E.toInt()) // Light Green
        put("yoggie", 0xFFD46322.toInt()) // Orange
        put("agarimoo tongue", 0xFFD43232.toInt()) // Red
    }
}