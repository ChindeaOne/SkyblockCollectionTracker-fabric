package io.github.chindeaone.collectiontracker.api.hypixelapi;

import io.github.chindeaone.collectiontracker.api.URLManager;
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static io.github.chindeaone.collectiontracker.api.URLManager.HTTP_CLIENT;
import static io.github.chindeaone.collectiontracker.collections.CollectionsManager.collectionSource;

public class HypixelApiFetcher {

    private static final Logger logger = LogManager.getLogger(HypixelApiFetcher.class);

    public static String fetchJsonData(String uuid, String token, String collection) {
        try {
            HttpRequest request = buildCollectionRequest(uuid, token, collection);
            HttpResponse<InputStream> response =
                    HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

            int status = response.statusCode();
            if (status == 200) {
                try (Reader reader = new InputStreamReader(response.body(), StandardCharsets.UTF_8)) {
                    StringBuilder content = new StringBuilder();
                    char[] buffer = new char[4096];
                    int n;
                    while ((n = reader.read(buffer)) != -1) {
                        content.append(buffer, 0, n);
                    }
                    return content.toString();
                }
            } else {
                logger.error("[SCT]: Failed to fetch data. Server responded with code: {}", status);
                TrackingHandler.stopTracking();
            }

        } catch (Exception e) {
            logger.error("[SCT]: An error occurred while fetching data from the server", e);
        }
        return null;
    }

    private static HttpRequest buildCollectionRequest(String uuid, String token, String collection) {
        return HttpRequest.newBuilder(URI.create(URLManager.TRACKED_COLLECTION_URL))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + token)
                .header("X-UUID", uuid)
                .header("X-COLLECTION", collection)
                .header("X-SOURCE", collectionSource)
                .header("User-Agent", URLManager.AGENT)
                .header("Accept", "application/json")
                .GET()
                .build();
    }
}