package io.github.chindeaone.collectiontracker.api.colors;

import com.google.gson.JsonParser;
import io.github.chindeaone.collectiontracker.api.URLManager;
import io.github.chindeaone.collectiontracker.utils.ColorUtils;
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

import static io.github.chindeaone.collectiontracker.api.URLManager.HTTP_CLIENT;

public class FetchColors {

    public static final Logger logger = LogManager.getLogger(FetchColors.class);
    public static volatile boolean hasColors = false;

    public static void fetchColorsData() {
        try {
            URI uri = URI.create(URLManager.COLORS_URL);

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
                    ColorUtils.setupColors(JsonParser.parseReader(reader).getAsJsonObject());
                    hasColors = true;
                    logger.info("[SCT]: Successfully fetched colors data.");
                }
            } else {
                logger.error("[SCT]: Failed to fetch colors data. Server responded with code: {}", status);
            }
        } catch (Exception e) {
            logger.error("[SCT]: An error occurred while fetching colors data: ", e);
        }
    }
}