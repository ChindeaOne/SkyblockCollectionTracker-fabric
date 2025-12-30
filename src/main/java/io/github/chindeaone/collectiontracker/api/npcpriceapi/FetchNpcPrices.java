package io.github.chindeaone.collectiontracker.api.npcpriceapi;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker;
import io.github.chindeaone.collectiontracker.api.URLManager;
import io.github.chindeaone.collectiontracker.collections.prices.NpcPrices;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;


public class FetchNpcPrices {

    private static final Logger logger = LogManager.getLogger(FetchNpcPrices.class);
    public static boolean hasNpcPrice = false;

    public static void fetchPrices() {
        try {
            URI uri = URI.create(URLManager.STATUS_URL);
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
                Map<String, Integer> prices = gson.fromJson(content.toString(), new TypeToken<Map<String, Integer>>(){}.getType());
                NpcPrices.collectionPrices.putAll(prices);

                logger.info("[SCT]: Successfully received the npc prices.");
            }
        } catch (Exception e) {
            logger.error("Error while receiving the npc prices", e);
        }
    }

    private static @NotNull HttpURLConnection getHttpURLConnection(URL url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        String gameVersion = SkyblockCollectionTracker.MC_VERSION;

        conn.setRequestProperty("X-GAME-VERSION", gameVersion);
        conn.setRequestProperty("User-Agent", URLManager.AGENT);

        conn.setConnectTimeout(5000); // 5 seconds
        conn.setReadTimeout(5000); // 5 seconds

        conn.setRequestProperty("Content-Type", "application/json");
        return conn;
    }
}
