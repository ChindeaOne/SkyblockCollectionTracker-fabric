package io.github.chindeaone.collectiontracker.api.hypixelapi;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.chindeaone.collectiontracker.api.URLManager;
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager;
import io.github.chindeaone.collectiontracker.util.SkillUtils;
import io.github.chindeaone.collectiontracker.util.PlayerData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import static io.github.chindeaone.collectiontracker.api.URLManager.HTTP_CLIENT;

public class SkillApiFetcher {

    private static final Logger logger = LogManager.getLogger(SkillApiFetcher.class);

    public static void fetchSkillsData() {
        try {
            URI uri = URI.create(URLManager.SKILLS_URL);

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(5))
                    .header("X-UUID", PlayerData.INSTANCE.getPlayerUUID())
                    .header("Authorization", "Bearer " + TokenManager.getToken())
                    .header("User-Agent", URLManager.AGENT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<InputStream> response =
                    HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {
                try (Reader reader = new InputStreamReader(response.body(), StandardCharsets.UTF_8)) {
                    Gson gson = new Gson();
                    Type mapType = new TypeToken<Map<String, Double>>() {}.getType();
                    Map<String, Double> skills = gson.fromJson(reader, mapType);

                    SkillUtils.updateFromApi(skills);
                    logger.info("[SCT]: Successfully received the skill data.");
                }
            } else {
                logger.error("[SCT]: Failed to fetch skill data. HTTP {}", response.statusCode());
            }
        } catch (Exception e) {
            logger.error("[SCT]: Error while receiving the skill data", e);
        }
    }
}