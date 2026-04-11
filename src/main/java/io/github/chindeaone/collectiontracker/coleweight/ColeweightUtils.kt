package io.github.chindeaone.collectiontracker.coleweight

import io.github.chindeaone.collectiontracker.api.coleweight.ColeweightFetcher
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.utils.ColorUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import io.github.chindeaone.collectiontracker.utils.PlayerData
import io.github.chindeaone.collectiontracker.utils.ServerUtils
import io.github.chindeaone.collectiontracker.utils.toRankComponent
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

object ColeweightUtils {

    private val playerCooldowns = mutableMapOf<String, Long>()
    private var lastPlayer: String? = null
    private const val COOLDOWN_DURATION = 5 * 60 * 1000L // 5 minutes cd

    private fun isPlayerCached(name: String): Boolean {
        val lastFetch = playerCooldowns[name] ?: 0L
        return lastPlayer == name && (System.currentTimeMillis() - lastFetch < COOLDOWN_DURATION)
    }

    @JvmStatic
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

    @JvmStatic
    fun getColeweightDetailed(playerName: String) {
        getColeweight(playerName, true)
    }

    @JvmStatic
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
        val isMe = playerName.equals(PlayerData.playerName, ignoreCase = true)
        val rankComp = getCustomColor(storage.rank, isMe, playerName)

        val fullMessage = Component.empty().append(rankComp).append(" §b$playerName's Coleweight: ${storage.coleweight} (Top ${storage.percentage}%)")

        if (detailed) {
            fullMessage.append("\n§6Experience:")
            storage.experience.forEach { (k, v) -> fullMessage.append("\n  §e$k: §b$v") }
            fullMessage.append("\n§6Powder:")
            storage.powder.forEach { (k, v) -> fullMessage.append("\n  §e$k: §b$v") }
            fullMessage.append("\n§6Collection:")
            storage.collection.forEach { (k, v) -> fullMessage.append("\n  §e$k: §b$v") }
            fullMessage.append("\n§6Miscellaneous:")
            storage.miscellaneous.forEach { (k, v) -> fullMessage.append("\n  §e$k: §b$v") }
        }

        Minecraft.getInstance().execute { ChatUtils.sendComponent(fullMessage, true) }
    }

    private fun displayColeweightLeaderboard(position: Int) {
        val leaderboard = ColeweightManager.storage.tempLeaderboard
        val (start, end) = if (position <= 100) {
            0 to minOf(position, leaderboard.size)
        } else {
            val startIndex = minOf(position - 15, leaderboard.size)
            val endIndex = minOf(position + 16, leaderboard.size)
            startIndex to endIndex
        }
        val subList = leaderboard.subList(start, end)

        Minecraft.getInstance().execute {
            subList.forEachIndexed { index, entry ->
                val rank = start + index + 1
                val isMe = entry.name.equals(PlayerData.playerName, ignoreCase = true)
                val message = Component.empty()
                    .append(getCustomColor(rank, isMe, entry.name))
                    .append(" §a${entry.name}: §b${entry.coleweight}")
                ChatUtils.sendComponent(message, true)
            }
        }
    }

    fun getCustomColor(rank: Int, isMe: Boolean, playerName: String): Component {
        return rank.toRankComponent(isMe, playerName)
    }

    @JvmStatic
    fun setPlayerCustomColor(playerName: String, hexColor: String) {
        if (playerName.equals(PlayerData.playerName, ignoreCase = true)) {
            ChatUtils.sendMessage("§cYou cannot set a custom color for yourself like this. Use the feature in `/sct`.", true)
            return
        }
        ConfigHelper.setColeweightCustomColor(playerName, hexColor)
        val color: Component = ColorUtils.coloredText(hexColor)
        ChatUtils.sendComponent(Component.empty().append("§aCustom color for ").append(playerName).append("§a set to ").append(color).append("§a."), true)
    }

    @JvmStatic
    fun removePlayerCustomColor(playerName: String) {
        if (playerName.equals(PlayerData.playerName, ignoreCase = true)) {
            ChatUtils.sendMessage("§cYou cannot remove a custom color for yourself like this. Use the feature in `/sct`.", true)
            return
        }
        ConfigHelper.removeColeweightCustomColor(playerName)
        ChatUtils.sendMessage("§aCustom color for $playerName removed.", true)
    }

    @JvmStatic
    fun setGlobalColor(color: String) {
        val top10Cw = ColeweightManager.storage.leaderboard.take(10)
        if (top10Cw.none { it.name.equals(PlayerData.playerName, ignoreCase = true) }) {
            ChatUtils.sendMessage("§cYou must be in the top 10 of the Coleweight leaderboard to set a global color.", true)
            return
        }
        ColeweightFetcher.setGlobalColor(PlayerData.playerName, color)
    }
}