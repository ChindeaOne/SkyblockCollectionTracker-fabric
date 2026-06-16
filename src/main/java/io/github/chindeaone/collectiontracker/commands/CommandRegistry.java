package io.github.chindeaone.collectiontracker.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker;
import io.github.chindeaone.collectiontracker.coleweight.ColeweightUtils;
import io.github.chindeaone.collectiontracker.config.ConfigHelper;
import io.github.chindeaone.collectiontracker.config.categories.overlay.LeaderboardConfig;
import io.github.chindeaone.collectiontracker.farmingweight.FarmingweightUtils;
import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.gui.GuiManager;
import io.github.chindeaone.collectiontracker.gui.OverlayManager;
import io.github.chindeaone.collectiontracker.gui.overlays.StopwatchOverlay;
import io.github.chindeaone.collectiontracker.gui.overlays.TimerOverlay;
import io.github.chindeaone.collectiontracker.tracker.coleweight.ColeweightTrackingHandler;
import io.github.chindeaone.collectiontracker.tracker.commissions.CommissionsTracker;
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler;
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler;
import io.github.chindeaone.collectiontracker.utils.PlayerData;
import io.github.chindeaone.collectiontracker.utils.SkillUtils;
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler;
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandRegistry {

    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) ->
                dispatcher.register(ClientCommands.literal(SkyblockCollectionTracker.NAMESPACE)
                // sct -> opens the config GUI
                .executes(_ -> {
                    GuiManager.INSTANCE.openConfigGui(null);
                    return 1;
                })
                // sct edit -> opens the position editor
                .then(ClientCommands.literal("edit")
                        .executes(_ -> {
                            Minecraft.getInstance().execute(GuiManager::openGuiPositionEditor);
                            return 1;
                        })
                        .then(ClientCommands.literal("title")
                                .executes(_ -> {
                                    Minecraft.getInstance().execute(GuiManager::openGuiTitlePositionEditor);
                                    return 1;
                                })
                        )
                )
                // sct commands -> shows the list of commands
                .then(ClientCommands.literal("commands")
                        // sct commands -> shows first page of commands
                        .executes(_ -> {
                            CommandHelper.showCommands(1);
                            return 1;
                        })
                        // sct commands <page>
                        .then(ClientCommands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    int page = IntegerArgumentType.getInteger(context, "page");
                                    CommandHelper.showCommands(page);
                                    return 1;
                                })
                        )
                )
                // sct collections -> shows the list of collections
                .then(ClientCommands.literal("collections")
                        // sct collections -> opens first category (page 1)
                        .executes(_ -> {
                            CollectionList.sendCollectionList(1);
                            return 1;
                        })

                        // sct collections <page> or <category>
                        .then(ClientCommands.argument("arg", StringArgumentType.word())
                                .suggests(CATEGORY_SUGGESTIONS)
                                .executes(context -> {
                                    String arg = StringArgumentType.getString(context, "arg");
                                    // Try to parse as page number
                                    try {
                                        int page = Integer.parseInt(arg);
                                        if (page < 1) {
                                            ChatUtils.sendMessage("§cPage number must be at least 1.", true);
                                        } else {
                                            CollectionList.sendCollectionList(page);
                                        }
                                    } catch (NumberFormatException e) {
                                        // Not a number, treat as category
                                        Integer page = CollectionList.getPageForCategory(arg);

                                        if (page == null) {
                                            ChatUtils.sendMessage("§cUnknown category.", true);
                                        } else {
                                            CollectionList.sendCollectionList(page);
                                        }
                                    }
                                    return 1;
                                })
                        )
                )
                // sct track <collection>
                .then(ClientCommands.literal("track")
                        .executes(_ -> {
                            ChatUtils.sendMessage("Usage: /sct track <collection>",true);
                            return 1;
                        })
                        .then(ClientCommands.argument("collection", StringArgumentType.greedyString())
                                .suggests(COLLECTION_SUGGESTIONS)
                                .executes(context -> {
                                    String input = StringArgumentType.getString(context, "collection").trim();
                                    List<String> collections = CollectionsManager.getAllCollections();
                                    List<String> foundCollections = new ArrayList<>();

                                    String remaining = input;
                                    while (!remaining.isEmpty()) {
                                        boolean found = false;
                                        List<String> sortedCollections = collections.stream()
                                                .sorted((a, b) -> Integer.compare(b.length(), a.length()))
                                                .toList();

                                        for (String coll : sortedCollections) {
                                            if (remaining.toLowerCase().startsWith(coll.toLowerCase())) {
                                                foundCollections.add(coll);
                                                remaining = remaining.substring(coll.length()).trim();
                                                found = true;
                                                break;
                                            }
                                        }

                                        if (!found) {
                                            // skip
                                            int nextSpace = remaining.indexOf(' ');
                                            if (nextSpace == -1) break;
                                            remaining = remaining.substring(nextSpace).trim();
                                        }
                                    }

                                    if (foundCollections.size() > 1 || (foundCollections.size() == 1 && foundCollections.getFirst().equalsIgnoreCase("gemstone"))) {
                                        CollectionTracker.startMultiTracking(foundCollections);
                                    } else if (foundCollections.size() == 1) {
                                        CollectionTracker.startTracking(foundCollections.getFirst());
                                    } else {
                                        CollectionTracker.startTracking(input);
                                    }
                                    return 1;
                                })
                        )
                )
                // sct stop
                .then(ClientCommands.literal("stop")
                        .executes(_ -> {
                            if (MultiTrackingHandler.isMultiTracking()) {
                                MultiTrackingHandler.stopMultiTrackingManual();
                            } else {
                                TrackingHandler.stopTrackingManual();
                            }
                            return 1;
                        })
                )
                // sct pause
                .then(ClientCommands.literal("pause")
                        .executes(_ -> {
                            if (MultiTrackingHandler.isMultiTracking()) {
                                MultiTrackingHandler.pauseMultiTracking();
                            } else {
                                TrackingHandler.pauseTracking();
                            }
                            return 1;
                        })
                )
                // sct resume
                .then(ClientCommands.literal("resume")
                        .executes(_ -> {
                            if (MultiTrackingHandler.isMultiPaused()) {
                                MultiTrackingHandler.resumeMultiTracking();
                            } else {
                                TrackingHandler.resumeTracking();
                            }
                            return 1;
                        })
                )
                // sct restart
                .then(ClientCommands.literal("restart")
                        .executes(_ -> {
                            if (MultiTrackingHandler.isMultiTracking() || MultiTrackingHandler.isMultiPaused()) {
                                MultiTrackingHandler.restartMultiTracking();
                            } else {
                                TrackingHandler.restartTracking();
                            }
                            return 1;
                        })
                )
                // sct skill -> skill tracking commands
                .then(ClientCommands.literal("skill")
                        // sct skill track <skillName>
                        .then(ClientCommands.literal("track")
                                .executes(_ -> {
                                    ChatUtils.sendMessage("Usage: /sct skill track <skill>",true);
                                    return 1;
                                })
                                .then(ClientCommands.argument("skillName", StringArgumentType.greedyString())
                                        .suggests(SKILL_LIST)
                                        .executes(context -> {
                                            SkillTracker.startTracking(StringArgumentType.getString(context, "skillName").trim());
                                            return 1;
                                        })
                                )
                        )
                        // sct skill stop
                        .then(ClientCommands.literal("stop")
                                .executes(_ -> {
                                    SkillTrackingHandler.stopTrackingManual();
                                    return 1;
                                })
                        )
                        // sct skill pause
                        .then(ClientCommands.literal("pause")
                                .executes(_ -> {
                                    SkillTrackingHandler.pauseTracking();
                                    return 1;
                                })
                        )
                        // sct skill resume
                        .then(ClientCommands.literal("resume")
                                .executes(_ -> {
                                    SkillTrackingHandler.resumeTracking();
                                    return 1;
                                })
                        )
                        // sct skill restart
                        .then(ClientCommands.literal("restart")
                                .executes(_ -> {
                                    SkillTrackingHandler.restartTracking();
                                    return 1;
                                })
                        )
                )
                // sct cw -> shows player's coleweight
                .then(ClientCommands.literal("cw")
                        // sct cw -> shows player's coleweight
                        .executes(_ -> {
                            ColeweightUtils.getColeweight(PlayerData.INSTANCE.getPlayerName(), false);
                            return 1;
                        })
                        // sct cw find <player> -> shows specified player(or local player)'s coleweight
                        .then(ClientCommands.literal("find")
                                .executes(_ -> {
                                    ColeweightUtils.getColeweight(PlayerData.INSTANCE.getPlayerName(), false);
                                    return 1;
                                })
                                .then(ClientCommands.argument("player", StringArgumentType.string())
                                        .suggests(PLAYER_SUGGESTIONS)
                                        .executes(context -> {
                                            String playerName = StringArgumentType.getString(context, "player").trim();
                                            ColeweightUtils.getColeweight(playerName, false);
                                            return 1;
                                        })
                                )
                        )
                        .then(ClientCommands.literal("detailed")
                                .executes(_ -> {
                                    String playerName = PlayerData.INSTANCE.getPlayerName();
                                    ColeweightUtils.getColeweightDetailed(playerName);
                                    return 1;
                                })
                                .then(ClientCommands.argument("player", StringArgumentType.string())
                                        .suggests(PLAYER_SUGGESTIONS)
                                        .executes(context -> {
                                            String playerName = StringArgumentType.getString(context, "player").trim();
                                            ColeweightUtils.getColeweightDetailed(playerName);
                                            return 1;
                                        })
                                )
                        )
                        // sct cw lb <length>
                        .then(ClientCommands.literal("lb")
                                .executes(_ -> {
                                    ChatUtils.sendMessage("Usage: /sct cw lb <length>.",true);
                                    return 1;
                                })
                                .then(ClientCommands.argument("position", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            int position = IntegerArgumentType.getInteger(context, "position");
                                            ColeweightUtils.getColeweightLeaderboard(position);
                                            return 1;
                                        })
                                )
                        )
                        // sct cw color set <ign> <color>
                        .then(ClientCommands.literal("color")
                                .executes(_ -> {
                                    ChatUtils.sendMessage("Usage: /sct cw color set <player name> <hex color>.",true);
                                    return 1;
                                })
                                .then(ClientCommands.literal("set")
                                        .then(ClientCommands.argument("target", StringArgumentType.string())
                                                .suggests(((context, builder) -> {
                                                    String remaining = builder.getRemaining().toLowerCase();
                                                    if ("global".startsWith(remaining)) builder.suggest("global");
                                                    for (String playerName : context.getSource().getOnlinePlayerNames()) {
                                                        if (playerName.toLowerCase().startsWith(remaining)) builder.suggest(playerName);
                                                    }
                                                    return builder.buildFuture();
                                                }))
                                                .then(ClientCommands.argument("hex color", StringArgumentType.greedyString())
                                                        .executes(context -> {
                                                            String target = StringArgumentType.getString(context, "target").trim();
                                                            String color = StringArgumentType.getString(context, "hex color").trim();
                                                            String formattedHex = color.startsWith("#") ? color : "#" + color;

                                                            if (!formattedHex.matches("^#?[0-9a-fA-F]{6}$")) {
                                                                ChatUtils.sendMessage("§cInvalid color format.", true);
                                                                return 1;
                                                            }

                                                            if (target.equalsIgnoreCase("global")) {
                                                                ColeweightUtils.setGlobalColor(formattedHex);
                                                                ChatUtils.sendMessage("§aGlobal coleweight color set to " + formattedHex, true);
                                                            } else {
                                                                ColeweightUtils.setPlayerCustomColor(target, formattedHex);
                                                                ChatUtils.sendMessage("§aColeweight color for " + target + " set to " + formattedHex, true);
                                                            }
                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                                .then(ClientCommands.literal("remove")
                                        .executes(_ -> {
                                            ChatUtils.sendMessage("Usage: /sct cw color remove <player name>.",true);
                                            return 1;
                                        })
                                        .then(ClientCommands.argument("player name", StringArgumentType.string())
                                                .suggests(PLAYER_SUGGESTIONS)
                                                .executes(context -> {
                                                    String name = StringArgumentType.getString(context, "player name").trim();
                                                    ColeweightUtils.removePlayerCustomColor(name);
                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(ClientCommands.literal("track")
                                .executes(_ -> {
                                    ColeweightTrackingHandler.startTracking();
                                    return 1;
                                })
                        )
                        .then(ClientCommands.literal("stop")
                                .executes(_ -> {
                                    ColeweightTrackingHandler.stopTrackingManual();
                                    return 1;
                                })
                        )
                        .then(ClientCommands.literal("pause")
                                .executes(_ -> {
                                    ColeweightTrackingHandler.pauseTracking();
                                    return 1;
                                })
                        ).then(ClientCommands.literal("resume")
                                .executes(_ -> {
                                    ColeweightTrackingHandler.resumeTracking();
                                    return 1;
                                })
                        )
                        .then(ClientCommands.literal("restart")
                                .executes(_ -> {
                                    ColeweightTrackingHandler.restartTracking();
                                    return 1;
                                })
                        )
                )
                .then(ClientCommands.literal("fw")
                        .executes(_ -> {
                            FarmingweightUtils.getFarmingweight(PlayerData.INSTANCE.getPlayerName());
                            return 1;
                        })
                        .then(ClientCommands.literal("find")
                                .executes(_ -> {
                                    FarmingweightUtils.getFarmingweight(PlayerData.INSTANCE.getPlayerName());
                                    return 1;
                                })
                                .then(ClientCommands.argument("player", StringArgumentType.string())
                                        .suggests(PLAYER_SUGGESTIONS)
                                        .executes(context -> {
                                            String playerName = StringArgumentType.getString(context, "player").trim();
                                            FarmingweightUtils.getFarmingweight(playerName);
                                            return 1;
                                        })
                                )
                        )
                        .then(ClientCommands.literal("lb")
                                .executes(_ -> {
                                    ChatUtils.sendMessage("Usage: /sct fw lb <length>.", true);
                                    return 1;
                                })
                                .then(ClientCommands.argument("position", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            int position = IntegerArgumentType.getInteger(context, "position");
                                            FarmingweightUtils.getFarmingweightLeaderboard(position);
                                            return 1;
                                        })
                                )
                        )
                        // sct fw color set <ign> <color>
                        .then(ClientCommands.literal("color")
                                .executes(_ -> {
                                    ChatUtils.sendMessage("Usage: /sct fw color set <player name> <hex color>.", true);
                                    return 1;
                                })
                                .then(ClientCommands.literal("set")
                                        .then(ClientCommands.argument("target", StringArgumentType.string())
                                                .suggests(((context, builder) -> {
                                                    String remaining = builder.getRemaining().toLowerCase();
                                                    if ("global".startsWith(remaining)) {
                                                        builder.suggest("global");
                                                    }
                                                    for (String playerName : context.getSource().getOnlinePlayerNames()) {
                                                        if (playerName.toLowerCase().startsWith(remaining)) {
                                                            builder.suggest(playerName);
                                                        }
                                                    }
                                                    return builder.buildFuture();
                                                }))
                                                .then(ClientCommands.argument("hex color", StringArgumentType.greedyString())
                                                        .executes(context -> {
                                                            String target = StringArgumentType.getString(context, "target").trim();
                                                            String color = StringArgumentType.getString(context, "hex color").trim();
                                                            String formattedHex = color.startsWith("#") ? color : "#" + color;

                                                            if (!formattedHex.matches("^#?[0-9a-fA-F]{6}$")) {
                                                                ChatUtils.sendMessage("§cInvalid color format. Use hex format like #RRGGBB.", true);
                                                                return 1;
                                                            }

                                                            // 3. Handle the logic internally based on the "target" string
                                                            if (target.equalsIgnoreCase("global")) {
                                                                FarmingweightUtils.setGlobalColor(formattedHex);
                                                                ChatUtils.sendMessage("§aGlobal farmingweight color set to " + formattedHex, true);
                                                            } else {
                                                                FarmingweightUtils.setPlayerCustomColor(target, formattedHex);
                                                                ChatUtils.sendMessage("§aFarmingweight color for " + target + " set to " + formattedHex, true);
                                                            }
                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                                .then(ClientCommands.literal("remove")
                                        .executes(_ -> {
                                            ChatUtils.sendMessage("Usage: /sct fw color remove <player name>.", true);
                                            return 1;
                                        })
                                        .then(ClientCommands.argument("player name", StringArgumentType.string())
                                                .suggests(PLAYER_SUGGESTIONS)
                                                .executes(context -> {
                                                    String name = StringArgumentType.getString(context, "player name").trim();
                                                    FarmingweightUtils.removePlayerCustomColor(name);
                                                    return 1;
                                                })
                                        )
                                )
                        )
                )
                // sct changelog -> opens the changelog GUI
                .then(ClientCommands.literal("changelog")
                        .executes(_ -> {
                            Minecraft.getInstance().execute(GuiManager::openChangelog);
                            return 1;
                        })
                )
                // sct timer -> timer commands
                .then(ClientCommands.literal("timer")
                        .then(ClientCommands.literal("set")
                                .then(ClientCommands.argument("time", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            String time =StringArgumentType.getString(context, "time");
                                            int seconds = parseToSeconds(time);

                                            if (seconds < 0) {
                                                ChatUtils.sendMessage("§cInvalid time format. Use formats like '1h30m', '45s', or '90'.", true);
                                                return 1;
                                            }

                                            TimerOverlay timer = OverlayManager.getTimerOverlay();
                                            if (timer != null) {
                                                timer.setTimer(seconds);
                                            }
                                            return 1;
                                        })
                                )
                        )
                        .then(ClientCommands.literal("pause")
                                .executes(_ -> {
                                    TimerOverlay timer = OverlayManager.getTimerOverlay();
                                    assert timer != null;
                                    timer.pauseTimer();
                                    return 1;
                                })
                        )
                        .then(ClientCommands.literal("resume")
                                .executes(_ -> {
                                    TimerOverlay timer = OverlayManager.getTimerOverlay();
                                    assert timer != null;
                                    timer.pauseTimer();
                                    return 1;
                                })
                        )
                        .then(ClientCommands.literal("stop")
                                .executes(_ -> {
                                    TimerOverlay timer = OverlayManager.getTimerOverlay();
                                    assert timer != null;
                                    timer.setTimer(0);
                                    return 1;
                                })
                        )
                )
                // sct stopwatch -> stopwatch commands
                .then(ClientCommands.literal("stopwatch")
                        .then(ClientCommands.literal("start")
                                .executes(_ -> {
                                    StopwatchOverlay stopwatch = OverlayManager.getStopwatchOverlay();
                                    assert stopwatch != null;
                                    stopwatch.startStopwatch();
                                    return 1;
                                })
                        )
                        .then(ClientCommands.literal("pause")
                                .executes(_ -> {
                                    StopwatchOverlay stopwatch = OverlayManager.getStopwatchOverlay();
                                    assert stopwatch != null;
                                    stopwatch.pauseStopwatch();
                                    return 1;
                                })
                        )
                        .then(ClientCommands.literal("resume")
                                .executes(_ -> {
                                    StopwatchOverlay stopwatch = OverlayManager.getStopwatchOverlay();
                                    assert stopwatch != null;
                                    stopwatch.pauseStopwatch();
                                    return 1;
                                })
                        )
                        .then(ClientCommands.literal("stop")
                                .executes(_ -> {
                                    StopwatchOverlay stopwatch = OverlayManager.getStopwatchOverlay();
                                    assert stopwatch != null;
                                    stopwatch.stopStopwatch();
                                    return 1;
                                })
                        )
                )

                // sct setCustomGoalPosition -> set custom position goal
                .then(ClientCommands.literal("setCustomGoalPosition")
                        .executes(_ -> {
                            ChatUtils.sendMessage("§cUsage: /sct setCustomGoalPosition <collection/skill name> <position>", true);
                            return 1;
                        })
                        .then(ClientCommands.argument("goal", StringArgumentType.greedyString())
                                .suggests(CUSTOM_GOAL_POSITION_SUGGESTIONS)
                                .executes(context -> {
                                    String input = StringArgumentType.getString(context, "goal").trim();

                                    int lastSpace = input.lastIndexOf(' ');
                                    if (lastSpace == -1) {
                                        ChatUtils.sendMessage("§cUsage: /sct setCustomGoalPosition <collection/skill name> <position>", true);
                                        return 1;
                                    }

                                    String name = input.substring(0, lastSpace).trim();
                                    String positionStr = input.substring(lastSpace + 1).trim();

                                    int position;
                                    try {
                                        position = Integer.parseInt(positionStr);
                                    } catch (NumberFormatException e) {
                                        ChatUtils.sendMessage("§cInvalid position!", true);
                                        return 1;
                                    }

                                    if (position < 1) {
                                        ChatUtils.sendMessage("§cPosition must be at least 1!", true);
                                        return 1;
                                    }

                                    ConfigHelper.setCustomGoalType(LeaderboardConfig.CustomGoalType.POSITION);
                                    ConfigHelper.setCustomGoal(name, position, null);

                                    ChatUtils.sendMessage("§aCustom goal set for " + name + " at position " + position, true);
                                    return 1;
                                })
                        )
                )
                // sct setCustomGoalAmount -> set custom amount goal
                .then(ClientCommands.literal("setCustomGoalAmount")
                        .executes(_ -> {
                            ChatUtils.sendMessage("§cUsage: /sct setCustomGoalAmount <collection/skill name> <amount>", true);
                            return 1;
                        })
                        .then(ClientCommands.argument("goal", StringArgumentType.greedyString())
                                .suggests(CUSTOM_GOAL_AMOUNT_SUGGESTIONS)
                                .executes(context -> {
                                    String input = StringArgumentType.getString(context, "goal").trim();

                                    int lastSpace = input.lastIndexOf(' ');
                                    if (lastSpace == -1) {
                                        ChatUtils.sendMessage("§cUsage: /sct setCustomGoalAmount <collection/skill name> <amount>", true);
                                        return 1;
                                    }

                                    String name = input.substring(0, lastSpace).trim();
                                    String amountStr = input.substring(lastSpace + 1).trim();

                                    long amount = parseAmount(amountStr);

                                    if (amount < 0) {
                                        ChatUtils.sendMessage("§cInvalid value!", true);
                                        return 1;
                                    }

                                    ConfigHelper.setCustomGoalType(LeaderboardConfig.CustomGoalType.AMOUNT);
                                    ConfigHelper.setCustomGoal(name, null, amount);

                                    ChatUtils.sendMessage("§aCustom goal set for " + name + " at amount " + amountStr, true);
                                    return 1;
                                })
                        )
                )
                // sct commissions reset -> resets commissions tracker
                .then(ClientCommands.literal("commissions")
                        .then(ClientCommands.literal("reset")
                                .executes(_ -> {
                                    CommissionsTracker.INSTANCE.reset();
                                    ChatUtils.sendMessage("§aCommissions tracker has been reset.", true);
                                    return 1;
                                })
                        )
                )
        ));
    }

    private static final SuggestionProvider<FabricClientCommandSource> COLLECTION_SUGGESTIONS = (_, builder) -> {
        String arg = builder.getRemaining().toLowerCase();
        String prefix;
        String lastWord;

        if (arg.isEmpty()) {
            // no input, suggest collections
            prefix = "";
            lastWord = ""; // reminder that empty string is prefix for any string
        } else if (Character.isWhitespace(arg.charAt(arg.length() - 1))){
            // new collection, suggest collections and keep previous collections
            prefix = arg;
            lastWord = "";
        } else {
            int lastSpace = Math.max(arg.lastIndexOf(' '), arg.lastIndexOf('\t'));
            if (lastSpace == -1) { // no space = first collection
                prefix = "";
                lastWord = arg;
            } else { // more than one collection, divide by last space position to suggest next collection
                prefix = arg.substring(0, lastSpace + 1);
                lastWord = arg.substring(lastSpace + 1);
            }
        }

        for (String c : CollectionsManager.getAllCollections()) {
            boolean matches = Arrays.stream(c.toLowerCase().split("\\s+")).anyMatch(word -> word.startsWith(lastWord));

            if (matches) {
                builder.suggest(prefix + c);
            }
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<FabricClientCommandSource> CATEGORY_SUGGESTIONS = (_, builder) -> {
        String arg = builder.getRemaining().toLowerCase();
        for (String category : CollectionsManager.collections.keySet()) {
            if (category.toLowerCase().startsWith(arg)) {
                builder.suggest(category);
            }
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<FabricClientCommandSource> SKILL_LIST = (_, builder) -> {
        String arg = builder.getRemaining().toLowerCase();
        for (String skill : SkillUtils.getDisplayNames()) {
            if (skill.toLowerCase().startsWith(arg)) {
                builder.suggest(skill);
            }
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<FabricClientCommandSource> COLLECTION_AND_SKILL_SUGGESTIONS = (context, builder) -> {
        Suggestions collectionSuggestions = COLLECTION_SUGGESTIONS.getSuggestions(context, builder.createOffset(builder.getStart())).join();
        Suggestions skillSuggestions = SKILL_LIST.getSuggestions(context, builder.createOffset(builder.getStart())).join();

        for (Suggestion suggestion : collectionSuggestions.getList()) {
            builder.suggest(suggestion.getText());
        }
        for (Suggestion suggestion : skillSuggestions.getList()) {
            builder.suggest(suggestion.getText());
        }

        return builder.buildFuture();
    };

    private static final SuggestionProvider<FabricClientCommandSource> CUSTOM_GOAL_POSITION_SUGGESTIONS = (context, builder) -> {
        String remaining = builder.getRemaining();
        String completedName = getCompletedGoalName(remaining);

        if (completedName != null) {
            return builder.buildFuture();
        }

        return COLLECTION_AND_SKILL_SUGGESTIONS.getSuggestions(context, builder);
    };

    private static final SuggestionProvider<FabricClientCommandSource> CUSTOM_GOAL_AMOUNT_SUGGESTIONS = (context, builder) -> {
        String remaining = builder.getRemaining();
        String completedName = getCompletedGoalName(remaining);

        if (completedName != null) {
            return builder.buildFuture();
        }

        return COLLECTION_AND_SKILL_SUGGESTIONS.getSuggestions(context, builder);
    };

    private static final SuggestionProvider<FabricClientCommandSource> PLAYER_SUGGESTIONS = (context, builder) -> {
        String remaining = builder.getRemaining().toLowerCase();
        for (String playerName : context.getSource().getOnlinePlayerNames()) {
            if (playerName.toLowerCase().startsWith(remaining)) {
                builder.suggest(playerName);
            }
        }
        return builder.buildFuture();
    };

    private static List<String> getAllCollectionAndSkillNames() {
        List<String> names = new ArrayList<>();

        names.addAll(CollectionsManager.getAllCollections());
        names.addAll(SkillUtils.getDisplayNames());

        return names.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    private static String getCompletedGoalName(String input) {
        String lowerInput = input.toLowerCase(Locale.ROOT);

        return getAllCollectionAndSkillNames().stream()
                .filter(name -> lowerInput.equals(name.toLowerCase(Locale.ROOT) + " ") || lowerInput.startsWith(name.toLowerCase(Locale.ROOT) + " "))
                .max(Comparator.comparingInt(String::length))
                .orElse(null);
    }

    private static int parseToSeconds(String input) {
        Pattern pattern = Pattern.compile("(\\d+)([hms])");
        Matcher matcher = pattern.matcher(input.toLowerCase().replace(" ", ""));

        int seconds = 0;
        boolean found = false;

        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            char unit = matcher.group(2).charAt(0);
            switch (unit) {
                case 'h' -> seconds += value * 3600;
                case 'm' -> seconds += value * 60;
                case 's' -> seconds += value;
            }
            found = true;
        }

        if (!found) {
            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                return -1; // invalid input
            }
        }
        return seconds;
    }

    private static long parseAmount(String input) {
        input = input.toLowerCase().trim();

        if (input.matches("\\d+[kmb]")) {
            long number = Long.parseLong(input.replaceAll("[^0-9]", ""));
            char suffix = input.charAt(input.length() - 1);

            return switch (suffix) {
                case 'k' -> number * 1_000L;
                case 'm' -> number * 1_000_000L;
                case 'b' -> number * 1_000_000_000L;
                default -> -1;
            };
        }

        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}