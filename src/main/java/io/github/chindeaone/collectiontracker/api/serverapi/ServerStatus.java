package io.github.chindeaone.collectiontracker.api.serverapi;

import io.github.chindeaone.collectiontracker.api.URLManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static io.github.chindeaone.collectiontracker.api.URLManager.HTTP_CLIENT;

public class ServerStatus {

    private static final Logger logger = LogManager.getLogger(ServerStatus.class);

    public static boolean checkServer() {
        try {
            URI uri = URI.create(URLManager.STATUS_URL);

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(3))
                    .header("User-Agent", URLManager.AGENT)
                    .build();

            HttpResponse<Void> response = HTTP_CLIENT.send(
                    request,
                    HttpResponse.BodyHandlers.discarding()
            );

            return response.statusCode() == 200;

        } catch (IOException | InterruptedException e) {
            logger.error("[SCT]: Error checking server status", e);
            return false;
        }
    }
}
