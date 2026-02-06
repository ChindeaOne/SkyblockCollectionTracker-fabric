package io.github.chindeaone.collectiontracker.coleweight

import io.github.chindeaone.collectiontracker.api.coleweight.ColeweightFetcher
import io.github.chindeaone.collectiontracker.util.ChatUtils
import net.minecraft.client.Minecraft

object ColeweightUtils {

    fun getColeweight(playerName: String) {
        ChatUtils.sendMessage("§aFetching Coleweight for $playerName ...", true)

        ColeweightFetcher.fetchColeweightDataAsync(playerName) {
            val storage = ColeweightManager.storage
            val message = "${getRankColors(storage.rank)} §b$playerName's Coleweight: ${storage.coleweight} (Top ${storage.percentage}%)"
            Minecraft.getInstance().execute { ChatUtils.sendMessage(message, true) }
        }
    }

    fun getColeweightLeaderboard(length: Int) {
        ChatUtils.sendMessage("§aFetching Coleweight Leaderboard...", true)

        ColeweightFetcher.fetchColeweightLbAsync(length) {
            val leaderboard = ColeweightManager.storage.tempLeaderboard
            Minecraft.getInstance().execute {
                leaderboard.forEachIndexed { index, (player, coleweight) ->
                    val message = "${getRankColors(index + 1)} §a$player: §b$coleweight"
                    ChatUtils.sendMessage(message, true)
                }
            }
        }
    }

    fun getColeweightDetailed(playerName: String) {
        ChatUtils.sendMessage("§aFetching detailed Coleweight for $playerName ...", true)

        ColeweightFetcher.fetchColeweightDataAsync(playerName) {
            val storage = ColeweightManager.storage
            val message = buildString {
                appendLine("${getRankColors(storage.rank)} §b$playerName's Coleweight: ${storage.coleweight} (Top ${storage.percentage}%)")
                appendLine("§6Experience:")
                storage.experience.forEach { (k, v) -> appendLine("  §e$k: §b$v") }
                appendLine("§6Powder:")
                storage.powder.forEach { (k, v) -> appendLine("  §e$k: §b$v") }
                appendLine("§6Collection:")
                storage.collection.forEach { (k, v) -> appendLine("  §e$k: §b$v") }
                appendLine("§6Miscellaneous:")
                storage.miscellaneous.forEach { (k, v) -> appendLine("  §e$k: §b$v") }
            }
            Minecraft.getInstance().execute { ChatUtils.sendMessage(message, true) }
        }
    }

    fun getRankColors(rank: Int): String {
        return when (rank) {
            1 -> "§0[CW #${rank}]§r" // Black
            2 -> "§4[CW #${rank}]§r" // Dark Red
            3 -> "§2[CW #${rank}]§r" // Dark Green
            in 4..25 -> "§6[CW #${rank}]§r" // Gold
            in 26..100 -> "§3[CW #${rank}]§r" // Dark Aqua
            in 101..250 -> "§b[CW #${rank}]§r" // Aqua
            in 251..500 -> "§9[CW #${rank}]§r" // Blue
            in 501..1000 -> "§7[CW #${rank}]§r" // Gray
            else -> "" // No color for ranks above 1000
        }
    }
}