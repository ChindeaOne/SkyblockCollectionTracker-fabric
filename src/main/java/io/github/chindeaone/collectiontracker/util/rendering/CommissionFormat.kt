package io.github.chindeaone.collectiontracker.util.rendering

object CommissionFormat {
    enum class Area(val displayName: String) {
        DWARVEN_MINES("Dwarven Mines"),
        CRYSTAL_HOLLOWS("Crystal Hollows"),
        GLACITE_TUNNELS("Glacite Tunnels")
    }

    data class CommissionType(
        val name: String,
        val area: Area,
        val format: (String) -> String
    )

    // NEU Style
    private fun formatLine(line: String): String {
        var formatted = "§3$line§r"

        // Format completion: DONE or 100% -> §a (Green), others -> §c (Red)
        val completionRegex = Regex("(?i)DONE|\\d+(?:\\.\\d+)?%", RegexOption.IGNORE_CASE)
        formatted = formatted.replace(completionRegex) {
            val color = if (it.value.equals("DONE", ignoreCase = true)) "§a" else "§c"
            "$color${it.value}§r"
        }

        return formatted
    }

    val COMMISSIONS = listOf(
        // Dwarven Mines
        CommissionType("Mithril Miner", Area.DWARVEN_MINES) { formatLine(it) },
        CommissionType("Lava Springs Mithril", Area.DWARVEN_MINES) { formatLine(it) },
        CommissionType("Royal Mines Mithril", Area.DWARVEN_MINES) { formatLine(it) },
        CommissionType("Cliffside Veins Mithril", Area.DWARVEN_MINES) { formatLine(it) },
        CommissionType("Rampart's Quarry Mithril", Area.DWARVEN_MINES) { formatLine(it) },
        CommissionType("Upper Mines Mithril", Area.DWARVEN_MINES) { formatLine(it) },
        CommissionType("Titanium Miner", Area.DWARVEN_MINES) { formatLine(it) },
        CommissionType("Lava Springs Titanium", Area.DWARVEN_MINES) { formatLine(it) },
        CommissionType("Royal Mines Titanium", Area.DWARVEN_MINES) { formatLine(it) },
        CommissionType("Cliffside Veins Titanium", Area.DWARVEN_MINES) { formatLine(it) },
        CommissionType("Rampart's Quarry Titanium", Area.DWARVEN_MINES) { formatLine(it) },
        CommissionType("Upper Mines Titanium", Area.DWARVEN_MINES) { formatLine(it) },
        CommissionType("Goblin Slayer", Area.DWARVEN_MINES) { formatLine(it) },
        CommissionType("Glacite Walker Slayer", Area.DWARVEN_MINES) { formatLine(it) },
        CommissionType("Treasure Hoarder Puncher", Area.DWARVEN_MINES) { formatLine(it) },
        CommissionType("Goblin Raid Slayer", Area.DWARVEN_MINES) { formatLine(it) },
        CommissionType("Goblin Raid", Area.DWARVEN_MINES) { formatLine(it) },
        CommissionType("Raffle", Area.DWARVEN_MINES) { formatLine(it) },
        CommissionType("Golden Goblin Slayer", Area.DWARVEN_MINES) { formatLine(it) },
        CommissionType("Star Sentry Puncher", Area.DWARVEN_MINES) { formatLine(it) },
        CommissionType("Lucky Raffle", Area.DWARVEN_MINES) { formatLine(it) },
        CommissionType("2x Mithril Powder Collector", Area.DWARVEN_MINES) { formatLine(it) },

        // Crystal Hollows
        CommissionType("Hard Stone Miner", Area.CRYSTAL_HOLLOWS) { formatLine(it) },
        CommissionType("Jade Gemstone Collector", Area.CRYSTAL_HOLLOWS) { formatLine(it) },
        CommissionType("Amber Gemstone Collector", Area.CRYSTAL_HOLLOWS) { formatLine(it) },
        CommissionType("Topaz Gemstone Collector", Area.CRYSTAL_HOLLOWS) { formatLine(it) },
        CommissionType("Sapphire Gemstone Collector", Area.CRYSTAL_HOLLOWS) { formatLine(it) },
        CommissionType("Amethyst Gemstone Collector", Area.CRYSTAL_HOLLOWS) { formatLine(it) },
        CommissionType("Ruby Gemstone Collector", Area.CRYSTAL_HOLLOWS) { formatLine(it) },
        CommissionType("Chest Looter", Area.CRYSTAL_HOLLOWS) { formatLine(it) },
        CommissionType("Team Treasurite Member Slayer", Area.CRYSTAL_HOLLOWS) { formatLine(it) },
        CommissionType("Sludge Slayer", Area.CRYSTAL_HOLLOWS) { formatLine(it) },
        CommissionType("Yog Slayer", Area.CRYSTAL_HOLLOWS) { formatLine(it) },
        CommissionType("Automaton Slayer", Area.CRYSTAL_HOLLOWS) { formatLine(it) },
        CommissionType("Goblin Slayer", Area.CRYSTAL_HOLLOWS) { formatLine(it) },
        CommissionType("Thyst Slayer", Area.CRYSTAL_HOLLOWS) { formatLine(it) },
        CommissionType("Jade Crystal Hunter", Area.CRYSTAL_HOLLOWS) { formatLine(it) },
        CommissionType("Amber Crystal Hunter", Area.CRYSTAL_HOLLOWS) { formatLine(it) },
        CommissionType("Topaz Crystal Hunter", Area.CRYSTAL_HOLLOWS) { formatLine(it) },
        CommissionType("Sapphire Crystal Hunter", Area.CRYSTAL_HOLLOWS) { formatLine(it) },
        CommissionType("Amethyst Crystal Hunter", Area.CRYSTAL_HOLLOWS) { formatLine(it) },
        CommissionType("Boss Corleone Slayer", Area.CRYSTAL_HOLLOWS) { formatLine(it) },

        // Glacite Tunnels
        CommissionType("Mineshaft Explorer", Area.GLACITE_TUNNELS) { formatLine(it) },
        CommissionType("Corpse Looter", Area.GLACITE_TUNNELS) { formatLine(it) },
        CommissionType("Maniac Slayer", Area.GLACITE_TUNNELS) { formatLine(it) },
        CommissionType("Scrap", Area.GLACITE_TUNNELS) { formatLine(it) },
        CommissionType("Onyx Gemstone", Area.GLACITE_TUNNELS) { formatLine(it) },
        CommissionType("Aquamarine Gemstone", Area.GLACITE_TUNNELS) { formatLine(it) },
        CommissionType("Peridot Gemstone", Area.GLACITE_TUNNELS) { formatLine(it) },
        CommissionType("Citrine Gemstone", Area.GLACITE_TUNNELS) { formatLine(it) },
        CommissionType("Glacite", Area.GLACITE_TUNNELS) { formatLine(it) },
        CommissionType("Umber", Area.GLACITE_TUNNELS) { formatLine(it) },
        CommissionType("Tungsten", Area.GLACITE_TUNNELS) { formatLine(it) },
    )
}