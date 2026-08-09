package io.github.chindeaone.collectiontracker.commands;

import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.utils.Colors;
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class CollectionList {

    private static final int PAGE_SIZE = 15; // Max collections per page

    private record Page(String category, int color, List<String> collections) {}

    public static void sendCollectionList(int page) {
        Map<String, Integer> categoryColors = getStringIntegerMap();

        // Ordered categories
        List<Map.Entry<String, Set<String>>> categories = new ArrayList<>(CollectionsManager.collections.entrySet());

        List<Page> pages = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : categories) {
            String category = entry.getKey();
            int color = categoryColors.get(category);

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

        ChatUtils.sendCategoryPage(current.category, current.color, current.collections, page, totalPages);
    }

    private static @NonNull Map<String, Integer> getStringIntegerMap() {
        Map<String, Integer> categoryColors = new LinkedHashMap<>();
        categoryColors.put("Farming", Colors.GREEN.getColor());
        categoryColors.put("Mining", Colors.GOLD.getColor());
        categoryColors.put("Combat", Colors.RED.getColor());
        categoryColors.put("Foraging", Colors.DARK_GREEN.getColor());
        categoryColors.put("Fishing", Colors.AQUA.getColor());
        categoryColors.put("Rift", Colors.DARK_PURPLE.getColor());
        categoryColors.put("Miscellaneous", Colors.DARK_GRAY.getColor());
        return categoryColors;
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