package io.github.chindeaone.collectiontracker.api.tokenapi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.chindeaone.collectiontracker.api.URLManager;
import io.github.chindeaone.collectiontracker.util.PlayerData;
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

public class TokenFetcher {

    private static final Logger logger = LogManager.getLogger(TokenFetcher.class);

    public String fetchToken() throws Exception {
        URI uri = URI.create(URLManager.TOKEN_URL);

        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(5))
                .header("X-UUID", PlayerData.INSTANCE.getPlayerUUID())
                .header("User-Agent", URLManager.AGENT)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<InputStream> response =
                HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

        int status = response.statusCode();
        if (status != 200) {
            logger.error("[SCT]: Failed to fetch token, response code: {}", status);
            return null;
        }

        try (Reader reader = new InputStreamReader(response.body(), StandardCharsets.UTF_8)) {
            JsonObject jsonResponse = JsonParser.parseReader(reader).getAsJsonObject();
            logger.info("[SCT]: Successfully fetched token");

            return jsonResponse.getAsJsonPrimitive("token").getAsString();
        } catch (Exception e) {
            logger.error("[SCT]: An error occurred while parsing the token response", e);
            return null;
        }
    }
}
