package io.github.chindeaone.collectiontracker.utils.hotx

enum class MineshaftMayhemBuff(
    val pattern: Regex,
    val formattedBuff: String
) {
    SCRAP_CHANCE(
        Regex("""^Your Suspicious Scrap chance was buffed by your Mineshaft Mayhem perk!""", RegexOption.IGNORE_CASE),
        "§9Suspicious Scrap Chance"
    ),
    MINING_FORTUNE(
        Regex("""^You received a (?:\uE053 )?Mining Fortune buff from your Mineshaft Mayhem perk!""", RegexOption.IGNORE_CASE),
        "§6+100\uE053 Mining Fortune"
    ),
    MINING_SPEED(
        Regex("""^You received a (?:\uE015 )?Mining Speed buff from your Mineshaft Mayhem perk!""", RegexOption.IGNORE_CASE),
        "§6+200\uE015 Mining Speed"
    ),
    COLD_RESISTANCE(
        Regex("""^You received a (?:\uE006 )?Cold Resistance buff from your Mineshaft Mayhem perk!""", RegexOption.IGNORE_CASE),
        "§b+10\uE006 Cold Resistance"
    ),
    ABILITY_COOLDOWN(
        Regex("""^Your Pickaxe Ability cooldown was reduced from your Mineshaft Mayhem perk!""", RegexOption.IGNORE_CASE),
        "§9Pickaxe Cooldown Reduction"
    );

    companion object {
        fun formatMayhemBuff(message: String): String? {
            for (buff in entries) {
                if (buff.pattern.containsMatchIn(message)) {
                    return buff.formattedBuff
                }
            }
            return null
        }
    }
}