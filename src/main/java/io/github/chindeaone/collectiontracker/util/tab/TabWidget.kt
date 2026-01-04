package io.github.chindeaone.collectiontracker.util.tab

enum class TabWidget(private val headerRegex: String) {
    // Thank you Skyhanni for all the widgets' regexes
    PLAYER_LIST(
        // language=RegExp
        "(?:§.)*Players (?:§.)*\\(\\d+\\)",
    ),
    INFO(
        // language=RegExp
        "(?:§.)*Info",
    ),
    AREA(
        // language=RegExp
        "(?:§.)*(Area|Dungeon): (?:§.)*(?<island>.*)",
    ),
    SERVER(
        // language=RegExp
        "Server: (?:§.)*(?<serverid>.*)",
    ),
    GEMS(
        // language=RegExp
        "Gems: (?:§.)*(?<gems>.*)",
    ),
    FAIRY_SOULS(
        // language=RegExp
        "Fairy Souls: (?:§.)*(?<got>\\d+)(?:§.)*\\/(?:§.)*(?<max>\\d+)",
    ),
    PROFILE(
        // language=RegExp
        "(?:§.)+Profile: §r§.(?<profile>[\\w\\s]+[^ §]).*",
    ),
    SB_LEVEL(
        // language=RegExp
        "SB Level(?:§.)*: (?:§.)*\\[(?:§.)*(?<level>\\d+)(?:§.)*\\] (?:§.)*(?<xp>\\d+).*",
    ),
    BANK(
        // language=RegExp
        "Bank: (?:§.)*(?<amount>[^§]+)(?:(?:§.)* \\/ (?:§.)*(?<personal>.*))?",
    ),
    INTEREST(
        // language=RegExp
        "Interest: (?:§.)*(?<time>[^§]+)(?:§.)*( \\((?<amount>[^)]+)\\))?",
    ),
    SOULFLOW(
        // language=RegExp
        "Soulflow: (?:§.)*(?<amount>.*)",
    ),
    PET(
        // language=RegExp
        "(?:§.)*Pet:",
    ),
    PET_TRAINING(
        // language=RegExp
        "(?:§.)*Pet Training:",
    ),
    PET_SITTER(
        // language=RegExp
        "Kat: .*",
    ),
    FIRE_SALE(
        // language=RegExp
        "(?:§.)*Fire Sales: .*",
    ),
    ELECTION(
        // language=RegExp
        "(?:§.)*Election: (?:§.)*(?<time>.*)",
    ),
    EVENT(
        // language=RegExp
        "(?:§.)*Event: (?<color>(?:§.)*)(?<event>.*)",
    ),
    SKILLS(
        // language=RegExp
        "(?:§.)*Skills: ?(?:§.)*(?<avg>[\\d.]*)",
    ),
    STATS(
        // language=RegExp
        "(?:§.)*Stats:",
    ),
    GUESTS(
        // language=RegExp
        "(?:§.)*Guests (?:§.)*.*",
    ),
    COOP(
        // language=RegExp
        "(?:§.)*Coop (?:§.)*.*",
    ),
    ISLAND(
        // language=RegExp
        "(?:§.)*Island",
    ),
    MINION(
        // language=RegExp
        "(?:§.)*Minions: (?:§.)*(?<used>\\d+)(?:§.)*/(?:§.)*(?<max>\\d+)",
    ),
    JERRY_ISLAND_CLOSING(
        // language=RegExp
        "Island closes in: (?:§.)*(?<time>.*)",
    ),
    NORTH_STARS(
        // language=RegExp
        "North Stars: (?:§.)*(?<amount>\\d+)",
    ),
    COLLECTION(
        // language=RegExp
        "(?:§.)*Collection:",
    ),
    JACOB_CONTEST(
        // language=RegExp
        "(?:§.)*Jacob's Contest:.*",
    ),
    SLAYER(
        // language=RegExp
        "(?:§.)*Slayer:",
    ),
    DAILY_QUESTS(
        // language=RegExp
        "(?:§.)*Daily Quests:",
    ),
    ACTIVE_EFFECTS(
        // language=RegExp
        "(?:§.)*Active Effects: (?:§.)*\\((?<amount>\\d+)\\)",
    ),
    BESTIARY(
        // language=RegExp
        "(?:§.)*Bestiary:",
    ),
    ESSENCE(
        // language=RegExp
        "(?:§.)*Essence:.*",
    ),
    FORGE(
        // language=RegExp
        "(?:§.)*Forges:",
    ),
    TIMERS(
        // language=RegExp
        "(?:§.)*Timers:",
    ),
    DUNGEON_STATS(
        // language=RegExp
        "Opened Rooms: (?:§.)*(?<opend>\\d+)",
    ),
    PARTY(
        // language=RegExp
        "(?:§.)*Party:.*",
    ),
    TRAPPER(
        // language=RegExp
        "(?:§.)*Trapper:",
    ),
    COMMISSIONS(
        // language=RegExp
        "(?:§.)*Commissions:",
    ),
    POWDER(
        // language=RegExp
        "(?:§.)*Powders:",
    ),
    CRYSTAL(
        // language=RegExp
        "(?:§.)*Crystals:",
    ),
    UNCLAIMED_CHESTS(
        // language=RegExp
        "Unclaimed chests: (?:§.)*(?<amount>\\d+)",
    ),
    RAIN(
        // language=RegExp
        "(?<type>Thunder|Rain): (?:§.)*(?<time>.*)",
    ),
    BROODMOTHER(
        // language=RegExp
        "Broodmother: (?:§.)*(?<stage>.*)",
    ),
    EYES_PLACED(
        // language=RegExp
        "Eyes placed: (?:§.)*(?<amount>\\d).*|(?:§.)*Dragon spawned!|(?:§.)*Egg respawning!",
    ),
    PROTECTOR(
        // language=RegExp
        "Protector: (?:§.)*(?<time>.*)",
    ),
    DRAGON(
        // language=RegExp
        "(?:§.)*Dragon: (?:§.)*\\((?<type>[^)]*)\\)",
    ),
    VOLCANO(
        // language=RegExp
        "Volcano: (?:§.)*(?<time>.*)",
    ),
    REPUTATION(
        // language=RegExp
        "(?:§.)*(?<faction>Barbarian|Mage) Reputation:",
    ),
    FACTION_QUESTS(
        // language=RegExp
        "(?:§.)*Faction Quests:",
    ),
    TROPHY_FISH(
        // language=RegExp
        "(?:§.)*Trophy Fish:",
    ),
    RIFT_INFO(
        // language=RegExp
        "(?:§.)*Good to know:",
    ),
    RIFT_SHEN(
        // language=RegExp
        "(?:§.)*Shen: (?:§.)*\\((?<time>[^)])\\)",
    ),
    RIFT_BARRY(
        // language=RegExp
        "(?:§.)*Advertisement:",
    ),
    COMPOSTER(
        // language=RegExp
        "(?:§.)*Composter:",
    ),
    GARDEN_LEVEL(
        // language=RegExp
        "Garden Level: (?:§.)*(?<level>.*)",
    ),
    COPPER(
        // language=RegExp
        "Copper: (?:§.)*(?<amount>\\d+)",
    ),
    PESTS(
        // language=RegExp
        "(?:§.)*Pests:",
    ),
    PEST_TRAPS(
        // language=RegExp
        "(?:§.)*Pest Traps: (?:§.)*(?<count>\\d+)\\/(?<max>\\d+)",
    ),
    FULL_TRAPS(
        /**
         * REGEX-TEST: §r§fFull Traps: §r§a#1§r§7, §r§a#2§r§7, §r§a#3
         * REGEX-TEST: §r§fFull Traps: §r§a#2§r§7, §r§a#3
         * REGEX-TEST: §r§fFull Traps: §r§a#3
         * REGEX-TEST: §r§fFull Traps: §r§7None
         */
        // language=RegExp
        "(?:§.)*Full Traps: (?:§.)*(?:None|§r§a(?<traps>#\\d(?:§r§7, §r§a#\\d(?:§r§7, §r§a#\\d)?)?))",
    ),
    NO_BAIT(
        /**
         * REGEX-TEST: §r§fNo Bait: §r§c#1§r§7, §r§c#2§r§7, §r§c#3
         * REGEX-TEST: §r§fNo Bait: §r§c#2§r§7, §r§c#3
         * REGEX-TEST: §r§fNo Bait: §r§c#3
         * REGEX-TEST: §r§fNo Bait: §r§7None
         */
        // language=RegExp
        "(?:§.)*No Bait: (?:§.)*(?:None|§r§c(?<traps>#\\d(?:§r§7, §r§c#\\d(?:§r§7, §r§c#\\d)?)?))"
    ),
    VISITORS(
        // language=RegExp
        "(?:§.)*Visitors: (?:§.)*\\((?<count>\\d+)\\)",
    ),
    CROP_MILESTONE(
        // language=RegExp
        "(?:§.)*Crop Milestones:",
    ),
    PRIVATE_ISLAND_CRYSTALS(
        // language=RegExp
        "Crystals: (?:§.)*(?<count>\\d+)",
    ),
    OLD_PET_SITTER(
        // language=RegExp
        "Pet Sitter:.*",
    ),
    DUNGEON_HUB_PROGRESS(
        // language=RegExp
        "(?:§.)*Dungeons:",
    ),
    DUNGEON_PUZZLE(
        // language=RegExp
        "(?:§.)*Puzzles: (?:§.)*\\((?<amount>\\d+)\\)",
    ),
    DUNGEON_PARTY(
        // language=RegExp
        "(?:§.)*Party (?:§.)*\\(\\d+\\)",
    ),
    DUNGEON_PLAYER_STATS(
        // language=RegExp
        "(?:§.)*Player Stats",
    ),
    DUNGEON_SKILLS_AND_STATS(
        // language=RegExp
        "(?:§.)*Skills: (?:§.)*\\w+ \\d+: (?:§.)*[\\d.]+%",
    ),

    /** This line holds no information, only here because every widget must be present */
    DUNGEON_ACCOUNT_INFO_LINE(
        // language=RegExp
        "(?:§.)*Account Info",
    ),
    DUNGEON_STATS_LINE(
        // language=RegExp
        "(?:§.)*Dungeon Stats",
    ),
    FROZEN_CORPSES(
        // language=RegExp
        "§b§lFrozen Corpses:",
    ),
    SCRAP(
        // language=RegExp
        "Scrap: (?:§.)*(?<amount>\\d)(?:§.)*/(?:§.)*\\d",
    ),
    EVENT_TRACKERS(
        // language=RegExp
        "§e§lEvent Trackers:",
    ),
    AGATHA_CONTEST(
        // language=RegExp
        "(?:§.)*Agatha's Contest:.*",
    ),
    MOONGLADE_BEACON(
        // language=RegExp
        "(?:§.)*Moonglade Beacon: §r§b(?<stacks>\\d+) Stacks?",
    ),
    SALTS(
        // language=RegExp
        "(?:§.)*Salts:",
    ),
    FOREST_WHISPERS(
        // language=RegExp
        "(?:§.)*Forest Whispers: (?:§.)*(?<amount>.*)",
    ),
    SHARD_TRAPS(
        // language=RegExp
        "(?:§.)*Shard Traps"
    ),
    STARBORN_TEMPLE(
        // language=RegExp
        "§9§lStarborn Temple:",
    ),
    PITY(
        // language=RegExp
        "§d§lPity:",
    ),
    PICKAXE_COOLDOWN(
        // language=RegExp
        "§9§lPickaxe Ability:",
    ),
    ;

    private val pattern: Regex = Regex("^\\s*(?:$headerRegex)\\s*$")

    var lines: List<String> = emptyList()
        private set

    var isPresent: Boolean = false
        private set

    companion object {
        fun update(tabLines: List<String>) {
            val headerPatterns: List<Regex> = entries.map { it.pattern }

            for (w in entries) {
                val start = tabLines.indexOfFirst { w.pattern.containsMatchIn(it) }
                if (start == -1) {
                    w.isPresent = false
                    w.lines = emptyList()
                    continue
                }

                w.lines = sliceSection(
                    tabLines = tabLines,
                    startIndex = start,
                    headerRegexes = headerPatterns,
                    maxBodyLines = 10
                )
                w.isPresent = true
            }
        }

        private fun sliceSection(
            tabLines: List<String>,
            startIndex: Int,
            headerRegexes: List<Regex>,
            maxBodyLines: Int
        ): List<String> {
            val out = ArrayList<String>(1 + maxBodyLines)
            out += tabLines[startIndex]

            var bodyCount = 0
            var i = startIndex + 1
            while (i < tabLines.size && bodyCount < maxBodyLines) {
                val line = tabLines[i]

                if (headerRegexes.any { it.containsMatchIn(line) }) break

                if (line.replace(Regex("§."), "").trim().isNotEmpty()) {
                    out += line
                    bodyCount++
                }
                i++
            }

            return out
        }
    }
}