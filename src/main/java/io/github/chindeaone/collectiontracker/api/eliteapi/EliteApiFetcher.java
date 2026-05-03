package io.github.chindeaone.collectiontracker.api.eliteapi;

import io.github.chindeaone.collectiontracker.api.URLManager;
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager;
import io.github.chindeaone.collectiontracker.farmingweight.FarmingweightManager;
import io.github.chindeaone.collectiontracker.utils.ColorUtils;
import io.github.chindeaone.collectiontracker.utils.PlayerData;
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static io.github.chindeaone.collectiontracker.api.URLManager.HTTP_CLIENT;

public class EliteApiFetcher {

    private static final Logger logger = LogManager.getLogger(EliteApiFetcher.class);
    public static boolean hasFarmingweightLb = false;
    public static boolean hasFarmingweightTopColors = false;

    public static void fetchFarmingweightDataAsync(String playerName, String uuid, String profileId, Runnable onComplete) {
        try {
            URI uri = URI.create(URLManager.FARMINGWEIGHT_URL);
            HttpRequest request = buildPlayerRequest(uri, uuid, profileId);

            HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        int status = response.statusCode();

                        if (status == 401) {
                            logger.warn("[SCT]: Invalid or expired token. Fetching a new one and retrying...");
                            try {
                                TokenManager.fetchAndStoreToken();
                                HttpRequest retryRequest = buildPlayerRequest(uri, uuid, profileId);
                                HttpResponse<String> retryResponse = HTTP_CLIENT.send(retryRequest, HttpResponse.BodyHandlers.ofString());
                                status = retryResponse.statusCode();

                                if (status == 200) {
                                    if (retryResponse.body() == null || retryResponse.body().isEmpty()) {
                                        Minecraft.getInstance().execute(() ->
                                                ChatUtils.sendMessage("§cCouldn't find " + playerName + "'s Farming Weight.", true)
                                        );
                                        logger.warn("[SCT]: Received empty response when fetching Farming Weight data for player: {}", playerName);
                                        return;
                                    }
                                    FarmingweightManager.updateFarmingweight(retryResponse.body());
                                    logger.info("[SCT]: Successfully fetched Farming Weight data for player: {} (after token refresh)", playerName);
                                    if (onComplete != null) onComplete.run();
                                } else {
                                    Minecraft.getInstance().execute(() ->
                                            ChatUtils.sendMessage("§cCouldn't find " + playerName + "'s Farming Weight.", true)
                                    );
                                    logger.warn("[SCT]: Failed to fetch Farming Weight data for player: {} after token refresh. HTTP status: {}", playerName, status);
                                }
                            } catch (Exception e) {
                                logger.error("[SCT]: An error occurred while retrying Farming Weight fetch after token refresh.", e);
                            }
                            return;
                        }

                        if (status == 429) {
                            logger.warn("[SCT]: Rate limit exceeded for Farming Weight API. Limit: 10 requests per 10 minutes.");
                            Minecraft.getInstance().execute(() ->
                                    ChatUtils.sendMessage("§Farming weight fetching limit reached! Try again later.", true)
                            );
                            return;
                        }

                        if (status == 200) {
                            if (response.body() == null || response.body().isEmpty()) {
                                Minecraft.getInstance().execute(() ->
                                        ChatUtils.sendMessage("§cCouldn't find " + playerName + "'s Farming Weight.", true)
                                );
                                logger.warn("[SCT]: Received empty response when fetching Farming Weight data for player: {}", playerName);
                                return;
                            }
                            FarmingweightManager.updateFarmingweight(response.body());
                            logger.info("[SCT]: Successfully fetched Farming Weight data for player: {}", playerName);
                            if (onComplete != null) onComplete.run();
                        } else {
                            Minecraft.getInstance().execute(() ->
                                    ChatUtils.sendMessage("§cCouldn't find " + playerName + "'s Farming Weight.", true)
                            );
                            logger.warn("[SCT]: Failed to fetch Farming Weight data for player: {}. HTTP status: {}", playerName, status);
                        }
                    })
                    .exceptionally(e -> {
                        logger.error("[SCT]: An error occurred while fetching Farming Weight data.", e);
                        return null;
                    });
        } catch (Exception e) {
            logger.error("[SCT]: An error occurred while fetching Farming Weight data.", e);
        }
    }

    public static void fetchFarmingweightLbAsync(Runnable onComplete) {
        try {
            URI uri = URI.create(URLManager.FARMINGWEIGHT_URL + "/lb");

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
                                        ChatUtils.sendMessage("§cCouldn't fetch Farming Weight leaderboard data.", true)
                                );
                                logger.warn("[SCT]: Received empty response when fetching Farming Weight leaderboard data.");
                                return;
                            }
                            FarmingweightManager.updateFarmingweightLb(response.body(), false);
                            logger.info("[SCT]: Successfully fetched Farming Weight leaderboard data.");
                            if (onComplete != null) {
                                try {
                                    onComplete.run();
                                } catch (Exception e) {
                                    logger.error("[SCT]: An error occurred while executing the onComplete callback.", e);
                                }
                            }
                        } else {
                            logger.warn("[SCT]: Failed to fetch Farming Weight leaderboard data. HTTP status: {}", status);
                        }
                    })
                    .exceptionally(e -> {
                        logger.error("[SCT]: An error occurred while fetching Farming Weight leaderboard data.", e);
                        return null;
                    });
        } catch (Exception e) {
            logger.error("[SCT]: An error occurred while fetching Farming Weight leaderboard data.", e);
        }
    }

    public static void fetchFarmingweightLbTop1k() {
        try {
            URI uri = URI.create(URLManager.FARMINGWEIGHT_URL + "/top1k");

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
                    logger.warn("Received empty response when fetching Farming Weight leaderboard for top 1k players.");
                    return;
                }
                FarmingweightManager.updateFarmingweightLb(response.body(), true);
                hasFarmingweightLb = true;
                logger.info("[SCT] Successfully fetched Farming Weight leaderboard for top 1k players.");
            } else {
                logger.warn("[SCT]: Failed to fetch Farming Weight leaderboard for top 1k players. HTTP status: {}", status);
            }
        } catch (Exception e) {
            logger.error("[SCT]: An error occurred while fetching Farming Weight leaderboard data.", e);
        }
    }

    public static void setGlobalColor(String playerName, String uuid, String color) {
        try {
            URI uri = URI.create(URLManager.FARMINGWEIGHT_URL + "/color");

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Bearer " + TokenManager.getToken())
                    .header("X-UUID", uuid)
                    .header("X-NAME", playerName)
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
                        .header("X-UUID", uuid)
                        .header("X-NAME", playerName)
                        .header("X-COLOR", color)
                        .header("User-Agent", URLManager.AGENT)
                        .header("Accept", "application/json")
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();

                response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                status = response.statusCode();
            }

            if (status == 200) {
                logger.info("[SCT]: Successfully set global Farming Weight color for player: {}", playerName);
                Minecraft.getInstance().execute(() ->
                        ChatUtils.INSTANCE.sendComponent(Component.empty()
                                .append("§aGlobal color set to ")
                                .append(ColorUtils.INSTANCE.coloredText(color))
                                .append("."), true));
            } else {
                logger.warn("[SCT]: Failed to set global Farming Weight color for player: {}. HTTP status: {}", playerName, status);
                Minecraft.getInstance().execute(() ->
                        ChatUtils.sendMessage("§cFailed to set global Farming Weight color.", true)
                );
            }
        } catch (Exception e) {
            logger.error("[SCT]: An error occurred while setting global Farming Weight color. ", e);
        }
    }

    public static void fetchFarmingweightTopColors() {
        try {
            URI uri = URI.create(URLManager.FARMINGWEIGHT_URL + "/colors");

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
                    logger.warn("Received empty response when fetching Farming Weight top colors.");
                    return;
                }
                FarmingweightManager.updateFarmingweightTopColors(response.body());
                hasFarmingweightTopColors = true;
                logger.info("[SCT] Successfully fetched Farming Weight top colors.");
            } else {
                logger.warn("[SCT]: Failed to fetch Farming Weight top colors. HTTP status: {}", status);
            }
        } catch (Exception e) {
            logger.error("[SCT]: An error occurred while fetching Farming Weight top colors.", e);
        }
    }

    public static String fetchCollectionLeaderboard(String collection) {
        try {
            URI uri = URI.create(URLManager.COLLECTION_LEADERBOARD_URL + "/" + collection);

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Bearer " + TokenManager.getToken())
                    .header("X-NAME", PlayerData.INSTANCE.getPlayerName())
                    .header("X-UUID", PlayerData.INSTANCE.getPlayerUUID())
                    .header("X-PROFILEID", PlayerData.INSTANCE.getProfileId())
                    .header("User-Agent", URLManager.AGENT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();

            if (status == 401) {
                logger.warn("[SCT]: Invalid or expired token for leaderboard. Fetching a new one and retrying...");
                TokenManager.fetchAndStoreToken();
                String token = TokenManager.getToken();

                request = HttpRequest.newBuilder(uri)
                        .timeout(Duration.ofSeconds(15))
                        .header("Authorization", "Bearer " + token)
                        .header("X-NAME", PlayerData.INSTANCE.getPlayerName())
                        .header("X-UUID", PlayerData.INSTANCE.getPlayerUUID())
                        .header("X-PROFILEID", PlayerData.INSTANCE.getProfileId())
                        .header("User-Agent", URLManager.AGENT)
                        .header("Accept", "application/json")
                        .GET()
                        .build();

                response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                status = response.statusCode();
            }

            if (status == 200) {
                return response.body();
            } else {
                logger.error("[SCT]: Failed to fetch leaderboard data. Server responded with code: {}", status);
            }
        } catch (Exception e) {
            logger.error("[SCT]: An error occurred while fetching leaderboard data", e);
        }
        return null;
    }

    private static HttpRequest buildPlayerRequest(URI uri, String uuid, String profileId) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + TokenManager.getToken())
                .header("X-UUID", uuid)
                .header("X-PROFILEID", profileId)
                .header("User-Agent", URLManager.AGENT)
                .header("Accept", "application/json");

        return builder.GET().build();
    }
}

