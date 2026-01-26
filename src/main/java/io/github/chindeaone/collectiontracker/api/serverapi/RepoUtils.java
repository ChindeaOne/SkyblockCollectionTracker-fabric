package io.github.chindeaone.collectiontracker.api.serverapi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker;
import io.github.chindeaone.collectiontracker.api.URLManager;
import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.categories.About;
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

public class RepoUtils {
    private static final Logger logger = LogManager.getLogger(RepoUtils.class);

    public static volatile String latestVersion;
    public static String latestReleaseTag;
    public static String latestBetaTag;

    private static final String currentVersion = SkyblockCollectionTracker.VERSION;

    public static void checkGithubReleases() throws Exception {
        URI uri = URI.create(URLManager.GITHUB_URL);

        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(5))
                .header("User-Agent", URLManager.AGENT)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<InputStream> response =
                HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

        int status = response.statusCode();

        if (status != 200) {
            logger.error("[SCT]: Failed to fetch GitHub releases, response code: {}", status);
            return;
        }

        try (Reader reader = new InputStreamReader(response.body(), StandardCharsets.UTF_8)) {
            JsonObject jsonResponse = JsonParser.parseReader(reader).getAsJsonObject();
            logger.info("[SCT]: Successfully fetched GitHub releases");

            latestReleaseTag = jsonResponse.getAsJsonPrimitive("latest_tag").getAsString();
            latestBetaTag = jsonResponse.getAsJsonPrimitive("latest_beta_tag").getAsString();
        }
    }

    public static void setLatestVersion() {
        // Normalize tags before comparison
        latestReleaseTag = normalizeTags(latestReleaseTag);
        latestBetaTag = normalizeTags(latestBetaTag);

        // If this is current version, skip check
        if (currentVersion.equals(latestReleaseTag) || currentVersion.equals(latestBetaTag)) {
            latestVersion = null;
            return;
        }

        // Compare versions and set latestVersion accordingly
        if (ConfigAccess.getUpdateType().equals(About.UpdateType.RELEASE)) {
            if (latestVersion == null || !latestVersion.equals(latestReleaseTag)) {
                latestVersion = latestReleaseTag;
            }
        }
        if (ConfigAccess.getUpdateType().equals(About.UpdateType.BETA)) {
            if (latestVersion == null || !latestVersion.equals(latestBetaTag)) {
                latestVersion = latestBetaTag;
            }
        }
    }

    private static String normalizeTags(String tag) {
        // remove 'v' prefix if present
        tag = tag.startsWith("v") ? tag.substring(1) : tag;

        // remove metadata if preset
        int plusIndex = tag.indexOf('+');
        if (plusIndex != -1) {
            tag = tag.substring(0, plusIndex);
        }
        return tag;
    }
}