package io.github.chindeaone.collectiontracker.commands;

import io.github.chindeaone.collectiontracker.util.ChatUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public class CommandHelper {

    public static final String title = "§6§lSkyblock Collection Tracker §7- §eCommands";
    private static final List<String> commandLines = List.of(
            "§a◆/sct",
            "§a/sct help",
            "§a◆/sct track",
            "§a◆/sct stop",
            "§a◆/sct pause",
            "§a◆/sct resume",
            "§a◆/sct restart",
            "§a◆/sct collections"
    );

    public static void showCommands() {

        List<MutableComponent> components = buildCommandComponents();
        ChatUtils.INSTANCE.sendCommands(title, components);
    }

    public static List<MutableComponent> buildCommandComponents() {
        List<String> descriptions = List.of(
                "§eOpens the gui.",
                "§eShows this message.",
                "§eTracks your Skyblock collection.",
                "§eStops tracking.",
                "§ePauses tracking.",
                "§eResumes tracking.",
                "§eRestarts tracking.",
                "§eShows all collections available."
        );

        List<MutableComponent> components = new ArrayList<>();
        for (int i = 0; i < commandLines.size(); i++) {
            String cmd = commandLines.get(i);
            String desc = i < descriptions.size() ? descriptions.get(i) : "";
            MutableComponent comp = Component.literal(cmd);
            comp.withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(Component.literal(desc))));
            components.add(comp);
        }
        return components;
    }
}
