package io.github.chindeaone.collectiontracker.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker;
import io.github.chindeaone.collectiontracker.coleweight.ColeweightUtils;
import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.gui.GuiManager;
import io.github.chindeaone.collectiontracker.gui.OverlayManager;
import io.github.chindeaone.collectiontracker.gui.overlays.TimerOverlay;
import io.github.chindeaone.collectiontracker.tracker.coleweight.ColeweightTrackingHandler;
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler;
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler;
import io.github.chindeaone.collectiontracker.utils.PlayerData;
import io.github.chindeaone.collectiontracker.utils.SkillUtils;
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler;
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class CommandRegistry {

    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal(SkyblockCollectionTracker.NAMESPACE)
                // sct -> opens the config GUI
                .executes(context -> {
                    GuiManager.INSTANCE.openConfigGui(null);
                    return 1;
                })
                // sct edit -> opens the position editor
                .then(ClientCommandManager.literal("edit")
                        .executes(context -> {
                            Minecraft.getInstance().execute(GuiManager::openGuiPositionEditor);
                            return 1;
                        })
                        .then(ClientCommandManager.literal("title")
                                .executes(context -> {
                                    Minecraft.getInstance().execute(GuiManager::openGuiTitlePositionEditor);
                                    return 1;
                                })
                        )
                )
                // sct commands -> shows the list of commands
                .then(ClientCommandManager.literal("commands")
                        // sct commands -> shows first page of commands
                        .executes(context -> {
                            CommandHelper.showCommands(1);
                            return 1;
                        })
                        // sct commands <page>
                        .then(ClientCommandManager.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    int page = IntegerArgumentType.getInteger(context, "page");
                                    CommandHelper.showCommands(page);
                                    return 1;
                                })
                        )
                )
                // sct collections -> shows the list of collections
                .then(ClientCommandManager.literal("collections")
                        // sct collections -> opens first category (page 1)
                        .executes(context -> {
                            CollectionList.sendCollectionList(1);
                            return 1;
                        })

                        // sct collections <page> or <category>
                        .then(ClientCommandManager.argument("arg", StringArgumentType.word())
                                .suggests(CATEGORY_SUGGESTIONS)
                                .executes(context -> {
                                    String arg = StringArgumentType.getString(context, "arg");
                                    // Try to parse as page number
                                    try {
                                        int page = Integer.parseInt(arg);
                                        if (page < 1) {
                                            ChatUtils.INSTANCE.sendMessage("§cPage number must be at least 1.", true);
                                        } else {
                                            CollectionList.sendCollectionList(page);
                                        }
                                    } catch (NumberFormatException e) {
                                        // Not a number, treat as category
                                        Integer page = CollectionList.getPageForCategory(arg);

                                        if (page == null) {
                                            ChatUtils.INSTANCE.sendMessage("§cUnknown category.", true);
                                        } else {
                                            CollectionList.sendCollectionList(page);
                                        }
                                    }
                                    return 1;
                                })
                        )
                )
                // sct track <collection>
                .then(ClientCommandManager.literal("track")
                        .executes(context -> {
                            ChatUtils.INSTANCE.sendMessage("Usage: /sct track <collection>",true);
                            return 1;
                        })
                        .then(ClientCommandManager.argument("collection", StringArgumentType.greedyString())
                                .suggests(COLLECTION_SUGGESTIONS)
                                .executes(context -> {
                                    CollectionTracker.startTracking(StringArgumentType.getString(context, "collection").trim());
                                    return 1;
                                })
                        )
                )
                // sct stop
                .then(ClientCommandManager.literal("stop")
                        .executes(context -> {
                            TrackingHandler.stopTrackingManual();
                            return 1;
                        })
                )
                // sct pause
                .then(ClientCommandManager.literal("pause")
                        .executes(context -> {
                            TrackingHandler.pauseTracking();
                            return 1;
                        })
                )
                // sct resume
                .then(ClientCommandManager.literal("resume")
                        .executes(context -> {
                            TrackingHandler.resumeTracking();
                            return 1;
                        })
                )
                // sct restart
                .then(ClientCommandManager.literal("restart")
                        .executes(context -> {
                            TrackingHandler.restartTracking();
                            return 1;
                        })
                )
                // sct skill -> skill tracking commands
                .then(ClientCommandManager.literal("skill")
                        // sct skill track <skillName>
                        .then(ClientCommandManager.literal("track")
                                .executes(context -> {
                                    ChatUtils.INSTANCE.sendMessage("Usage: /sct skill track <skill>",true);
                                    return 1;
                                })
                                .then(ClientCommandManager.argument("skillName", StringArgumentType.greedyString())
                                        .suggests(SKILL_LIST)
                                        .executes(context -> {
                                            SkillTracker.startTracking(StringArgumentType.getString(context, "skillName").trim());
                                            return 1;
                                        })
                                )
                        )
                        // sct skill stop
                        .then(ClientCommandManager.literal("stop")
                                .executes(context -> {
                                    SkillTrackingHandler.stopTrackingManual();
                                    return 1;
                                })
                        )
                        // sct skill pause
                        .then(ClientCommandManager.literal("pause")
                                .executes(context -> {
                                    SkillTrackingHandler.pauseTracking();
                                    return 1;
                                })
                        )
                        // sct skill resume
                        .then(ClientCommandManager.literal("resume")
                                .executes(context -> {
                                    SkillTrackingHandler.resumeTracking();
                                    return 1;
                                })
                        )
                        // sct skill restart
                        .then(ClientCommandManager.literal("restart")
                                .executes(context -> {
                                    SkillTrackingHandler.restartTracking();
                                    return 1;
                                })
                        )
                )
                // sct cw -> shows player's coleweight
                .then(ClientCommandManager.literal("cw")
                        // sct cw -> shows player's coleweight
                        .executes(context -> {
                            ColeweightUtils.getColeweight(PlayerData.INSTANCE.getPlayerName(), false);
                            return 1;
                        })
                        // sct cw find <player> -> shows specified player(or local player)'s coleweight
                        .then(ClientCommandManager.literal("find")
                                .executes(context -> {
                                    ColeweightUtils.getColeweight(PlayerData.INSTANCE.getPlayerName(), false);
                                    return 1;
                                })
                                .then(ClientCommandManager.argument("player", StringArgumentType.string())
                                        .suggests(PLAYER_SUGGESTIONS)
                                        .executes(context -> {
                                            String playerName = StringArgumentType.getString(context, "player").trim();
                                            ColeweightUtils.getColeweight(playerName, false);
                                            return 1;
                                        })
                                )
                        )
                        .then(ClientCommandManager.literal("detailed")
                                .executes(context -> {
                                    String playerName = PlayerData.INSTANCE.getPlayerName();
                                    ColeweightUtils.getColeweightDetailed(playerName);
                                    return 1;
                                })
                                .then(ClientCommandManager.argument("player", StringArgumentType.string())
                                        .suggests(PLAYER_SUGGESTIONS)
                                        .executes(context -> {
                                            String playerName = StringArgumentType.getString(context, "player").trim();
                                            ColeweightUtils.getColeweightDetailed(playerName);
                                            return 1;
                                        })
                                )
                        )
                        // sct cw lb <length>
                        .then(ClientCommandManager.literal("lb")
                                .executes(context -> {
                                    ChatUtils.INSTANCE.sendMessage("Usage: /sct cw lb <length>.",true);
                                    return 1;
                                })
                                .then(ClientCommandManager.argument("position", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            int position = IntegerArgumentType.getInteger(context, "position");
                                            ColeweightUtils.getColeweightLeaderboard(position);
                                            return 1;
                                        })
                                )
                        )
                        .then(ClientCommandManager.literal("track")
                                .executes(context -> {
                                    ColeweightTrackingHandler.startTracking();
                                    return 1;
                                })
                        )
                        .then(ClientCommandManager.literal("stop")
                                .executes(context -> {
                                    ColeweightTrackingHandler.stopTrackingManual();
                                    return 1;
                                })
                        )
                        .then(ClientCommandManager.literal("pause")
                                .executes(context -> {
                                    ColeweightTrackingHandler.pauseTracking();
                                    return 1;
                                })
                        ).then(ClientCommandManager.literal("resume")
                                .executes(context -> {
                                    ColeweightTrackingHandler.resumeTracking();
                                    return 1;
                                })
                        )
                        .then(ClientCommandManager.literal("restart")
                                .executes(context -> {
                                    ColeweightTrackingHandler.restartTracking();
                                    return 1;
                                })
                        )
                )
                .then(ClientCommandManager.literal("changelog")
                        .executes(context -> {
                            Minecraft.getInstance().execute(GuiManager::openChangelog);
                            return 1;
                        })
                )
                .then(ClientCommandManager.literal("track-multi")
                        .executes(context-> {
                            ChatUtils.INSTANCE.sendMessage("Usage: /sct multi-track <collection1> <collection2> etc.",true);
                            return 1;
                        })
                        .then(ClientCommandManager.argument("collections", StringArgumentType.greedyString())
                                .suggests(MULTI_COLLECTION_SUGGESTIONS)
                                .executes(context -> {
                                    String collectionsArgs = StringArgumentType.getString(context, "collections").trim();
                                    List<String> collections = CollectionsManager.getAllCollections();
                                    List<String> foundCollections = new ArrayList<>();

                                    String remaining = collectionsArgs;
                                    while (!remaining.isEmpty()) {
                                        boolean found = false;
                                        List<String> sortedCollections = collections.stream()
                                                .sorted((a, b) -> Integer.compare(b.length(), a.length()))
                                                .toList();

                                        for (String coll : sortedCollections) {
                                            if (remaining.startsWith(coll)) {
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

                                    CollectionTracker.startMultiTracking(foundCollections);
                                    return 1;
                                })
                        )
                )
                .then(ClientCommandManager.literal("stop-multi")
                        .executes(context -> {
                            MultiTrackingHandler.stopMultiTrackingManual();
                            return 1;
                        })
                )
                .then(ClientCommandManager.literal("pause-multi")
                        .executes(context -> {
                            MultiTrackingHandler.pauseMultiTracking();
                            return 1;
                        })
                )
                .then(ClientCommandManager.literal("resume-multi")
                        .executes(context -> {
                            MultiTrackingHandler.resumeMultiTracking();
                            return 1;
                        })
                )
                .then(ClientCommandManager.literal("restart-multi")
                        .executes(context -> {
                            MultiTrackingHandler.restartMultiTracking();
                            return 1;
                        })
                )
                .then(ClientCommandManager.literal("timer")

                        .then(ClientCommandManager.literal("set")
                                .then(ClientCommandManager.argument("time", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            int time = IntegerArgumentType.getInteger(context, "time");
                                            TimerOverlay timer = OverlayManager.getTimerOverlay();
                                            assert timer != null;
                                            timer.setTimer(time);
                                            return 1;
                                        })
                                )
                        )
                        .then(ClientCommandManager.literal("pause")
                                .executes(context -> {
                                    TimerOverlay timer = OverlayManager.getTimerOverlay();
                                    assert timer != null;
                                    timer.pauseTimer();
                                    return 1;
                                })
                        )
                        .then(ClientCommandManager.literal("resume")
                                .executes(context -> {
                                    TimerOverlay timer = OverlayManager.getTimerOverlay();
                                    assert timer != null;
                                    timer.pauseTimer();
                                    return 1;
                                })
                        )
                        .then(ClientCommandManager.literal("stop")
                                .executes(context -> {
                                    TimerOverlay timer = OverlayManager.getTimerOverlay();
                                    assert timer != null;
                                    timer.setTimer(0);
                                    return 1;
                                })
                        )
                )
        ));
    }

    private static final SuggestionProvider<FabricClientCommandSource> COLLECTION_SUGGESTIONS = (context, builder) -> {
        String arg = builder.getRemaining().toLowerCase();
        for (String c : CollectionsManager.getAllCollections()) {
            if (c.toLowerCase().startsWith(arg)) {
                builder.suggest(c);
            }
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<FabricClientCommandSource> MULTI_COLLECTION_SUGGESTIONS = (context, builder) -> {
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
            if (c.toLowerCase().startsWith(lastWord)) {
                builder.suggest(prefix + c);
            }
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<FabricClientCommandSource> CATEGORY_SUGGESTIONS = (context, builder) -> {
        String arg = builder.getRemaining().toLowerCase();
        for (String category : CollectionsManager.collections.keySet()) {
            if (category.toLowerCase().startsWith(arg)) {
                builder.suggest(category);
            }
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<FabricClientCommandSource> SKILL_LIST = (context, builder) -> {
        String arg = builder.getRemaining().toLowerCase();
        for (String skill : SkillUtils.getDisplayNames()) {
            if (skill.toLowerCase().startsWith(arg)) {
                builder.suggest(skill);
            }
        }
        return builder.buildFuture();
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
}