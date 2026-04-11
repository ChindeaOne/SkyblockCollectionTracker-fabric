package io.github.chindeaone.collectiontracker.api.coleweight;

import io.github.chindeaone.collectiontracker.api.URLManager;
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager;
import io.github.chindeaone.collectiontracker.coleweight.ColeweightManager;
import io.github.chindeaone.collectiontracker.utils.ColorUtils;
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils;
import io.github.chindeaone.collectiontracker.utils.PlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static io.github.chindeaone.collectiontracker.api.URLManager.HTTP_CLIENT;

public class ColeweightFetcher {

    private static final Logger logger = LogManager.getLogger(ColeweightFetcher.class);
    public static boolean hasColeweightLb = false;
    public static boolean hasColeweightTopColors = false;

    public static void fetchColeweightDataAsync(String playerName, Runnable onComplete) {
        try {
            URI uri = URI.create(URLManager.COLEWEIGHT_URL);

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(15))
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

                        if (status == 401) {
                            logger.warn("[SCT]: Invalid or expired token. Fetching a new one and retrying...");
                            try {
                                TokenManager.fetchAndStoreToken();
                                HttpRequest retryRequest = HttpRequest.newBuilder(uri)
                                        .timeout(Duration.ofSeconds(15))
                                        .header("Authorization", "Bearer " + TokenManager.getToken())
                                        .header("X-UUID", PlayerData.INSTANCE.getPlayerUUID())
                                        .header("X-NAME", playerName)
                                        .header("User-Agent", URLManager.AGENT)
                                        .header("Accept", "application/json")
                                        .GET()
                                        .build();
                                HttpResponse<String> retryResponse = HTTP_CLIENT.send(retryRequest, HttpResponse.BodyHandlers.ofString());
                                status = retryResponse.statusCode();

                                if (status == 200) {
                                    if (retryResponse.body() == null || retryResponse.body().isEmpty()) {
                                        Minecraft.getInstance().execute(() ->
                                                ChatUtils.INSTANCE.sendMessage("§cCouldn't find " + playerName + "'s coleweight.", true)
                                        );
                                        logger.warn("[SCT]: Received empty response when fetching Coleweight data for player: {}", playerName);
                                        return;
                                    }
                                    ColeweightManager.updateColeweight(retryResponse.body());
                                    logger.info("[SCT]: Successfully fetched Coleweight data for player: {} (after token refresh)", playerName);
                                    if (onComplete != null) onComplete.run();
                                } else {
                                    Minecraft.getInstance().execute(() ->
                                            ChatUtils.INSTANCE.sendMessage("§cCouldn't find " + playerName + "'s coleweight.", true)
                                    );
                                    logger.warn("[SCT]: Failed to fetch Coleweight data for player: {} after token refresh. HTTP status: {}", playerName, status);
                                }
                            } catch (Exception e) {
                                logger.error("[SCT]: An error occurred while retrying Coleweight fetch after token refresh.", e);
                            }
                            return;
                        }

                        if (status == 200) {
                            if (response.body() == null || response.body().isEmpty()) {
                                Minecraft.getInstance().execute(() ->
                                        ChatUtils.INSTANCE.sendMessage("§cCouldn't find " + playerName + "'s coleweight.", true)
                                );
                                logger.warn("[SCT]: Received empty response when fetching Coleweight data for player: {}", playerName);
                                return;
                            }
                            ColeweightManager.updateColeweight(response.body());
                            logger.info("[SCT]: Successfully fetched Coleweight data for player: {}", playerName);
                            if (onComplete != null) onComplete.run();
                        } else {
                            Minecraft.getInstance().execute(() ->
                                    ChatUtils.INSTANCE.sendMessage("§cCouldn't find " + playerName + "'s coleweight.", true)
                            );
                            logger.warn("[SCT]: Failed to fetch Coleweight data for player: {}. HTTP status: {}", playerName, status);
                        }
                    })
                    .exceptionally(e -> {
                        logger.error("[SCT]: An error occurred while fetching Coleweight data. ", e);
                        return null;
                    });
        } catch (Exception e) {
            logger.error("[SCT]: An error occurred while fetching Coleweight data. ", e);
        }
    }

    public static void fetchColeweightLbAsync(Runnable onComplete) {
        try {
            URI uri = URI.create(URLManager.COLEWEIGHT_URL + "/lb");

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", URLManager.AGENT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        int status = response.statusCode();
                        if (status == 200) {
                            if (response.body() == null || response.body().isEmpty()) {
                                Minecraft.getInstance().execute(() ->
                                        ChatUtils.INSTANCE.sendMessage("§cCouldn't fetch Coleweight leaderboard data.", true)
                                );
                                logger.warn("[SCT]: Received empty response when fetching Coleweight leaderboard data.");
                                return;
                            }
                            ColeweightManager.updateColeweightLb(response.body(), false);
                            logger.info("[SCT]: Successfully fetched Coleweight leaderboard data.");
                            if (onComplete != null) {
                                try {
                                    onComplete.run();
                                } catch (Exception e) {
                                    logger.error("[SCT]: An error occurred while executing the onComplete callback. ", e);
                                }
                            }
                        } else {
                            logger.warn("[SCT]: Failed to fetch Coleweight leaderboard data. HTTP status: {}", status);
                        }
                    })
                    .exceptionally(e -> {
                        logger.error("[SCT]: An error occurred while fetching Coleweight leaderboard data.", e);
                        return null;
                    });
        } catch (Exception e) {
            logger.error("[SCT]: An error occurred while fetching Coleweight leaderboard data.", e);
        }
    }

    public static void fetchColeweightLbTop1k() {
        try {
            URI uri = URI.create(URLManager.COLEWEIGHT_URL + "/top1k");

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", URLManager.AGENT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();
            if (status == 200) {
                if (response.body() == null || response.body().isEmpty()) {
                    logger.warn("Received empty response when fetching Coleweight leaderboard for top 1k players.");
                    return;
                }
                ColeweightManager.updateColeweightLb(response.body(), true);
                hasColeweightLb = true;
                logger.info("[SCT] Successfully fetched Coleweight leaderboard for top 1k players.");
            } else {
                logger.warn("[SCT]: Failed to fetch Coleweight leaderboard for top 1k players. HTTP status: {}", status);
            }
        } catch (Exception e) {
            logger.error("[SCT]: An error occurred while fetching Coleweight leaderboard data.", e);
        }
    }

    public static String fetchColeweightData() {
        try {
            URI uri = URI.create(URLManager.COLEWEIGHT_URL);

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Bearer " + TokenManager.getToken())
                    .header("X-UUID", PlayerData.INSTANCE.getPlayerUUID())
                    .header("X-NAME", PlayerData.INSTANCE.getPlayerName())
                    .header("User-Agent", URLManager.AGENT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();

            if (status == 401) {
                logger.warn("[SCT]: Invalid or expired token. Fetching a new one and retrying...");
                TokenManager.fetchAndStoreToken();
                request = HttpRequest.newBuilder(uri)
                        .timeout(Duration.ofSeconds(15))
                        .header("Authorization", "Bearer " + TokenManager.getToken())
                        .header("X-UUID", PlayerData.INSTANCE.getPlayerUUID())
                        .header("X-NAME", PlayerData.INSTANCE.getPlayerName())
                        .header("User-Agent", URLManager.AGENT)
                        .header("Accept", "application/json")
                        .GET()
                        .build();
                response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                status = response.statusCode();
            }

            if (status == 200) {
                logger.info("[SCT]: Successfully fetched Coleweight data for player: {}", PlayerData.INSTANCE.getPlayerName());
                return response.body();
            } else {
                Minecraft.getInstance().execute(() ->
                        ChatUtils.INSTANCE.sendMessage("§cCouldn't find your coleweight.", true)
                );
                logger.warn("[SCT]: Failed to fetch Coleweight data for player: {}. HTTP status: {}", PlayerData.INSTANCE.getPlayerName(), status);
                return null;
            }
        } catch (Exception e) {
            logger.error("[SCT]: An error occurred while fetching Coleweight data. ", e);
        }
        return null;
    }

    public static void setGlobalColor(String player, String color) {
        try {
            URI uri = URI.create(URLManager.COLEWEIGHT_URL + "/color");

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Bearer " + TokenManager.getToken())
                    .header("X-UUID", PlayerData.INSTANCE.getPlayerUUID())
                    .header("X-NAME", player)
                    .header("X-COLOR", color)
                    .header("User-Agent", URLManager.AGENT)
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();

            if (status == 401) {
                logger.warn("[SCT]: Invalid or expired token. Fetching a new one and retry...");
                TokenManager.fetchAndStoreToken();
                request = HttpRequest.newBuilder(uri)
                        .timeout(Duration.ofSeconds(15))
                        .header("Authorization", "Bearer " + TokenManager.getToken())
                        .header("X-UUID", PlayerData.INSTANCE.getPlayerUUID())
                        .header("X-NAME", player)
                        .header("X-COLOR", color)
                        .header("User-Agent", URLManager.AGENT)
                        .header("Accept", "application/json")
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();

                response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                status = response.statusCode();
            }

            if (status == 200) {
                logger.info("[SCT]: Successfully set global Coleweight color for player: {}", player);
                Minecraft.getInstance().execute(() ->
                        ChatUtils.INSTANCE.sendComponent(Component.empty()
                                .append("§aGlobal color set to ")
                                .append(ColorUtils.INSTANCE.coloredText(color))
                                .append("."), true));
            } else {
                logger.warn("[SCT]: Failed to set global Coleweight color for player: {}. HTTP status: {}", player, status);
                Minecraft.getInstance().execute(() ->
                        ChatUtils.INSTANCE.sendMessage("§cFailed to set global Coleweight color.", true)
                );
            }
        } catch (Exception e) {
            logger.error("[SCT]: An error occurred while setting global Coleweight color. ", e);
        }
    }

    public static void fetchColeweightTopColors() {
        try {
            URI uri = URI.create(URLManager.COLEWEIGHT_URL + "/colors");

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(5))
                    .header("User-Agent", URLManager.AGENT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();
            if (status == 200) {
                if (response.body() == null || response.body().isEmpty()) {
                    logger.warn("Received empty response when fetching Coleweight top colors.");
                    return;
                }
                ColeweightManager.updateColeweightTopColors(response.body());
                hasColeweightTopColors = true;
                logger.info("[SCT] Successfully fetched Coleweight top colors.");
            } else {
                logger.warn("[SCT]: Failed to fetch Coleweight top colors. HTTP status: {}", status);
            }
        } catch (Exception e) {
            logger.error("[SCT]: An error occurred while fetching Coleweight top colors.", e);
        }
    }
}