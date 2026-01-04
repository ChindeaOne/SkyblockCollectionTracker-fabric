package io.github.chindeaone.collectiontracker.util.rendering

object CommissionFormat {
    enum class Area(val displayName: String) {
        DWARVEN_MINES("Dwarven Mines"),
        CRYSTAL_HOLLOWS("Crystal Hollows"),
        GLACITE_TUNNELS("Glacite Tunnels")
    }

    data class CommissionType(
        val name: String,
        val description: String,
        val area: Area,
        val format: (String) -> String
    )

    private fun formatLine(line: String, keywords: List<String>, numbers: Boolean = true): String {
        var formatted = line

        // Format completion: DONE or 100% -> §a (Green), others -> §c (Red)
        val completionRegex = Regex("(?i)DONE|\\d+%", RegexOption.IGNORE_CASE)
        formatted = formatted.replace(completionRegex) {
            val color = if (it.value.equals("DONE", ignoreCase = true) || it.value == "100%") "§a" else "§c"
            "$color${it.value}§r"
        }

        // Format numbers: §a (Green)
        if (numbers) {
            // Match numbers not followed by % and not preceded by § or another digit
            formatted = formatted.replace(Regex("(?<![§\\d])\\d+(?![\\d%])")) { "§a${it.value}§r" }
        }
        // Format keywords: §b (Aqua)
        for (keyword in keywords) {
            formatted = formatted.replace(Regex("(?i)$keyword")) { "§b${it.value}§r" }
        }
        return formatted
    }

    val COMMISSIONS = listOf(
        // Dwarven Mines
        CommissionType("Mithril Miner", "Mine 350 Mithril Ore", Area.DWARVEN_MINES) { formatLine(it, listOf("Mithril Ore")) },
        CommissionType("Lava Springs Mithril", "Mine 250 Mithril Ore in Lava Springs", Area.DWARVEN_MINES) { formatLine(it, listOf("Mithril Ore", "Lava Springs")) },
        CommissionType("Royal Mines Mithril", "Mine 250 Mithril Ore in Royal Mines", Area.DWARVEN_MINES) { formatLine(it, listOf("Mithril Ore", "Royal Mines")) },
        CommissionType("Cliffside Veins Mithril", "Mine 250 Mithril Ore in Cliffside Veins", Area.DWARVEN_MINES) { formatLine(it, listOf("Mithril Ore", "Cliffside Veins")) },
        CommissionType("Rampart's Quarry Mithril", "Mine 250 Mithril Ore in Rampart's Quarry", Area.DWARVEN_MINES) { formatLine(it, listOf("Mithril Ore", "Rampart's Quarry")) },
        CommissionType("Upper Mines Mithril", "Mine 250 Mithril Ore in Upper Mines", Area.DWARVEN_MINES) { formatLine(it, listOf("Mithril Ore", "Upper Mines")) },
        CommissionType("Titanium Miner", "Mine 15 Titanium Ore", Area.DWARVEN_MINES) { formatLine(it, listOf("Titanium Ore")) },
        CommissionType("Lava Springs Titanium", "Mine 10 Titanium Ore in Lava Springs", Area.DWARVEN_MINES) { formatLine(it, listOf("Titanium Ore", "Lava Springs")) },
        CommissionType("Royal Mines Titanium", "Mine 10 Titanium Ore in Royal Mines", Area.DWARVEN_MINES) { formatLine(it, listOf("Titanium Ore", "Royal Mines")) },
        CommissionType("Cliffside Veins Titanium", "Mine 10 Titanium Ore in Cliffside Veins", Area.DWARVEN_MINES) { formatLine(it, listOf("Titanium Ore", "Cliffside Veins")) },
        CommissionType("Rampart's Quarry Titanium", "Mine 10 Titanium Ore in Rampart's Quarry", Area.DWARVEN_MINES) { formatLine(it, listOf("Titanium Ore", "Rampart's Quarry")) },
        CommissionType("Upper Mines Titanium", "Mine 10 Titanium Ore in Upper Mines", Area.DWARVEN_MINES) { formatLine(it, listOf("Titanium Ore", "Upper Mines")) },
        CommissionType("Goblin Slayer", "Slay 100 Goblins", Area.DWARVEN_MINES) { formatLine(it, listOf("Goblins")) },
        CommissionType("Glacite Walker Slayer", "Slay 50 Glacite Walkers", Area.DWARVEN_MINES) { formatLine(it, listOf("Glacite Walkers")) },
        CommissionType("Treasure Hoarder Puncher", "Damage Treasure Hoarders 10 times", Area.DWARVEN_MINES) { formatLine(it, listOf("Treasure Hoarders")) },
        CommissionType("Goblin Raid Slayer", "Slay 20 Goblins during the Goblin Raid Event", Area.DWARVEN_MINES) { formatLine(it, listOf("Goblins", "Goblin Raid Event")) },
        CommissionType("Goblin Raid", "Participate in the Goblin Raid Event", Area.DWARVEN_MINES) { formatLine(it, listOf("Goblin Raid Event")) },
        CommissionType("Raffle", "Participate in the Raffle Event", Area.DWARVEN_MINES) { formatLine(it, listOf("Raffle Event")) },
        CommissionType("Golden Goblin Slayer", "Slay 1 Golden Goblin", Area.DWARVEN_MINES) { formatLine(it, listOf("Golden Goblin")) },
        CommissionType("Star Sentry Puncher", "Damage Star Sentrys 10 times", Area.DWARVEN_MINES) { formatLine(it, listOf("Star Sentrys")) },
        CommissionType("Lucky Raffle", "Deposit 20 Tickets during the Raffle Event", Area.DWARVEN_MINES) { formatLine(it, listOf("Tickets", "Raffle Event")) },
        CommissionType("2x Mithril Powder Collector", "Collect 500 Mithril Powder during the 2x Powder Event", Area.DWARVEN_MINES) { formatLine(it, listOf("Mithril Powder", "2x Powder Event")) },

        // Crystal Hollows
        CommissionType("Hard Stone Miner", "Mine 1,000 Hard Stone", Area.CRYSTAL_HOLLOWS) { formatLine(it, listOf("Hard Stone")) },
        CommissionType("Jade Gemstone Collector", "Collect 1,000 Jade Gemstones", Area.CRYSTAL_HOLLOWS) { formatLine(it, listOf("Jade Gemstones")) },
        CommissionType("Amber Gemstone Collector", "Collect 1,000 Amber Gemstones", Area.CRYSTAL_HOLLOWS) { formatLine(it, listOf("Amber Gemstones")) },
        CommissionType("Topaz Gemstone Collector", "Collect 1,000 Topaz Gemstones", Area.CRYSTAL_HOLLOWS) { formatLine(it, listOf("Topaz Gemstones")) },
        CommissionType("Sapphire Gemstone Collector", "Collect 1,000 Sapphire Gemstones", Area.CRYSTAL_HOLLOWS) { formatLine(it, listOf("Sapphire Gemstones")) },
        CommissionType("Amethyst Gemstone Collector", "Collect 1,000 Amethyst Gemstones", Area.CRYSTAL_HOLLOWS) { formatLine(it, listOf("Amethyst Gemstones")) },
        CommissionType("Ruby Gemstone Collector", "Collect 1,000 Ruby Gemstones", Area.CRYSTAL_HOLLOWS) { formatLine(it, listOf("Ruby Gemstones")) },
        CommissionType("Chest Looter", "Open 3 chests", Area.CRYSTAL_HOLLOWS) { formatLine(it, listOf("chests")) },
        CommissionType("Team Treasurite Member Slayer", "Slay 13 Team Treasurite Members", Area.CRYSTAL_HOLLOWS) { formatLine(it, listOf("Team Treasurite Members")) },
        CommissionType("Sludge Slayer", "Slay 25 Sludges", Area.CRYSTAL_HOLLOWS) { formatLine(it, listOf("Sludges")) },
        CommissionType("Yog Slayer", "Slay 13 Yogs", Area.CRYSTAL_HOLLOWS) { formatLine(it, listOf("Yogs")) },
        CommissionType("Automaton Slayer", "Slay 13 Automatons", Area.CRYSTAL_HOLLOWS) { formatLine(it, listOf("Automatons")) },
        CommissionType("Goblin Slayer", "Slay 13 Goblins", Area.CRYSTAL_HOLLOWS) { formatLine(it, listOf("Goblins")) },
        CommissionType("Thyst Slayer", "Slay 5 Thysts", Area.CRYSTAL_HOLLOWS) { formatLine(it, listOf("Thysts")) },
        CommissionType("Jade Crystal Hunter", "Find a Jade Crystal", Area.CRYSTAL_HOLLOWS) { formatLine(it, listOf("Jade Crystal")) },
        CommissionType("Amber Crystal Hunter", "Find a Amber Crystal", Area.CRYSTAL_HOLLOWS) { formatLine(it, listOf("Amber Crystal")) },
        CommissionType("Topaz Crystal Hunter", "Find a Topaz Crystal", Area.CRYSTAL_HOLLOWS) { formatLine(it, listOf("Topaz Crystal")) },
        CommissionType("Sapphire Crystal Hunter", "Find a Sapphire Crystal", Area.CRYSTAL_HOLLOWS) { formatLine(it, listOf("Sapphire Crystal")) },
        CommissionType("Amethyst Crystal Hunter", "Find a Amethyst Crystal", Area.CRYSTAL_HOLLOWS) { formatLine(it, listOf("Amethyst Crystal")) },
        CommissionType("Boss Corleone Slayer", "Slay 1 Boss Corleone", Area.CRYSTAL_HOLLOWS) { formatLine(it, listOf("Boss Corleone")) },

        // Glacite Tunnels
        CommissionType("Mineshaft Explorer", "Enter 1 Mineshaft", Area.GLACITE_TUNNELS) { formatLine(it, listOf("Mineshaft")) },
        CommissionType("Corpse Looter", "Loot 2 Frozen Corpses", Area.GLACITE_TUNNELS) { formatLine(it, listOf("Frozen Corpses")) },
        CommissionType("Maniac Slayer", "Kill a total of 10 Glacite Caver, Glacite Bowman, Glacite Mage, Glacite Mutt", Area.GLACITE_TUNNELS) { formatLine(it, listOf("Glacite Caver", "Glacite Bowman", "Glacite Mage", "Glacite Mutt")) },
        CommissionType("Scrap", "Obtain 1 Suspicious Scrap", Area.GLACITE_TUNNELS) { formatLine(it, listOf("Suspicious Scrap")) },
        CommissionType("Onyx Gemstone", "Collect 1,500 Onyx Gemstone", Area.GLACITE_TUNNELS) { formatLine(it, listOf("Onyx Gemstone")) },
        CommissionType("Aquamarine Gemstone", "Collect 1,500 Aquamarine Gemstone", Area.GLACITE_TUNNELS) { formatLine(it, listOf("Aquamarine Gemstone")) },
        CommissionType("Peridot Gemstone", "Collect 1,500 Peridot Gemstone", Area.GLACITE_TUNNELS) { formatLine(it, listOf("Peridot Gemstone")) },
        CommissionType("Citrine Gemstone", "Collect 1,500 Citrine Gemstone", Area.GLACITE_TUNNELS) { formatLine(it, listOf("Citrine Gemstone")) },
        CommissionType("Glacite", "Collect a total of 1,500 Glacite", Area.GLACITE_TUNNELS) { formatLine(it, listOf("Glacite", "Enchanted Glacite")) },
        CommissionType("Umber", "Collect a total of 1,500 Umber or Enchanted Umber", Area.GLACITE_TUNNELS) { formatLine(it, listOf("Umber", "Enchanted Umber")) },
        CommissionType("Tungsten", "Collect a total of 1,500 Tungsten or Enchanted Tungsten", Area.GLACITE_TUNNELS) { formatLine(it, listOf("Tungsten", "Enchanted Tungsten")) },
    )
}