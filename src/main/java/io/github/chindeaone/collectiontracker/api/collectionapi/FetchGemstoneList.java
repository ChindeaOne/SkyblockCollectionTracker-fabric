package io.github.chindeaone.collectiontracker.api.collectionapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.chindeaone.collectiontracker.api.URLManager;
import io.github.chindeaone.collectiontracker.collections.GemstonesManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FetchGemstoneList {

    private static final Logger logger = LogManager.getLogger(FetchGemstoneList.class);
    public static boolean hasGemstoneList = false;

    public static void fetchGemstoneList() {
        try {
            URI uri = URI.create(URLManager.AVAILABLE_GEMSTONES_URL);
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

                JsonObject jsonObject = JsonParser.parseString(content.toString()).getAsJsonObject();

                List<String> keys = new ArrayList<>();
                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    keys.add(entry.getKey());
                }
                GemstonesManager.gemstones = keys.toArray(new String[0]);
                hasGemstoneList = true;
            }
            logger.info("[SCT]: Successfully received the gemstone list.");
        } catch (Exception e) {
            logger.error("[SCT]: Error while receiving the gemstone list", e);
        }
    }

    private static @NotNull HttpURLConnection getHttpURLConnection(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", URLManager.AGENT);
        conn.setConnectTimeout(5000); // 5 seconds
        conn.setReadTimeout(5000); // 5 seconds
        return conn;
    }
}
