package io.github.chindeaone.collectiontracker.api.bazaarapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.chindeaone.collectiontracker.api.URLManager;
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager;
import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager;
import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.collections.prices.GemstonePrices;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.github.chindeaone.collectiontracker.api.URLManager.HTTP_CLIENT;

public class FetchBazaarPrice {

    private static final Logger logger = LogManager.getLogger(FetchBazaarPrice.class);

    public static void fetchData(String uuid, String token, String collection) {
        try {
            URI uri = URI.create(URLManager.BAZAAR_URL);

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(5))
                    .header("X-UUID", uuid)
                    .header("Authorization", "Bearer " + token)
                    .header("X-COLLECTION", collection)
                    .header("User-Agent", URLManager.AGENT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<InputStream> response =
                    HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

            int status = response.statusCode();
            if (status == 200) {
                try (Reader reader = new InputStreamReader(response.body(), StandardCharsets.UTF_8)) {
                    JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

                    // Map the new data structure
                    JsonObject collectionObject = jsonObject.entrySet().iterator().next().getValue().getAsJsonObject();
                    Map.Entry<String, JsonElement> entry = collectionObject.entrySet().iterator().next();

                    String data = entry.getValue().getAsJsonObject().toString();
                    String type = entry.getKey();

                    if ("gemstone".equals(type)) {
                        GemstonePrices.setPrices(data);
                    } else {
                        BazaarCollectionsManager.setPricesAndRecipes(data, type);
                    }
                    CollectionsManager.collectionType = type;
                    logger.info("[SCT]: Successfully fetched bazaar price for collection '{}'", collection);
                }
            }
        } catch (Exception e) {
            logger.error("[SCT]: Error fetching bazaar price for collection '{}': {}", collection, e.getMessage());
        }
    }

    public static void fetchData(String uuid, String token, List<String> collections) {
        try {
            URI uri = URI.create(URLManager.BAZAAR_URL);

            // For `gemstone` collection only, add all gemstones so it fetches prices for every type
            List<String> newCollections = addGemstones(collections);

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(5))
                    .header("X-UUID", uuid)
                    .header("Authorization", "Bearer " + token)
                    .header("X-COLLECTION", String.join(", ", newCollections))
                    .header("User-Agent", URLManager.AGENT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<InputStream> response =
                    HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

            int status = response.statusCode();

            if (status == 401) {
                logger.warn("[SCT]: Invalid or expired token. Fetching a new one and retrying...");
                TokenManager.fetchAndStoreToken();
                token = TokenManager.getToken();

                request = HttpRequest.newBuilder(uri)
                        .timeout(Duration.ofSeconds(5))
                        .header("X-UUID", uuid)
                        .header("Authorization", "Bearer " + token)
                        .header("X-COLLECTION", String.join(", ", newCollections))
                        .header("User-Agent", URLManager.AGENT)
                        .header("Accept", "application/json")
                        .GET()
                        .build();
                response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
                status = response.statusCode();
            }

            if (status == 200) {
                try (Reader reader = new InputStreamReader(response.body(), StandardCharsets.UTF_8)) {
                    JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

                    for (Map.Entry<String, JsonElement> collectionEntry : jsonObject.entrySet()) {
                        String collectionId = collectionEntry.getKey();
                        JsonObject typeWrapper = collectionEntry.getValue().getAsJsonObject();

                        if (typeWrapper.entrySet().iterator().hasNext()) {
                            Map.Entry<String, JsonElement> typeEntry = typeWrapper.entrySet().iterator().next();
                            String type = typeEntry.getKey();
                            String data = typeEntry.getValue().getAsJsonObject().toString();

                            if ("gemstone".equals(type)) {
                                GemstonePrices.setPrices(collectionId, data);
                            } else {
                                BazaarCollectionsManager.setPricesAndRecipes(collectionId, data, type);
                            }
                            CollectionsManager.multiCollectionTypes.put(collectionId, type);
                        }
                    }
                    logger.info("[SCT]: Successfully fetched bazaar price for collection list '{}'", collections);
                }
            }
        } catch (Exception e) {
            logger.error("[SCT]: Error fetching bazaar price for collections '{}': {}", collections, e.getMessage());
        }
    }

    private static @NotNull List<String> addGemstones(List<String> collections) {
        List<String> newCollections = new ArrayList<>(collections);
        if (newCollections.contains("gemstone")) {
            newCollections.remove("gemstone");
            newCollections.add("ruby");
            newCollections.add("sapphire");
            newCollections.add("topaz");
            newCollections.add("amethyst");
            newCollections.add("jade");
            newCollections.add("jasper");
            newCollections.add("amber");
            newCollections.add("opal");
            newCollections.add("aquamarine");
            newCollections.add("peridot");
            newCollections.add("citrine");
            newCollections.add("onyx");
        }
        return newCollections;
    }
}