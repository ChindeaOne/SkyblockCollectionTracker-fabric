package io.github.chindeaone.collectiontracker.api.skilltreeapi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.chindeaone.collectiontracker.api.URLManager;
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager;
import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.ConfigHelper;
import io.github.chindeaone.collectiontracker.utils.PlayerData;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static io.github.chindeaone.collectiontracker.api.URLManager.HTTP_CLIENT;

public class FetchSkillTree {

    public static final Logger logger = LogManager.getLogger(FetchSkillTree.class);
    public static volatile boolean hasSkillTree = false;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static ScheduledFuture<?> pendingHotmReset = null;
    private static ScheduledFuture<?> pendingHotfReset = null;

    public static void fetchSkillTree(boolean mining, boolean foraging) {
        if (alreadyHasData()) {
            hasSkillTree = true;
            logger.info("[SCT]: Skill tree data already exists. Skipping fetch.");
            return;
        }
        try {
            URI uri = URI.create(URLManager.SKILLTREE_URL);

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(5))
                    .header("Authorization", "Bearer " + TokenManager.getToken())
                    .header("X-UUID", PlayerData.INSTANCE.getPlayerUUID())
                    .header("X-MINING", String.valueOf(mining))
                    .header("X-FORAGING", String.valueOf(foraging))
                    .header("User-Agent", URLManager.AGENT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<InputStream> response =
                    HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

            int status = response.statusCode();
            if (status == 200) {
                try (Reader reader = new InputStreamReader(response.body(), StandardCharsets.UTF_8)) {
                    JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

                    if (jsonObject.has("mining")) {
                        JsonObject miningTree = jsonObject.getAsJsonObject("mining");
                        if (miningTree.has("core_of_the_mountain")) {
                            ConfigHelper.setCotmLevel(miningTree.get("core_of_the_mountain").getAsInt());
                        }
                        if (miningTree.has("professional")) {
                            ConfigHelper.setProfessionalMS(miningTree.get("professional").getAsInt());
                        }
                        if (miningTree.has("strong_arm")) {
                            ConfigHelper.setStrongArmMS(miningTree.get("strong_arm").getAsInt());
                        }
                    }
                    if (jsonObject.has("foraging")) {
                        JsonObject foragingTree = jsonObject.getAsJsonObject("foraging");
                        if (foragingTree.has("center_of_the_forest")) {
                            ConfigHelper.setCotfLevel(foragingTree.get("center_of_the_forest").getAsInt());
                        }
                    }
                    hasSkillTree = true;
                    logger.info("[SCT]: Successfully fetched skill tree data.");
                }
            } else {
                logger.error("[SCT]: Failed to fetch skill tree data. Server responded with code: {}", status);
            }
        } catch (Exception e) {
            logger.error("[SCT]: An error occurred while fetching skill tree data: ", e);
        }
    }

    public static void resetHotm() {
        if (pendingHotmReset == null || pendingHotmReset.isDone()) {
            pendingHotmReset = scheduler.schedule(() -> fetchSkillTree(true, false), 10, TimeUnit.MINUTES);
            logger.info("[SCT]: Scheduled a mining skill tree fetch in 10 minutes.");
        }
    }

    public static void resetHotf() {
        if (pendingHotfReset == null || pendingHotfReset.isDone()) {
            pendingHotfReset = scheduler.schedule(() -> fetchSkillTree(false, true), 10, TimeUnit.MINUTES);
            logger.info("[SCT]: Scheduled a foraging skill tree fetch in 10 minutes.");
        }
    }

    private static boolean alreadyHasData() {
        return ConfigAccess.getCotmLevel() != 0 && ConfigAccess.getProfessionalMS() != 0 && ConfigAccess.getStrongArmMS() != 0 && ConfigAccess.getCotfLevel() != 0;
    }
}
