package io.github.chindeaone.collectiontracker.commands;

import io.github.chindeaone.collectiontracker.util.ChatUtils;

public class CommandHelper {

    public static void showCommands() {
        ChatUtils.INSTANCE.sendMessage("                        §f<§eInfo§f>", false);
        ChatUtils.INSTANCE.sendMessage("§a◆/sct => Opens the gui.", false);
        ChatUtils.INSTANCE.sendMessage("§a◆/sct track => Tracks your Skyblock collection.", false);
        ChatUtils.INSTANCE.sendMessage("§a◆/sct stop => Stops tracking.", false);
        ChatUtils.INSTANCE.sendMessage("§a◆/sct pause => Pauses tracking.", false);
        ChatUtils.INSTANCE.sendMessage("§a◆/sct resume => Resumes tracking.", false);
        ChatUtils.INSTANCE.sendMessage("§a◆/sct collections => Shows all collections available.", false);
        ChatUtils.INSTANCE.sendMessage("§a◆/sct multitrack => Tracks multiple collections at once.", false);
    }
}
