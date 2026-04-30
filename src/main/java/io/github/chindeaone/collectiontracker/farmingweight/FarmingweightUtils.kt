package io.github.chindeaone.collectiontracker.farmingweight

import io.github.chindeaone.collectiontracker.api.farmingweight.EliteApiFetcher
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.utils.ColorUtils
import io.github.chindeaone.collectiontracker.utils.PlayerData
import io.github.chindeaone.collectiontracker.utils.ServerUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import io.github.chindeaone.collectiontracker.utils.toFWRankComponent
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

object FarmingweightUtils {

    private val playerCooldowns = mutableMapOf<String, Long>()
    private var lastPlayer: String? = null
    private const val COOLDOWN_DURATION = 5 * 60 * 1000L

    private fun isPlayerCached(name: String): Boolean {
        val lastFetch = playerCooldowns[name] ?: 0L
        return lastPlayer == name && (System.currentTimeMillis() - lastFetch < COOLDOWN_DURATION)
    }

    @JvmStatic
    fun getFarmingweight(playerName: String) {
        if (!ServerUtils.serverStatus) {
            ChatUtils.sendMessage("§cAPI server is currently offline. Please try again later.", true)
            return
        }

        ChatUtils.sendMessage("§aFetching Farming Weight for $playerName ...", true)

        if (isPlayerCached(playerName)) {
            displayFarmingweight(playerName, FarmingweightManager.storage)
            return
        }

        val resolvedProfileId = PlayerData.profileId
        EliteApiFetcher.fetchFarmingweightDataAsync(playerName, PlayerData.playerUUID, resolvedProfileId) {
            playerCooldowns[playerName] = System.currentTimeMillis()
            lastPlayer = playerName
            displayFarmingweight(playerName, FarmingweightManager.storage)
        }
    }

    @JvmStatic
    fun getFarmingweightLeaderboard(position: Int) {
        if (!ServerUtils.serverStatus) {
            ChatUtils.sendMessage("§cAPI server is currently offline. Please try again later.", true)
            return
        }
        if (position > 10000) {
            ChatUtils.sendMessage("§cRequested leaderboard length exceeds the maximum limit of 10000.", true)
            return
        }

        ChatUtils.sendMessage("§aFetching Top $position in Farming Weight...", true)
        EliteApiFetcher.fetchFarmingweightLbAsync {
            displayFarmingweightLeaderboard(position)
        }
    }

    private fun displayFarmingweight(playerName: String, storage: FarmingweightStorage) {
        val isMe = playerName.equals(PlayerData.playerName, ignoreCase = true)
        val rankComp = getRankComponent(storage.rank, isMe, playerName)
        val fullMessage = Component.empty().append(rankComp)
            .append(" §e$playerName's Farming Weight: ${storage.weight}")

        Minecraft.getInstance().execute { ChatUtils.sendComponent(fullMessage, true) }
    }

    private fun displayFarmingweightLeaderboard(position: Int) {
        val leaderboard = FarmingweightManager.storage.tempLeaderboard
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
                    .append(getRankComponent(rank, isMe, entry.name))
                    .append(" §a${entry.name}: §b${entry.weight}")
                ChatUtils.sendComponent(message, true)
            }
        }
    }

    fun getRankComponent(rank: Int, isMe: Boolean, playerName: String): Component {
        return rank.toFWRankComponent(isMe, playerName)
    }

    @JvmStatic
    fun setPlayerCustomColor(playerName: String, hexColor: String) {
        if (playerName.equals(PlayerData.playerName, ignoreCase = true)) {
            ChatUtils.sendMessage("§cYou cannot set a custom color for yourself like this. Use the feature in `/sct`.", true)
            return
        }
        ConfigHelper.setFarmingweightCustomColor(playerName, hexColor)
        val color: Component = ColorUtils.coloredText(hexColor)
        ChatUtils.sendComponent(Component.empty().append("§aCustom color for ").append(playerName).append("§a set to ").append(color).append("§a."), true)
    }

    @JvmStatic
    fun removePlayerCustomColor(playerName: String) {
        if (playerName.equals(PlayerData.playerName, ignoreCase = true)) {
            ChatUtils.sendMessage("§cYou cannot remove a custom color for yourself like this. Use the feature in `/sct`.", true)
            return
        }
        ConfigHelper.removeFarmingweightColor(playerName)
        ChatUtils.sendMessage("§aCustom color for $playerName removed.", true)
    }

    @JvmStatic
    fun setGlobalColor(color: String) {
        val playerName = PlayerData.playerName
        val uuid = PlayerData.playerUUID

        val top10Fw = FarmingweightManager.storage.leaderboard.take(10)
        if (top10Fw.none { it.name.equals(playerName, ignoreCase = true) }) {
            ChatUtils.sendMessage("§cYou must be in the top 10 of the Farming Weight leaderboard to set a global color.", true)
            return
        }
        EliteApiFetcher.setGlobalColor(playerName, uuid, color)
    }
}
