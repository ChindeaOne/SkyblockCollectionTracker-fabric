package io.github.chindeaone.collectiontracker.api.bazaarapi;

import io.github.chindeaone.collectiontracker.api.URLManager;
import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager;
import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.collections.GemstonesManager;
import io.github.chindeaone.collectiontracker.collections.prices.GemstonePrices;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class FetchBazaarPrice {

    private static final Logger logger = LogManager.getLogger(FetchBazaarPrice.class);

    public static void fetchData(String uuid, String token, String collection) {
        String[] types = {"normal", "enchanted"};

        if(GemstonesManager.checkIfGemstone(collection)) {
            String type = "gemstone";
            tryFetchingBazaarData(uuid, token, collection, type);
        } else {
            for (String type : types) {
                tryFetchingBazaarData(uuid, token, collection, type);
            }
        }
    }

    private static void tryFetchingBazaarData(String uuid, String token, String collection, String type) {
        try {
            Thread.sleep(300);
            URI uri = URI.create(URLManager.BAZAAR_URL);
            URL url = uri.toURL();
            HttpURLConnection conn = getHttpURLConnection(uuid, token, url, collection, type);
            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    content.append(line);
                }
                in.close();
                conn.disconnect();

                if(type.equals("gemstone")) {
                    GemstonePrices.setPrices(content.toString());
                } else {
                    BazaarCollectionsManager.setPricesAndRecipes(content.toString(), type);;
                }
                // Set collection type for future references
                CollectionsManager.collectionType = type;
                logger.info("Bazaar price found for type: {}", type);
            } else if (responseCode == 404) {
                logger.warn("Type '{}' not found for collection '{}'", type, collection);
            } else {
                logger.warn("Server returned HTTP {} for type '{}'", responseCode, type);
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Error fetching bazaar price for collection '{}': {}", collection, e.getMessage());
        }
    }

    private static @NotNull HttpURLConnection getHttpURLConnection(String uuid, String token, URL url, String collection, String type) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("X-UUID", uuid);
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("X-COLLECTION", collection);
        conn.setRequestProperty("X-TYPE", type);
        conn.setRequestProperty("User-Agent", URLManager.AGENT);
        conn.setConnectTimeout(5000); // 5 seconds
        conn.setReadTimeout(5000);    // 5 seconds
        conn.setRequestProperty("Content-Type", "application/json");
        return conn;
    }
}
