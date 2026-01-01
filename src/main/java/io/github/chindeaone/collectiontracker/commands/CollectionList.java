package io.github.chindeaone.collectiontracker.commands;

import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.util.ChatUtils;
import io.github.chindeaone.collectiontracker.util.HypixelUtils;
import net.minecraft.client.Minecraft;

import java.util.*;

public class CollectionList {

    public static void sendCollectionList() {
        System.out.println(HypixelUtils.isOnSkyblock());
        System.out.println(HypixelUtils.INSTANCE.isInHypixel());

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ChatUtils.INSTANCE.sendMessage("§aList of all collections available:", true);
        ChatUtils.INSTANCE.sendMessage("", false);

        Map<String, String> categoryColors = new HashMap<>();
        categoryColors.put("Farming", "§a");
        categoryColors.put("Mining", "§9");
        categoryColors.put("Combat", "§4");
        categoryColors.put("Foraging", "§6");
        categoryColors.put("Fishing", "§3");
        categoryColors.put("Rift", "§5");
        categoryColors.put("Sacks", "§8");

        Map<String, List<String>> categorizedCollections = new LinkedHashMap<>();

        for (Map.Entry<String, Set<String>> entry : CollectionsManager.collections.entrySet()) {
            String category = entry.getKey();
            Set<String> items = entry.getValue();

            List<String> sortedItems = new ArrayList<>(items);
            categorizedCollections.put(category, sortedItems);
        }

        for (Map.Entry<String, List<String>> entry : categorizedCollections.entrySet()) {
            String category = entry.getKey();
            String color = categoryColors.getOrDefault(category, "§f");
            sendCategoryMessage(color, category, entry.getValue());
        }
    }

    private static void sendCategoryMessage(String color, String category, List<String> collections) {
        ChatUtils.INSTANCE.sendMessage("   " + color + category + " Collections:", false);
        for (String collection : collections) {
            String message = "   " + color + "- " + collection;
//            message.setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sct track " + collection)));
            ChatUtils.INSTANCE.sendMessage(message, false);
        }
        ChatUtils.INSTANCE.sendMessage();

    }
}

