package io.github.chindeaone.collectiontracker.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker;
import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.gui.GuiManager;
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler;
import io.github.chindeaone.collectiontracker.util.SkillUtils;
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler;
import io.github.chindeaone.collectiontracker.util.ChatUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;

public class CommandRegistry {

    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal(SkyblockCollectionTracker.NAMESPACE)
                // /sct -> opens the config GUI
                .executes(context -> {
                    GuiManager.INSTANCE.openConfigGui(null);
                    return 1;
                })
                // /sct edit -> opens the position editor
                .then(ClientCommandManager.literal("edit")
                        .executes(context -> {
                            Minecraft.getInstance().execute(GuiManager::openGuiPositionEditor);
                            return 1;
                        })
                )
                // /sct commands -> shows the list of commands
                .then(ClientCommandManager.literal("commands")
                        .executes(context -> {
                            CommandHelper.showCommands();
                            return 1;
                        })
                )
                // /sct collections -> shows the list of collections
                .then(ClientCommandManager.literal("collections")

                        // /sct collections -> opens first category (page 1)
                        .executes(context -> {
                            CollectionList.sendCollectionList(1);
                            return 1;
                        })

                        // /sct collections <page> or <category>
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
                // /sct track <collection>
                .then(ClientCommandManager.literal("track")
                        .executes(context -> {
                            ChatUtils.INSTANCE.sendMessage("Use: /sct track <collection>",true);
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
                // /sct stop
                .then(ClientCommandManager.literal("stop")
                        .executes(context -> {
                            TrackingHandler.stopTrackingManual();
                            return 1;
                        })
                )
                // /sct pause
                .then(ClientCommandManager.literal("pause")
                        .executes(context -> {
                            TrackingHandler.pauseTracking();
                            return 1;
                        })
                )
                // /sct resume
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
                // sct skill-track
                .then(ClientCommandManager.literal("skill")
                        .then(ClientCommandManager.literal("track")
                                .executes(context -> {
                                    ChatUtils.INSTANCE.sendMessage("Use: /sct skill",true);
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
}