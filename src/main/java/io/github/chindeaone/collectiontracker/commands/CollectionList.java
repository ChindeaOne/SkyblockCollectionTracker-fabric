package io.github.chindeaone.collectiontracker.commands;

import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.util.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

import java.util.*;

public class CollectionList {

    private static final int PAGE_SIZE = 15; // Max collections per page

    private record Page(String category, String color, List<String> collections) {}

    public static void sendCollectionList(int page) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Map<String, String> categoryColors = new HashMap<>();
        categoryColors.put("Farming", "§a");
        categoryColors.put("Mining", "§9");
        categoryColors.put("Combat", "§4");
        categoryColors.put("Foraging", "§2");
        categoryColors.put("Fishing", "§3");
        categoryColors.put("Rift", "§5");
        categoryColors.put("Sacks", "§8");

        // Ordered categories
        List<Map.Entry<String, Set<String>>> categories =
                new ArrayList<>(CollectionsManager.collections.entrySet());

        List<Page> pages = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : categories) {
            String category = entry.getKey();
            String color = categoryColors.getOrDefault(category, "§f");

            List<String> allCollections = new ArrayList<>(entry.getValue());
            if (allCollections.isEmpty()) {
                pages.add(new Page(category, color, Collections.emptyList()));
                continue;
            }

            for (int i = 0; i < allCollections.size(); i += PAGE_SIZE) {
                int end = Math.min(i + PAGE_SIZE, allCollections.size());
                List<String> sub = allCollections.subList(i, end);
                pages.add(new Page(category, color, new ArrayList<>(sub)));
            }
        }

        if (pages.isEmpty()) return;

        int totalPages = pages.size();
        page = Mth.clamp(page, 1, totalPages);

        Page current = pages.get(page - 1);

        ChatUtils.INSTANCE.sendCategoryPage(current.category, current.color, current.collections, page, totalPages);
    }
}