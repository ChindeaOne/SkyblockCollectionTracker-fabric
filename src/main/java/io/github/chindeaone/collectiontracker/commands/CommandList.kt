package io.github.chindeaone.collectiontracker.commands

import io.github.chindeaone.collectiontracker.utils.Colors
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent

object CommandList {

    private data class CommandEntry(
        val command: String,
        val description: String
    )

    private data class CommandPage(
        val category: String,
        val color: Int,
        val entries: List<CommandEntry>
    )

    private val PAGES = listOf<CommandPage>(
        CommandPage("General", Colors.YELLOW.color, listOf(
            CommandEntry("/sct", "Opens the GUI."),
            CommandEntry("/sct commands", "Shows this message."),
            CommandEntry("/sct commands <page>", "Jumps to a specific commands page."),
            CommandEntry("/sct changelog", "Shows all recent changes to the mod."),
            CommandEntry("/sct token", "Requests a new token if one is missing.")
        )),

        CommandPage("Editing", Colors.LIGHT_PURPLE.color, listOf(
            CommandEntry("/sct edit", "Opens the position editor."),
            CommandEntry("/sct edit title", "Opens the title position editor.")
        )),

        CommandPage("Tracking", Colors.DARK_AQUA.color, listOf(
            CommandEntry("/sct track <collection 1> <collection 2> ...", "Tracks one or more collections at once."),
            CommandEntry("/sct track <skill>", "Tracks your skill progress."),
            CommandEntry("/sct stop <collection|skill>", "Stops collection or skill tracking."),
            CommandEntry("/sct pause <collection|skill>", "Pauses collection or skill tracking."),
            CommandEntry("/sct resume <collection|skill>", "Resumes collection or skill tracking."),
            CommandEntry("/sct restart <collection|skill>", "Restarts collection or skill tracking."),
        )),

        CommandPage("Miscellaneous", Colors.BLUE.color, listOf(
            CommandEntry("/sct collections", "Shows all available collections (page 1)."),
            CommandEntry("/sct collections <page | category>", "Jumps to a specific collections page or category."),

            CommandEntry("/sct resetCommissionTracker", "Resets the commissions tracker."),

            CommandEntry("/sct leaderboard", "Opens a custom screen to view the leaderboard for collections and skills."),
            CommandEntry("/sct milestones", "Opens a custom screen to set your milestones for collections and skills."),
        )),

        CommandPage("Coleweight", Colors.AQUA.color, listOf(
            CommandEntry("/sct cw", "Shows your Coleweight."),
            CommandEntry("/sct cw find [player]", "Shows a player's Coleweight."),
            CommandEntry("/sct cw detailed [player]", "Shows detailed Coleweight information."),
            CommandEntry("/sct cw lb <length>", "Shows the Coleweight leaderboard."),

            CommandEntry("/sct cw track", "Starts Coleweight tracking."),
            CommandEntry("/sct cw pause", "Pauses Coleweight tracking."),
            CommandEntry("/sct cw resume", "Resumes Coleweight tracking."),
            CommandEntry("/sct cw restart", "Restarts Coleweight tracking."),
            CommandEntry("/sct cw stop", "Stops Coleweight tracking."),

            CommandEntry("/sct cw color set <player name> <hex color>", "Sets a custom Coleweight rank color for a player (client side only)."),
            CommandEntry("/sct cw color remove <player name>", "Removes a player's custom Coleweight rank color (client side only)."),
            CommandEntry("/sct cw color set global <hex color>", "Sets your own global Coleweight rank color (available only for the top 10 players in Coleweight)."),

            CommandEntry("/sct timer <set | pause | resume | stop>", "Manages the general timer."),
            CommandEntry("/sct sendTimer", "Sends your current timer in party chat."),
            CommandEntry("/sct stopwatch <start | pause | resume | stop>", "Manages the stopwatch."),
            CommandEntry("/sct sendStopwatch", "Sends your current stopwatch in party chat.")
        )),

        CommandPage("Farming Weight", Colors.GREEN.color, listOf(
            CommandEntry("/sct fw", "Shows your Farming Weight."),
            CommandEntry("/sct fw find [player]", "Shows a player's Farming Weight."),
            CommandEntry("/sct fw lb <length>", "Shows the Farming Weight leaderboard."),

            CommandEntry("/sct fw color set <player name> <hex color>", "Sets a custom Farming Weight rank color for a player (client side only)."),
            CommandEntry("/sct fw color remove <player name>", "Removes a player's custom Farming Weight rank color (client side only)."),
            CommandEntry("/sct fw color set global <hex color>", "Sets your own global Farming Weight rank color (available only for the top 10 players in Farming Weight).")
        )),

        CommandPage("Config Toggles", Colors.DARK_PURPLE.color, listOf(
            CommandEntry("/sct config toggleMiningStats", "Toggles the mining stats overlay."),
            CommandEntry("/sct config toggleMiningStatsOnlyOnMiningIslands", "Toggles the mining stats overlay in mining islands only."),
            CommandEntry("/sct config togglePickaxeAbility", "Toggles the pickaxe ability display."),
            CommandEntry("/sct config togglePickaxeAbilityOnlyOnMiningIslands", "Toggles the pickaxe ability display in mining islands only."),
            CommandEntry("/sct config togglePickaxeAbilityReadyTitle", "Toggles the pickaxe ability ready title."),
            CommandEntry("/sct config togglePickaxeAbilityExpiredTitle", "Toggles the pickaxe ability expired title."),
            CommandEntry("/sct config toggleSkyMall", "Toggles Sky Mall overlay."),
            CommandEntry("/sct config toggleSkyMallOnlyOnMiningIslands", "Toggles Sky Mall overlay in mining islands only."),
            CommandEntry("/sct config toggleSkyMallChatMessages", "Toggles Sky Mall chat messages."),
            CommandEntry("/sct config toggleCommissionsOverlay", "Toggles the commissions overlay."),
            CommandEntry("/sct config toggleCommissionsTracking", "Toggles the commissions tracking sub-overlay."),
            CommandEntry("/sct config toggleCommissionsKeybinds", "Toggles the commissions keybinds."),
            CommandEntry("/sct config toggleTempBuffTracker", "Toggles the temporary buff tracker."),
            CommandEntry("/sct config toggleTempBuffExpiredTitle", "Toggles the temporary buff expired title."),
            CommandEntry("/sct config toggleForagingStats", "Toggles the foraging stats overlay."),
            CommandEntry("/sct config toggleForagingStatsOnlyOnForagingIslands", "Toggles the foraging stats overlay in foraging islands only."),
            CommandEntry("/sct config toggleAxeAbility", "Toggles the axe ability display."),
            CommandEntry("/sct config toggleAxeAbilityOnlyOnForagingIslands", "Toggles the axe ability display in foraging islands only."),
            CommandEntry("/sct config toggleAxeAbilityReadyTitle", "Toggles the axe ability ready title."),
            CommandEntry("/sct config toggleAxeAbilityExpiredTitle", "Toggles the axe ability expired title."),
            CommandEntry("/sct config toggleLottery", "Toggles Lottery overlay."),
            CommandEntry("/sct config toggleLotteryOnlyOnForagingIslands", "Toggles Lottery overlay in foraging islands only."),
            CommandEntry("/sct config toggleLotteryChatMessages", "Toggles Lottery chat messages."),
            CommandEntry("/sct config toggleBeekeeper", "Toggles Beekeeper overlay."),
            CommandEntry("/sct config toggleBeekeeperOnlyOnForagingIslands", "Toggles Beekeeper overlay in foraging islands only."),
            CommandEntry("/sct config toggleBeekeeperChatMessages", "Toggles Beekeeper chat messages."),
            CommandEntry("/sct config toggleColeweightRankingInChat", "Toggles Coleweight ranking in chat."),
            CommandEntry("/sct config toggleColeweightRankInNameTag", "Toggles Coleweight rank in name tag."),
            CommandEntry("/sct config toggleColeweightOnlyOnMiningIslands", "Toggles Coleweight rank in name tag in mining islands only."),
            CommandEntry("/sct config toggleFarmingweightRankingInChat", "Toggles Farmingweight ranking in chat."),
            CommandEntry("/sct config toggleFarmingweightRankInNameTag", "Toggles Farmingweight rank in name tag."),
            CommandEntry("/sct config toggleFarmingweightOnlyOnFarmingIslands", "Toggles Farmingweight rank in name tag in farming islands only."),
            CommandEntry("/sct config toggleHeatmap", "Toggles the heatmap."),
            CommandEntry("/sct config toggleCollectionLeaderboard", "Toggles the collection leaderboard."),
            CommandEntry("/sct config toggleSkillLeaderboard", "Toggles the skill leaderboard."),
            CommandEntry("/sct config toggleCollectionMilestones", "Toggles the collection milestones overlay."),
            CommandEntry("/sct config toggleCollectionMilestonesTitleNotification", "Toggles the collection milestones title notification."),
            CommandEntry("/sct config toggleCollectionMilestonesSoundNotification", "Toggles the collection milestones sound notification."),
            CommandEntry("/sct config toggleSkillMilestones", "Toggles the skill milestones overlay."),
            CommandEntry("/sct config toggleSkillMilestonesTitleNotification", "Toggles the skill milestones title notification."),
            CommandEntry("/sct config toggleSkillMilestonesSoundNotification", "Toggles the skill milestones sound notification."),
            CommandEntry("/sct config toggleTimerNotifier", "Toggles the timer party notifier."),
            CommandEntry("/sct config toggleStopwatchNotifier", "Toggles the stopwatch party notifier.")
        ))
    )

    fun showCommands(page: Int) {
        val totalPages = PAGES.size
        val page = page.coerceIn(1, totalPages)
        val current = PAGES[page - 1]
        val components = buildComponentsForPage(current)

        ChatUtils.sendCommandPage(current.category, current.color, components, page, totalPages)
    }

    private fun buildComponentsForPage(commandPage: CommandPage): List<MutableComponent> {
        val components: MutableList<MutableComponent> = ArrayList()
        for (entry in commandPage.entries) {
            val comp = Component.literal("◆" + entry.command).withColor(commandPage.color)
            comp.withStyle {
                style -> style.withHoverEvent(HoverEvent.ShowText(Component.literal(entry.description).withColor(Colors.YELLOW.color)))

            }
            components.add(comp)
        }
        return components
    }
}
