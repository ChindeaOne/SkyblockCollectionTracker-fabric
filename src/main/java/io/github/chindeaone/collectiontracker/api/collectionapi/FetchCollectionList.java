package io.github.chindeaone.collectiontracker.api.collectionapi;

import com.google.gson.*;
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker;
import io.github.chindeaone.collectiontracker.api.URLManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.github.chindeaone.collectiontracker.api.URLManager.HTTP_CLIENT;
import static io.github.chindeaone.collectiontracker.collections.CollectionsManager.collections;

public class FetchCollectionList {

    private static final Logger logger = LogManager.getLogger(FetchCollectionList.class);
    public static boolean hasCollectionList = false;

    public static void fetchCollectionList() {
        try {
            URI uri = URI.create(URLManager.AVAILABLE_COLLECTIONS_URL);

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(5))
                    .header("User-Agent", URLManager.AGENT)
                    .header("X-GAME-VERSION", SkyblockCollectionTracker.MC_VERSION)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<InputStream> response = HTTP_CLIENT.send(
                    request,
                    HttpResponse.BodyHandlers.ofInputStream()
            );

            if (response.statusCode() == 200) {
                try (Reader reader = new InputStreamReader(response.body(), StandardCharsets.UTF_8)) {
                    JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

                    for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                        String category = entry.getKey();
                        JsonArray itemsArray = entry.getValue().getAsJsonArray();

                        Set<String> items = new HashSet<>();
                        for (JsonElement el : itemsArray) {
                            items.add(el.getAsString());
                        }

                        collections.put(category, items);
                    }
                }

                logger.info("[SCT]: Successfully received the collection list.");
            } else {
                logger.error("[SCT]: Failed to fetch collection list. HTTP {}", response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            logger.error("[SCT]: Error while receiving the collection list", e);
        }
    }
}
