package io.github.chindeaone.collectiontracker.api.coleweight;

import io.github.chindeaone.collectiontracker.api.URLManager;
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager;
import io.github.chindeaone.collectiontracker.coleweight.ColeweightManager;
import io.github.chindeaone.collectiontracker.utils.PlayerData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static io.github.chindeaone.collectiontracker.api.URLManager.HTTP_CLIENT;

public class ColeweightFetcher {

    private static final Logger logger = LogManager.getLogger(ColeweightFetcher.class);
    public static boolean hasColeweightLb = false;

    public static void fetchColeweightDataAsync(String playerName, Runnable onComplete) {
        try {
            URI uri = URI.create(URLManager.COLEWEIGHT_URL);

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(java.time.Duration.ofSeconds(5))
                    .header("Authorization", "Bearer " + TokenManager.getToken())
                    .header("X-UUID", PlayerData.INSTANCE.getPlayerUUID())
                    .header("X-NAME", playerName)
                    .header("User-Agent", URLManager.AGENT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        int status = response.statusCode();
                        if (status == 200) {
                            ColeweightManager.updateColeweight(response.body());
                            logger.info("Successfully fetched Coleweight data for player: {}", playerName);
                            if (onComplete != null) {
                                try {
                                    onComplete.run();
                                } catch (Exception e) {
                                    logger.error("An error occurred while executing the onComplete callback. ", e);
                                }
                            }
                        } else {
                            logger.warn("Failed to fetch Coleweight data for player: {}. HTTP status: {}", playerName, status);
                        }
                    })
                    .exceptionally(e -> {
                        logger.error("An error occurred while fetching Coleweight data. ", e);
                        return null;
                    });
        } catch (Exception e) {
            logger.error("An error occurred while fetching Coleweight data. ", e);
        }
    }

    public static void fetchColeweightLbAsync(Runnable onComplete) {
        try {
            URI uri = URI.create(URLManager.COLEWEIGHT_URL + "/lb");

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(java.time.Duration.ofSeconds(5))
                    .header("User-Agent", URLManager.AGENT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        int status = response.statusCode();
                        if (status == 200) {
                            ColeweightManager.updateColeweightLb(response.body(), false);
                            logger.info("Successfully fetched Coleweight leaderboard data.");
                            if (onComplete != null) {
                                try {
                                    onComplete.run();
                                } catch (Exception e) {
                                    logger.error("An error occurred while executing the onComplete callback. ", e);
                                }
                            }
                        } else {
                            logger.warn("Failed to fetch Coleweight leaderboard data. HTTP status: {}", status);
                        }
                    })
                    .exceptionally(e -> {
                        logger.error("An error occurred while fetching Coleweight leaderboard data.", e);
                        return null;
                    });
        } catch (Exception e) {
            logger.error("An error occurred while fetching Coleweight leaderboard data.", e);
        }
    }

    public static void fetchColeweightLbTop1k() {
        try {
            URI uri = URI.create(URLManager.COLEWEIGHT_URL + "/top1k");

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(java.time.Duration.ofSeconds(5))
                    .header("User-Agent", URLManager.AGENT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();
            if (status == 200) {
                ColeweightManager.updateColeweightLb(response.body(), true);
                hasColeweightLb = true;
                logger.info("[SCT] Successfully fetched Coleweight leaderboard for top 1k players.");
            } else {
                logger.warn("Failed to fetch Coleweight leaderboard for top 1k players. HTTP status: {}", status);
            }
        } catch (Exception e) {
            logger.error("An error occurred while fetching Coleweight leaderboard data.", e);
        }
    }
}