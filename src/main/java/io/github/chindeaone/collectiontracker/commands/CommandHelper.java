package io.github.chindeaone.collectiontracker.commands;

import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class CommandHelper {

    private record CommandEntry(String command, String description) {}

    private record CommandPage(String category, String color, List<CommandEntry> entries) {}

    private static final List<CommandPage> PAGES = List.of(
            new CommandPage("General Use", "§e", List.of(
                    new CommandEntry("/sct", "§eOpens the GUI."),
                    new CommandEntry("/sct edit", "§eOpens the position editor."),
                    new CommandEntry("/sct edit title", "§eOpens the title position editor."),
                    new CommandEntry("/sct commands", "§eShows this message."),
                    new CommandEntry("/sct commands <page>", "§eJumps to a specific commands page."),
                    new CommandEntry("/sct changelog", "§eShows all recent changes to the mod.")
            )),
            new CommandPage("Collection Tracking", "§3", List.of(
                    new CommandEntry("/sct collections", "§eShows all available collections (page 1)."),
                    new CommandEntry("/sct collections <page | category>", "§eJumps to a specific collections page or category."),
                    new CommandEntry("/sct track <collection 1> <collection 2> ...", "§eTracks one or more collections at once."),
                    new CommandEntry("/sct stop", "§eStops collection tracking."),
                    new CommandEntry("/sct pause", "§ePauses collection tracking."),
                    new CommandEntry("/sct resume", "§eResumes collection tracking."),
                    new CommandEntry("/sct restart", "§eRestarts collection tracking.")
            )),
            new CommandPage("Skill Tracking", "§6", List.of(
                    new CommandEntry("/sct skill track <skill>", "§eTracks your skill progress."),
                    new CommandEntry("/sct skill stop", "§eStops skill tracking."),
                    new CommandEntry("/sct skill pause", "§ePauses skill tracking."),
                    new CommandEntry("/sct skill resume", "§eResumes skill tracking."),
                    new CommandEntry("/sct skill restart", "§eRestarts skill tracking.")
            )),
            new CommandPage("Coleweight", "§b", List.of(
                    new CommandEntry("/sct cw", "§eShows your Coleweight."),
                    new CommandEntry("/sct cw find [player]", "§eShows a player's Coleweight."),
                    new CommandEntry("/sct cw detailed [player]", "§eShows detailed Coleweight information."),
                    new CommandEntry("/sct cw lb <length>", "§eShows the Coleweight leaderboard."),
                    new CommandEntry("/sct cw color set <player name> <hex color>", "§eSets a custom Coleweight rank color for a player (client side only)."),
                    new CommandEntry("/sct cw color remove <player name>", "§eRemoves a player's custom Coleweight rank color (client side only)."),
                    new CommandEntry("/sct cw color set global <hex color>", "§eSets your own global Coleweight rank color (available only for the top 10 players in Coleweight)."),
                    new CommandEntry("/sct cw track", "§eStarts Coleweight tracking."),
                    new CommandEntry("/sct cw stop", "§eStops Coleweight tracking."),
                    new CommandEntry("/sct cw pause", "§ePauses Coleweight tracking."),
                    new CommandEntry("/sct cw resume", "§eResumes Coleweight tracking."),
                    new CommandEntry("/sct cw restart", "§eRestarts Coleweight tracking.")
            )),
            new CommandPage("Farming Weight", "§a", List.of(
                    new CommandEntry("/sct fw", "§eShows your Farming Weight."),
                    new CommandEntry("/sct fw find [player]", "§eShows a player's Farming Weight."),
                    new CommandEntry("/sct fw lb <length>", "§eShows the Farming Weight leaderboard."),
                    new CommandEntry("/sct fw color set <player name> <hex color>", "§eSets a custom Farming Weight rank color for a player (client side only)."),
                    new CommandEntry("/sct fw color remove <player name>", "§eRemoves a player's custom Farming Weight rank color (client side only)."),
                    new CommandEntry("/sct fw color set global <hex color>", "§eSets your own global Farming Weight rank color (available only for the top 10 players in Farming Weight).")
            ))
    );

    public static int getTotalPages() {
        return PAGES.size();
    }

    public static void showCommands(int page) {
        int totalPages = getTotalPages();
        page = Mth.clamp(page, 1, totalPages);
        CommandPage current = PAGES.get(page - 1);
        List<MutableComponent> components = buildComponentsForPage(current);
        ChatUtils.INSTANCE.sendCommandPage(current.category(), current.color(), components, page, totalPages);
    }

    private static List<MutableComponent> buildComponentsForPage(CommandPage commandPage) {
        List<MutableComponent> components = new ArrayList<>();
        for (CommandEntry entry : commandPage.entries()) {
            MutableComponent comp = Component.literal(commandPage.color() + "◆" + entry.command());
            comp.withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(Component.literal(entry.description()))));
            components.add(comp);
        }
        return components;
    }
}
