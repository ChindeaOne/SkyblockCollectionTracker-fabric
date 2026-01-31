package io.github.chindeaone.collectiontracker.api.bazaarapi;

import io.github.chindeaone.collectiontracker.api.URLManager;
import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager;
import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.collections.GemstonesManager;
import io.github.chindeaone.collectiontracker.collections.prices.GemstonePrices;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static io.github.chindeaone.collectiontracker.api.URLManager.HTTP_CLIENT;

public class FetchBazaarPrice {

    private static final Logger logger = LogManager.getLogger(FetchBazaarPrice.class);

    public static void fetchData(String uuid, String token, String collection) {
        String[] types = {"normal", "enchanted", "gemstone"};

        if(GemstonesManager.checkIfGemstone(collection)) {
            String type = types[2]; // gemstone
            tryFetchingBazaarData(uuid, token, collection, type);
        } else {
            for (String type : types) {
                tryFetchingBazaarData(uuid, token, collection, type);
            }
        }
    }

    private static void tryFetchingBazaarData(String uuid, String token, String collection, String type) {
        try {
            Thread.sleep(300); // delay between requests to avoid spam

            URI uri = URI.create(URLManager.BAZAAR_URL);

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(5))
                    .header("X-UUID", uuid)
                    .header("Authorization", "Bearer " + token)
                    .header("X-COLLECTION", collection)
                    .header("X-TYPE", type)
                    .header("User-Agent", URLManager.AGENT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

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

                    String json = content.toString();

                    if ("gemstone".equals(type)) {
                        GemstonePrices.setPrices(json);
                    } else {
                        BazaarCollectionsManager.setPricesAndRecipes(json, type);
                    }

                    CollectionsManager.collectionType = type;
                    logger.info("Bazaar price found for type: {}", type);
                }

            } else if (status == 404) {
                logger.warn("Type '{}' not found for collection '{}'", type, collection);
            } else {
                logger.warn("Server returned HTTP {} for type '{}'", status, type);
            }

        } catch (IOException | InterruptedException e) {
            logger.error("Error fetching bazaar price for collection '{}': {}", collection, e.getMessage());
        }
    }
}