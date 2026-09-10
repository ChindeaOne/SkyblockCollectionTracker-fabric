package io.github.chindeaone.collectiontracker.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.suggestion.SuggestionProvider
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.coleweight.ColeweightUtils
import io.github.chindeaone.collectiontracker.farmingweight.FarmingweightUtils
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.gui.GuiManager
import io.github.chindeaone.collectiontracker.gui.OverlayManager
import io.github.chindeaone.collectiontracker.tracker.coleweight.ColeweightTrackingHandler
import io.github.chindeaone.collectiontracker.tracker.commissions.CommissionsTracker
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler
import io.github.chindeaone.collectiontracker.utils.PlayerData
import io.github.chindeaone.collectiontracker.utils.SkillUtils
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler
import io.github.chindeaone.collectiontracker.utils.HypixelUtils
import io.github.chindeaone.collectiontracker.utils.NumbersUtils
import io.github.chindeaone.collectiontracker.utils.ServerUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object CommandRegistry {

    fun init() {
        val sct = ClientCommands.literal(SkyblockCollectionTracker.NAMESPACE)

        ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher, _ ->
            ConfigCommandRegistry.register(dispatcher, sct)
            dispatcher.register(sct)
        })

        // sct -> opens the config GUI
        sct.executes {
            GuiManager.openConfigGui(null)
            1
        }

        // sct edit -> opens the position editor
        .then(ClientCommands.literal("edit")
            .executes {
                GuiManager.openGuiPositionEditor()
                1
            }
            .then(ClientCommands.literal("title")
                .executes {
                    GuiManager.openGuiTitlePositionEditor()
                    1
                }
            )
        )

        // sct commands -> shows the list of commands
        .then(ClientCommands.literal("commands")
            // sct commands -> shows first page of commands
            .executes {
                CommandList.showCommands(1)
                1
            }
            // sct commands <page>
            .then(ClientCommands.argument("page", IntegerArgumentType.integer(1))
                .executes {
                    val page: Int = IntegerArgumentType.getInteger(it, "page")
                    CommandList.showCommands(page)
                    1
                }
            )
        )

        // sct collections -> shows the list of collections
        .then(ClientCommands.literal("collections")
            // sct collections -> opens first category (page1)
            .executes {
                CollectionList.sendCollectionList(1)
                1
            }
            // sct collections <page> or <category>
            .then(ClientCommands.argument("arg", StringArgumentType.word())
                .suggests(COLLECTION_LIST)
                .executes {
                    val arg = StringArgumentType.getString(it, "arg")
                    // Try to parse as page number
                    val page = arg.toIntOrNull()

                    if (page != null) {
                        if (page < 1) {
                            ChatUtils.sendMessage("§cPage number must be at least 1.", true)
                        } else {
                            CollectionList.sendCollectionList(page)
                        }
                    } else {
                        // Not a number, treat as category
                        val categoryPage = CollectionList.getPageForCategory(arg)

                        if (categoryPage == null) {
                            ChatUtils.sendMessage("§cUnknown category.", true)
                        } else {
                            CollectionList.sendCollectionList(categoryPage)
                        }
                    }
                    1
                }
            )
        )

        // sct track <collection(s)/skill> -> general command covering both collection and skill tracking
        .then(ClientCommands.literal("track")
            .executes {
                ChatUtils.sendMessage("Usage: /sct track <collection/skill>", true)
                1
            }
            .then(ClientCommands.argument("targets", StringArgumentType.greedyString())
                .suggests(COLLECTION_AND_SKILL_SUGGESTIONS)
                .executes {
                    if (!canUseCommand()) return@executes 0

                    val input = StringArgumentType.getString(it, "targets").trim()

                    // only target is a skill
                    if (SkillUtils.isValidSkill(input)) {
                        SkillTracker.startTracking(input)
                        return@executes 1
                    }

                    val collections = CollectionsManager.allCollections.sortedByDescending { collection -> collection.length }
                    val foundCollections = mutableListOf<String>()

                    var remaining = input
                    while(remaining.isNotEmpty()) {
                        var found = false

                        for (coll in collections) {
                            if (remaining.startsWith(coll, ignoreCase = true)) {
                                foundCollections.add(coll)
                                remaining = remaining.substring(coll.length).trim()
                                found = true
                                break
                            }
                        }

                        if (!found) {
                            // skip
                            val nextSpace = remaining.indexOf(' ')
                            if (nextSpace == -1) break
                            remaining = remaining.substring(nextSpace).trim()
                        }
                    }

                    val containsSkill = input.split(' ').any { target -> SkillUtils.isValidSkill(target) }

                    if (containsSkill) {
                        ChatUtils.sendMessage("§cYou cannot track a skill and collections at the same time.")
                        return@executes 1
                    }

                    if (foundCollections.size > 1 || (foundCollections.size == 1 && foundCollections[0].equals("gemstone", ignoreCase = true))) {
                        CollectionTracker.startMultiTracking(foundCollections)
                    } else if (foundCollections.size == 1) {
                        CollectionTracker.startTracking(foundCollections[0])
                    } else {
                        CollectionTracker.startTracking(input)
                    }
                    1
                }
            )
        )

        // sct stop any tracker
        .then(ClientCommands.literal("stop")
            .executes {
                ChatUtils.sendMessage("Usage: /sct stop <collection/skill>", true)
                1
            }
            .then(ClientCommands.argument("type", StringArgumentType.word())
                .suggests(TRACKING_SUGGESTIONS)
                .executes {
                    if (!canUseCommand()) return@executes 0

                    val type = StringArgumentType.getString(it, "type").trim()
                    when (type) {
                        "collection" -> {
                            when {
                                MultiTrackingHandler.isMultiTracking -> MultiTrackingHandler.stopMultiTrackingManual()
                                TrackingHandler.isTracking -> TrackingHandler.stopTrackingManual()
                                else -> ChatUtils.sendMessage("§cNo collection is currently being tracked.", true)
                            }
                        }
                        "skill" -> SkillTrackingHandler.stopTrackingManual()
                        else -> ChatUtils.sendMessage("§cInvalid type. Use 'collection' or 'skill'.", true)
                    }
                    1
                }
            )
        )
        // sct pause any tracker
        .then(ClientCommands.literal("pause")
            .executes {
                ChatUtils.sendMessage("Usage: /sct pause <collection/skill>", true)
                1
            }
            .then(ClientCommands.argument("type", StringArgumentType.word())
                .suggests(TRACKING_SUGGESTIONS)
                .executes {
                    if (!canUseCommand()) return@executes 0
                    val type = StringArgumentType.getString(it, "type").trim()

                    when (type) {
                        "collection" -> {
                            when {
                                MultiTrackingHandler.isMultiTracking -> MultiTrackingHandler.pauseMultiTracking()
                                TrackingHandler.isTracking -> TrackingHandler.pauseTracking()
                                else -> ChatUtils.sendMessage("§cNo collection is currently being tracked.", true)
                            }
                        }
                        "skill" -> SkillTrackingHandler.pauseTracking()
                        else -> ChatUtils.sendMessage("§cInvalid type. Use 'collection' or 'skill'.", true)
                    }
                    1
                }
            )
        )

        // sct resume any tracker
        .then(ClientCommands.literal("resume")
            .executes {
                ChatUtils.sendMessage("Usage: /sct resume <collection/skill>", true)
                1
            }
            .then(ClientCommands.argument("type", StringArgumentType.word())
                .suggests(TRACKING_SUGGESTIONS)
                .executes {
                    if (!canUseCommand()) return@executes 0

                    val type = StringArgumentType.getString(it, "type").trim()
                    when (type) {
                        "collection" -> {
                            when {
                                MultiTrackingHandler.isMultiPaused -> MultiTrackingHandler.resumeMultiTracking()
                                TrackingHandler.isPaused -> TrackingHandler.resumeTracking()
                                else -> ChatUtils.sendMessage("§cNo collection is currently paused.", true)
                            }
                        }
                        "skill" -> SkillTrackingHandler.resumeTracking()
                        else -> ChatUtils.sendMessage("§cInvalid type. Use 'collection' or 'skill'.", true)
                    }
                    1
                }
            )
        )

        // sct restart any tracker
        .then(ClientCommands.literal("restart")
            .executes {
                ChatUtils.sendMessage("Usage: /sct restart <collection/skill>", true)
                1
            }
            .then(ClientCommands.argument("type", StringArgumentType.word())
                .suggests(TRACKING_SUGGESTIONS)
                .executes {
                    if (!canUseCommand()) return@executes 0

                    val type = StringArgumentType.getString(it, "type").trim()
                    when (type) {
                        "collection" -> {
                            when {
                                MultiTrackingHandler.isMultiTracking -> MultiTrackingHandler.restartMultiTracking()
                                TrackingHandler.isTracking -> TrackingHandler.restartTracking()
                                else -> ChatUtils.sendMessage("§cNo collection is currently being tracked.")
                            }
                        }
                        "skill" -> SkillTrackingHandler.restartTracking()
                        else -> ChatUtils.sendMessage("§cInvalid type. Use 'collection' or 'skill'.")
                    }
                    1
                }
            )
        )

        // sct cw -> shows player's coleweight
        .then(ClientCommands.literal("cw")
            // sct cw -> shows player's coleweight
            .executes {
                ColeweightUtils.getColeweight(PlayerData.playerName)
                1
            }
            // sct cw find <player> -> shows specified player(or local player)'s coleweight
            .then(ClientCommands.literal("find")
                .executes {
                    ColeweightUtils.getColeweight(PlayerData.playerName)
                    1
                }
                .then(ClientCommands.argument("player", StringArgumentType.string())
                    .suggests(PLAYER_SUGGESTIONS)
                    .executes {
                        val playerName = StringArgumentType.getString(it, "player").trim()
                        ColeweightUtils.getColeweight(playerName)
                        1
                    }
                )
            )
            .then(ClientCommands.literal("detailed")
                .executes {
                    val playerName = PlayerData.playerName
                    ColeweightUtils.getColeweightDetailed(playerName)
                    1
                }
                .then(ClientCommands.argument("player", StringArgumentType.string())
                    .suggests(PLAYER_SUGGESTIONS)
                    .executes {
                        val playerName = StringArgumentType.getString(it, "player").trim()
                        ColeweightUtils.getColeweightDetailed(playerName)
                        1
                    }
                )
            )
            // sct cw lb <length>
            .then(ClientCommands.literal("lb")
                .executes {
                    ChatUtils.sendMessage("Usage: /sct cw lb <length>.",true)
                    1
                }
                .then(ClientCommands.argument("position", IntegerArgumentType.integer())
                    .executes {
                        val position = IntegerArgumentType.getInteger(it, "position")
                        ColeweightUtils.getColeweightLeaderboard(position)
                        1
                    }
                )
            )
            // sct cw color set <ign> <color>
            .then(ClientCommands.literal("color")
                .executes {
                    ChatUtils.sendMessage("Usage: /sct cw color set <player name> <hex color>.",true)
                    1
                }
                .then(ClientCommands.literal("set")
                    .then(ClientCommands.argument("target", StringArgumentType.string())
                        .suggests { context, builder ->
                            val remaining = builder.remaining.lowercase()
                            if ("global".startsWith(remaining)) builder.suggest("global")
                            for (playerName in context.getSource().onlinePlayerNames) {
                                if (playerName.lowercase().startsWith(remaining)) builder.suggest(playerName)
                            }
                             builder.buildFuture()
                        }
                        .then(ClientCommands.argument("hex color", StringArgumentType.greedyString())
                            .executes {
                                val target = StringArgumentType.getString(it, "target").trim()
                                val color = StringArgumentType.getString(it, "hex color").trim()
                                val formattedHex = if (color.startsWith("#")) color else "#$color"

                                if (!formattedHex.matches("^#?[0-9a-fA-F]{6}$".toRegex())) {
                                    ChatUtils.sendMessage("§cInvalid color format.", true)
                                    return@executes 1
                                }

                                if (target.equals("global", ignoreCase = true)) {
                                    ColeweightUtils.setGlobalColor(formattedHex)
                                    ChatUtils.sendMessage("§aGlobal coleweight color set to $formattedHex", true)
                                } else {
                                    ColeweightUtils.setPlayerCustomColor(target, formattedHex)
                                    ChatUtils.sendMessage("§aColeweight color for $target set to $formattedHex", true)
                                }
                                1
                            }
                        )
                    )
                )
                .then(ClientCommands.literal("remove")
                    .executes {
                        ChatUtils.sendMessage("Usage: /sct cw color remove <player name>.",true)
                        1
                    }
                    .then(ClientCommands.argument("player name", StringArgumentType.string())
                        .suggests(PLAYER_SUGGESTIONS)
                        .executes {
                            val name = StringArgumentType.getString(it, "player name").trim()
                            ColeweightUtils.removePlayerCustomColor(name)
                            1
                        }
                    )
                )
            )
            .then(ClientCommands.literal("track")
                .executes {
                    if (!canUseCommand()) return@executes 0

                    ColeweightTrackingHandler.startTracking()
                    1
                }
            )
            .then(ClientCommands.literal("stop")
                .executes {
                    ColeweightTrackingHandler.stopTrackingManual()
                    1
                }
            )
            .then(ClientCommands.literal("pause")
                .executes {
                    ColeweightTrackingHandler.pauseTracking()
                    1
                }
            ).then(ClientCommands.literal("resume")
                .executes {
                    ColeweightTrackingHandler.resumeTracking()
                    1
                }
            )
            .then(ClientCommands.literal("restart")
                .executes {
                    ColeweightTrackingHandler.restartTracking()
                    1
                }
            )
        )

        // sct fw -> shows player's farming weight
        .then(ClientCommands.literal("fw")
            .executes {
                FarmingweightUtils.getFarmingweight(PlayerData.playerName)
                1
            }
            .then(ClientCommands.literal("find")
                .executes {
                    FarmingweightUtils.getFarmingweight(PlayerData.playerName)
                    1
                }
                .then(ClientCommands.argument("player", StringArgumentType.string())
                    .suggests(PLAYER_SUGGESTIONS)
                    .executes {
                        val playerName = StringArgumentType.getString(it, "player").trim()
                        FarmingweightUtils.getFarmingweight(playerName)
                        1
                    }
                )
            )
            .then(ClientCommands.literal("lb")
                .executes {
                    ChatUtils.sendMessage("Usage: /sct fw lb <length>.", true)
                    1
                }
                .then(ClientCommands.argument("position", IntegerArgumentType.integer())
                    .executes {
                        val position = IntegerArgumentType.getInteger(it, "position")
                        FarmingweightUtils.getFarmingweightLeaderboard(position)
                        1
                    }
                )
            )
            // sct fw color set <ign> <color>
            .then(ClientCommands.literal("color")
                .executes {
                    ChatUtils.sendMessage("Usage: /sct fw color set <player name> <hex color>.", true)
                    1
                }
                .then(ClientCommands.literal("set")
                    .then(ClientCommands.argument("target", StringArgumentType.string())
                        .suggests { context, builder ->
                            val remaining = builder.remaining.lowercase()
                            if ("global".startsWith(remaining)) {
                                builder.suggest("global")
                            }
                            for (playerName in context.getSource().onlinePlayerNames) {
                                if (playerName.lowercase().startsWith(remaining)) {
                                    builder.suggest(playerName)
                                }
                            }
                            builder.buildFuture()
                        }
                        .then(ClientCommands.argument("hex color", StringArgumentType.greedyString())
                            .executes {
                                val target = StringArgumentType.getString(it, "target").trim()
                                val color = StringArgumentType.getString(it, "hex color").trim()
                                val formattedHex = if (color.startsWith("#")) color else "#$color"

                                if (!formattedHex.matches("^#?[0-9a-fA-F]{6}$".toRegex())) {
                                    ChatUtils.sendMessage("§cInvalid color format. Use hex format like #RRGGBB.", true)
                                    return@executes 1
                                }

                                // 3. Handle the logic internally based on the "target" string
                                if (target.equals("global", ignoreCase = true)) {
                                    FarmingweightUtils.setGlobalColor(formattedHex)
                                    ChatUtils.sendMessage("§aGlobal farmingweight color set to $formattedHex", true)
                                } else {
                                    FarmingweightUtils.setPlayerCustomColor(target, formattedHex)
                                    ChatUtils.sendMessage("§aFarmingweight color for $target set to $formattedHex", true)
                                }
                                1
                            }
                        )
                    )
                )
                .then(ClientCommands.literal("remove")
                    .executes {
                        ChatUtils.sendMessage("Usage: /sct fw color remove <player name>.", true)
                        1
                    }
                    .then(ClientCommands.argument("player name", StringArgumentType.string())
                        .suggests(PLAYER_SUGGESTIONS)
                        .executes {
                            val name = StringArgumentType.getString(it, "player name").trim()
                            FarmingweightUtils.removePlayerCustomColor(name)
                            1
                        }
                    )
                )
            )
        )

        // sct changelog -> opens the changelog GUI
        .then(ClientCommands.literal("changelog")
            .executes {
                GuiManager.openChangelog()
                1
            }
        )

        // sct timer -> timer commands
        .then(ClientCommands.literal("timer")
            .then(ClientCommands.literal("set")
                .then(ClientCommands.argument("time", StringArgumentType.greedyString())
                    .executes {
                        val time = StringArgumentType.getString(it, "time")
                        val seconds = NumbersUtils.parseToSeconds(time)

                        if (seconds < 0) {
                            ChatUtils.sendMessage("§cInvalid time format. Use formats like '1h30m', '45s', or '90'.", true)
                            return@executes 1
                        }

                        val timer = OverlayManager.getTimerOverlay()
                        timer?.setTimer(seconds)
                        1
                    }
                )
            )
            .then(ClientCommands.literal("pause")
                .executes {
                    val timer = OverlayManager.getTimerOverlay()
                    timer?.pauseTimer()
                    1
                }
            )
            .then(ClientCommands.literal("resume")
                .executes {
                    val timer = OverlayManager.getTimerOverlay()
                    timer?.pauseTimer()
                    1
                }
            )
            .then(ClientCommands.literal("stop")
                .executes {
                    val timer = OverlayManager.getTimerOverlay()
                    timer?.setTimer(0)
                    1
                }
            )
        )
        // sct stopwatch -> stopwatch commands
        .then(ClientCommands.literal("stopwatch")
            .then(ClientCommands.literal("start")
                .executes {
                    val stopwatch = OverlayManager.getStopwatchOverlay()
                    stopwatch?.startStopwatch()
                    1
                }
            )
            .then(ClientCommands.literal("pause")
                .executes {
                    val stopwatch = OverlayManager.getStopwatchOverlay()
                    stopwatch?.pauseStopwatch()
                    1
                }
            )
            .then(ClientCommands.literal("resume")
                .executes {
                    val stopwatch = OverlayManager.getStopwatchOverlay()
                    stopwatch?.pauseStopwatch()
                    1
                }
            )
            .then(ClientCommands.literal("stop")
                .executes {
                    val stopwatch = OverlayManager.getStopwatchOverlay()
                    stopwatch?.stopStopwatch()
                    1
                }
            )
        )

        // sct leaderboard -> open custom screen to set custom leaderboard positions for collections and skills
        .then(ClientCommands.literal("leaderboard")
            .executes {
                GuiManager.openLeaderboardScreen()
                1
            }
        )

        // sct milestones -> open custom screen to set custom milestones for collections and skills
        .then(ClientCommands.literal("milestones")
            .executes {
                GuiManager.openMilestonesScreen()
                1
            }
        )

        // sct resetCommissionTracker -> resets commissions tracker
        .then(ClientCommands.literal("resetCommissionTracker")
            .executes {
                CommissionsTracker.reset()
                ChatUtils.sendMessage("§aCommissions tracker has been reset.", true)
                1
            }
        )

        // sct token -> fetches a new token from the server
        .then(ClientCommands.literal("token")
            .executes {
                ApiManager.fetchToken()
                1
            }
        )
    }

    private val TRACKING_SUGGESTIONS: SuggestionProvider<FabricClientCommandSource> = { _, builder ->
        builder.suggest("collection").suggest("skill").buildFuture()
    }

    private val COLLECTION_SUGGESTIONS: SuggestionProvider<FabricClientCommandSource> = { _, builder ->
        val arg = builder.remaining.lowercase()
        var prefix = ""
        var lastWord = ""

        if (arg.isEmpty()) {
            // no input, suggest collections
            prefix = ""
            lastWord = "" // reminder that empty string is prefix for any string
        } else if (Character.isWhitespace(arg[arg.length - 1])){
            // new collection, suggest collections and keep previous collections
            prefix = arg
            lastWord = ""
        } else {
            val lastSpace = arg.lastIndexOf(' ').coerceAtLeast(arg.lastIndexOf('\t'))
            if (lastSpace == -1) { // no space = first collection
                prefix = ""
                lastWord = arg
            } else { // more than one collection, divide by last space position to suggest next collection
                prefix = arg.substring(0, lastSpace + 1)
                lastWord = arg.substring(lastSpace + 1)
            }
        }

        for (c in CollectionsManager.allCollections) {
            val matches = c
                .lowercase()
                .split("\\s+")
                .any{ word -> word.startsWith(lastWord) }

            if (matches) {
                builder.suggest(prefix + c)
            }
        }
        builder.buildFuture()
    }

    private val COLLECTION_LIST: SuggestionProvider<FabricClientCommandSource> = { _, builder ->
        val arg = builder.remaining.lowercase()
        for (category in CollectionsManager.collections.keys) {
            if (category.lowercase().startsWith(arg)) {
                builder.suggest(category)
            }
        }
        builder.buildFuture()
    }

    private val SKILL_LIST: SuggestionProvider<FabricClientCommandSource> = { _, builder ->
        val arg = builder.remaining.lowercase()
        for (skill in SkillUtils.getDisplayNames()) {
            if (skill.lowercase().startsWith(arg)) {
                builder.suggest(skill)
            }
        }
        builder.buildFuture()
    }

    private val COLLECTION_AND_SKILL_SUGGESTIONS: SuggestionProvider<FabricClientCommandSource> = { context, builder ->
        val collectionSuggestions = COLLECTION_SUGGESTIONS.getSuggestions(context, builder.createOffset(builder.start)).join()
        val skillSuggestions = SKILL_LIST.getSuggestions(context, builder.createOffset(builder.start)).join()

        for (suggestion in collectionSuggestions.list) {
            builder.suggest(suggestion.text)
        }
        for (suggestion in skillSuggestions.list) {
            builder.suggest(suggestion.text)
        }

        builder.buildFuture()
    }

    private val PLAYER_SUGGESTIONS: SuggestionProvider<FabricClientCommandSource> = { context, builder ->
        val remaining = builder.remaining.lowercase()
        for (playerName in context.source.onlinePlayerNames) {
            if (playerName.lowercase().startsWith(remaining)) {
                builder.suggest(playerName)
            }
        }
        builder.buildFuture()
    }

    private fun canUseCommand(): Boolean {
        return checkIfInSkyblock() && checkIfServerIsOnline()
    }

    private fun checkIfInSkyblock(): Boolean {
        if (!HypixelUtils.isInSkyblock) {
            ChatUtils.sendMessage("§cYou must be on Hypixel Skyblock to use this command!", true)
            return false
        }
        return true
    }

    private fun checkIfServerIsOnline(): Boolean {
        if (!ServerUtils.serverStatus) {
            ChatUtils.sendMessage("§cThe API server is currently offline. Please try again later.", true)
            return false
        }
        return true
    }
}