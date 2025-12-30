package io.github.chindeaone.collectiontracker.api.collectionapi;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker;
import io.github.chindeaone.collectiontracker.api.URLManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.github.chindeaone.collectiontracker.collections.CollectionsManager.collections;

public class FetchCollectionList {

    private static final Logger logger = LogManager.getLogger(FetchCollectionList.class);
    public static boolean hasCollectionList = false;

    public static void fetchCollectionList() {
        try {
            URI uri = URI.create(URLManager.AVAILABLE_COLLECTIONS_URL);
            URL url = uri.toURL();
            HttpURLConnection conn = getHttpURLConnection(url);

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

                Gson gson = new Gson();
                JsonObject root = gson.fromJson(content.toString(), JsonObject.class);

                for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                    String category = entry.getKey();
                    JsonArray itemsArray = entry.getValue().getAsJsonArray();
                    Set<String> items = new HashSet<>();
                    for (int i = 0; i < itemsArray.size(); i++) {
                        items.add(itemsArray.get(i).getAsString());
                    }
                    collections.put(category, items);
                }
                logger.info("[SCT]: Successfully received the collection list.");
            }
        } catch (Exception e) {
            logger.error("[SCT]: Error while receiving the collection list", e);
        }
    }

    private static @NotNull HttpURLConnection getHttpURLConnection(URL url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        System.out.println("[SCT]: Fetching collection list from " + SkyblockCollectionTracker.MC_VERSION);
        String gameVersion = SkyblockCollectionTracker.MC_VERSION;

        conn.setRequestProperty("X-GAME-VERSION", gameVersion);
        conn.setRequestProperty("User-Agent", URLManager.AGENT);

        conn.setConnectTimeout(5000); // 5 seconds
        conn.setReadTimeout(5000); // 5 seconds

        conn.setRequestProperty("Content-Type", "application/json");
        return conn;
    }
}
