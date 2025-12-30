package io.github.chindeaone.collectiontracker.commands;

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker;
import io.github.chindeaone.collectiontracker.gui.GuiManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class CommandRegistry {

    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
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
            );
        });
    }
}
