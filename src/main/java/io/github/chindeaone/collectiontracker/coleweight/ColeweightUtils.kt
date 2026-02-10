package io.github.chindeaone.collectiontracker.coleweight

import io.github.chindeaone.collectiontracker.api.coleweight.ColeweightFetcher
import io.github.chindeaone.collectiontracker.util.ChatUtils
import io.github.chindeaone.collectiontracker.util.ServerUtils
import net.minecraft.client.Minecraft

object ColeweightUtils {

    private val playerCooldowns = mutableMapOf<String, Long>()
    private var lastPlayer: String? = null
    private const val COOLDOWN_DURATION = 5 * 60 * 1000L // 5 minutes cd

    private fun isPlayerCached(name: String): Boolean {
        val lastFetch = playerCooldowns[name] ?: 0L
        return lastPlayer == name && (System.currentTimeMillis() - lastFetch < COOLDOWN_DURATION)
    }

    fun getColeweight(playerName: String, detailed: Boolean = false) {
        if (!ServerUtils.serverStatus) {
            ChatUtils.sendMessage("§cAPI server is currently offline. Please try again later.", true)
            return
        }

        val msg = if (detailed) "detailed Coleweight" else "Coleweight"
        ChatUtils.sendMessage("§aFetching $msg for $playerName ...", true)

        if (isPlayerCached(playerName)) {
            displayColeweight(playerName, ColeweightManager.storage, detailed)
            return
        }

        ColeweightFetcher.fetchColeweightDataAsync(playerName) {
            playerCooldowns[playerName] = System.currentTimeMillis()
            lastPlayer = playerName
            displayColeweight(playerName, ColeweightManager.storage, detailed)
        }
    }

    fun getColeweightDetailed(playerName: String) {
        getColeweight(playerName, true)
    }

    fun getColeweightLeaderboard(position: Int) {
        if (!ServerUtils.serverStatus) {
            ChatUtils.sendMessage("§cAPI server is currently offline. Please try again later.", true)
            return
        }
        if (position > 5000) {
            ChatUtils.sendMessage("§cRequested leaderboard length exceeds the maximum limit of 5000.", true)
            return
        }

        ChatUtils.sendMessage("§aFetching Top $position in Coleweight...", true)
        ColeweightFetcher.fetchColeweightLbAsync {
            displayColeweightLeaderboard(position)
        }
    }

    private fun displayColeweight(playerName: String, storage: ColeweightStorage, detailed: Boolean = false) {
        val message = if (detailed) {
            buildString {
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
        } else "${getRankColors(storage.rank)} §b$playerName's Coleweight: ${storage.coleweight} (Top ${storage.percentage}%)"
        Minecraft.getInstance().execute { ChatUtils.sendMessage(message, true) }
    }

    private fun displayColeweightLeaderboard(position: Int) {
        val leaderboard = ColeweightManager.storage.tempLeaderboard
        val (start, end) = if (position <= 100) {
            0 to minOf(100, leaderboard.size)
        } else {
            val startIndex = minOf(position - 25, leaderboard.size)
            val endIndex = minOf(position + 25, leaderboard.size)
            startIndex to endIndex
        }
        val subList = leaderboard.subList(start, end)

        Minecraft.getInstance().execute {
            subList.forEachIndexed { index, (player, coleweight) ->
                val message = "${getRankColors(index + 1)} §a$player: §b$coleweight"
                ChatUtils.sendMessage(message, true)
            }
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
            else -> "[CW #${rank}]§r" // No color for ranks above 1000
        }
    }
}