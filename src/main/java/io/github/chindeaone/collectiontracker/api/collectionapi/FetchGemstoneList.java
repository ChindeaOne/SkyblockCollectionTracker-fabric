package io.github.chindeaone.collectiontracker.api.collectionapi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.chindeaone.collectiontracker.api.URLManager;
import io.github.chindeaone.collectiontracker.collections.GemstonesManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static io.github.chindeaone.collectiontracker.api.URLManager.HTTP_CLIENT;

public class FetchGemstoneList {

    private static final Logger logger = LogManager.getLogger(FetchGemstoneList.class);
    public static boolean hasGemstoneList = false;

    public static void fetchGemstoneList() {
        try {
            URI uri = URI.create(URLManager.AVAILABLE_GEMSTONES_URL);

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(5))
                    .header("User-Agent", URLManager.AGENT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<InputStream> response = HTTP_CLIENT.send(
                    request,
                    HttpResponse.BodyHandlers.ofInputStream()
            );

            if (response.statusCode() == 200) {
                try (Reader reader = new InputStreamReader(response.body(), StandardCharsets.UTF_8)) {
                    JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

                    GemstonesManager.gemstones = jsonObject.keySet().toArray(new String[0]);
                    hasGemstoneList = true;
                }

                logger.info("[SCT]: Successfully received the gemstone list.");
            } else {
                logger.error("[SCT]: Failed to fetch gemstone list. HTTP {}", response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            logger.error("[SCT]: Error while receiving the gemstone list", e);
        }
    }
}
