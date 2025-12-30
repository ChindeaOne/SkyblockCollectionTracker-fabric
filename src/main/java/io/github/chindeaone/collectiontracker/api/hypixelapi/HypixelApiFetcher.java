package io.github.chindeaone.collectiontracker.api.hypixelapi;

import io.github.chindeaone.collectiontracker.api.URLManager;
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import static io.github.chindeaone.collectiontracker.collections.CollectionsManager.collectionSource;

public class HypixelApiFetcher {

    private static final Logger logger = LogManager.getLogger(HypixelApiFetcher.class);

    public static String fetchJsonData(String uuid, String token, String collection) {
        try {
            URI uri = URI.create(URLManager.TRACKED_COLLECTION_URL);
            URL url = uri.toURL();
            HttpURLConnection conn = getHttpURLConnection(uuid, token, url, collection);

            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                in.close();
                conn.disconnect();

                return content.toString();
            } else if (responseCode == 401) {
                logger.warn("[SCT]: Invalid or expired token. Fetching a new one and retrying...");
                TokenManager.fetchAndStoreToken();

                conn = getHttpURLConnection(uuid, token, url, collection);
                responseCode = conn.getResponseCode();

                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }

                    in.close();
                    conn.disconnect();

                    return content.toString();
                } else {
                    logger.error("[SCT]: Retry failed. Server responded with code: {}", responseCode);
                }
            } else {
                logger.error("[SCT]: Failed to fetch data. Server responded with code: {}", responseCode);
            }

        } catch (Exception e) {
            logger.error("[SCT]: An error occurred while fetching data from the server: {}", e.getMessage());
        }

        return null;
    }

    private static @NotNull HttpURLConnection getHttpURLConnection(String uuid, String token, URL url, String collection) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        conn.setRequestProperty("X-UUID", uuid);
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("X-COLLECTION", collection);
        conn.setRequestProperty("X-SOURCE", collectionSource);
        conn.setRequestProperty("User-Agent", URLManager.AGENT);

        conn.setConnectTimeout(5000); // 5 seconds
        conn.setReadTimeout(5000); // 5 seconds

        conn.setRequestProperty("Content-Type", "application/json");
        return conn;
    }
}
