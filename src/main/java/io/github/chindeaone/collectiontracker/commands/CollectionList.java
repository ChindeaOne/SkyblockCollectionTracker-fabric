package io.github.chindeaone.collectiontracker.commands;

import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.util.ChatUtils;
import net.minecraft.util.Mth;

import java.util.*;

public class CollectionList {

    private static final int PAGE_SIZE = 15; // Max collections per page

    private record Page(String category, String color, List<String> collections) {}

    public static void sendCollectionList(int page) {
        Map<String, String> categoryColors = new HashMap<>();
        categoryColors.put("Farming", "§a"); // Green
        categoryColors.put("Mining", "§6"); // Gold
        categoryColors.put("Combat", "§c"); // Red
        categoryColors.put("Foraging", "§2"); // Dark green
        categoryColors.put("Fishing", "§b"); // Aqua
        categoryColors.put("Rift", "§5"); // Dark purple
        categoryColors.put("Miscellaneous", "§8"); // Dark gray

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

    public static Integer getPageForCategory(String categoryInput) {
        Map<String, Set<String>> collectionsMap = CollectionsManager.collections;

        int pageIndex = 1;

        for (Map.Entry<String, Set<String>> entry : collectionsMap.entrySet()) {
            String category = entry.getKey();
            List<String> allCollections = new ArrayList<>(entry.getValue());

            int pagesForThisCategory = Math.max(1,
                    (int) Math.ceil(allCollections.size() / (double) PAGE_SIZE));

            if (category.equalsIgnoreCase(categoryInput)) {
                return pageIndex; // first page of this category
            }

            pageIndex += pagesForThisCategory;
        }

        return null;
    }
}