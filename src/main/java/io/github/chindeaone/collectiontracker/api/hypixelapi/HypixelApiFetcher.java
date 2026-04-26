package io.github.chindeaone.collectiontracker.api.hypixelapi;

import io.github.chindeaone.collectiontracker.api.URLManager;
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager;
import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.commands.CollectionTracker;
import io.github.chindeaone.collectiontracker.utils.PlayerData;
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
            HttpRequest request = buildCollectionRequest(uuid, token, collection, collectionSource);
            HttpResponse<String> response =
                    HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            int status = response.statusCode();

            if (status == 401) {
                logger.warn("[SCT]: Invalid or expired token. Fetching a new one and retrying...");
                TokenManager.fetchAndStoreToken();
                token = TokenManager.getToken();

                request = buildCollectionRequest(uuid, token, collection, collectionSource);
                HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                status = response.statusCode();
            }

            if (status == 200) {
                return response.body();
            } else if (status == 404) {
                logger.warn("[SCT]: Collection API disabled for player.");
                return null;

            } else {
                logger.error("[SCT]: Failed to fetch collection data for uuid: {}", uuid);
            }

        } catch (Exception e) {
            logger.error("[SCT]: An error occurred while fetching data from the server", e);
        }
        return null;
    }

    public static String fetchMultiJsonData() {
        try {
            String collection = String.join(",", CollectionTracker.collectionList);
            String collectionSource = String.join(",", CollectionsManager.multiCollectionSource);
            HttpRequest request = buildCollectionRequest(PlayerData.INSTANCE.getPlayerUUID(), TokenManager.getToken(), collection, collectionSource);
            HttpResponse<String> response =
                    HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            int status = response.statusCode();

            if (status == 401) {
                logger.warn("[SCT]: Invalid or expired token. Fetching a new one and retrying...");
                TokenManager.fetchAndStoreToken();
                String token = TokenManager.getToken(); // get the new token

                request = buildCollectionRequest(PlayerData.INSTANCE.getPlayerUUID(), token, collection, collectionSource);
                response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                status = response.statusCode();
            }

            if (status == 200) {
                return response.body();
            } else if (status == 404) {
                logger.warn("[SCT]: Collection API disabled for player.");
                return null;
            } else {
                logger.error("[SCT]: Failed to fetch multi-collection data. Server responded with code: {}", status);
            }
        } catch (Exception e) {
            logger.error("[SCT]: An error occurred while fetching multi-collection data from the server", e);
        }
        return null;
    }

    private static HttpRequest buildCollectionRequest(String uuid, String token, String collection, String collectionSource) {
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