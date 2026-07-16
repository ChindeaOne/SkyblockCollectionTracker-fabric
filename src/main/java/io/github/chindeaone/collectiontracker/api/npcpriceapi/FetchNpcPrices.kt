package io.github.chindeaone.collectiontracker.api.npcpriceapi;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.chindeaone.collectiontracker.api.URLManager;
import io.github.chindeaone.collectiontracker.collections.prices.NpcPrices;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import static io.github.chindeaone.collectiontracker.api.URLManager.HTTP_CLIENT;

public class FetchNpcPrices {

    private static final Logger logger = LogManager.getLogger(FetchNpcPrices.class);
    public static volatile boolean hasNpcPrice = false;

    public static void fetchPrices() {
        try {
            URI uri = URI.create(URLManager.NPC_PRICES_URL);

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(5))
                    .header("User-Agent", URLManager.AGENT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<InputStream> response =
                    HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

            int status = response.statusCode();
            if (status == 200) {
                try (Reader reader = new InputStreamReader(response.body(), StandardCharsets.UTF_8)) {

                    Gson gson = new Gson();
                    Map<String, Integer> prices = gson.fromJson(
                            reader,
                            new TypeToken<Map<String, Integer>>() {}.getType()
                    );

                    NpcPrices.collectionPrices.putAll(prices);
                    hasNpcPrice = true;
                    logger.info("[SCT]: Successfully received the npc prices.");
                }
            } else {
                logger.error("[SCT]: Failed to fetch NPC prices. HTTP {}", status);
            }

        } catch (Exception e) {
            logger.error("[SCT]: Error while receiving the npc prices", e);
        }
    }
}