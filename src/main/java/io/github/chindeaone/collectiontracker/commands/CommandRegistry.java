package io.github.chindeaone.collectiontracker.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker;
import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.gui.GuiManager;
import io.github.chindeaone.collectiontracker.tracker.TrackingHandlerClass;
import io.github.chindeaone.collectiontracker.util.ChatUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class CommandRegistry {

    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal(SkyblockCollectionTracker.NAMESPACE)
                // /sct -> opens the config GUI
                .executes(context -> {
                    GuiManager.INSTANCE.openConfigGui(null);
                    return 1;
                })
                // /sct help -> shows the list of commands
                .then(ClientCommandManager.literal("help")
                        .executes(context -> {
                            CommandHelper.showCommands();
                            return 1;
                        })
                )
                // /sct collections -> shows the list of collections
                .then(ClientCommandManager.literal("collections")
                        .executes(context -> {
                            CollectionList.sendCollectionList();
                            return 1;
                        })
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
                                    StartTracker.startTracking(StringArgumentType.getString(context, "collection").trim());
                                    return 1;
                                })
                        )
                )
                // /sct stop
                .then(ClientCommandManager.literal("stop")
                        .executes(context -> {
                            TrackingHandlerClass.stopTrackingManual();
                            return 1;
                        })
                )
                // /sct pause
                .then(ClientCommandManager.literal("pause")
                        .executes(context -> {
                            TrackingHandlerClass.pauseTracking();
                            return 1;
                        })
                )
                // /sct resume
                .then(ClientCommandManager.literal("resume")
                        .executes(context -> {
                            TrackingHandlerClass.resumeTracking();
                            return 1;
                        })
                )
                // sct restart
                .then(ClientCommandManager.literal("restart")
                        .executes(context -> {
                            TrackingHandlerClass.restartTracking();
                            return 1;
                        })
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
}
