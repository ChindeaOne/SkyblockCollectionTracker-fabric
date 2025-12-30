package io.github.chindeaone.collectiontracker.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RepoUtils {
    private static final String API_URL = "https://api.github.com/repos/ChindeaOne/SkyblockCollectionTracker-fabric/releases";
    private static final Logger logger = LogManager.getLogger(RepoUtils.class);
    public static String latestVersion;

    public static void checkForUpdates(int updateSetting) {
        try {
            String currentVersion = SkyblockCollectionTracker.VERSION;
            URI uri = URI.create(API_URL);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

            if (connection.getResponseCode() != 200) {
                logger.error("[SCT]: Failed to check for updates. HTTP Response Code: {}", connection.getResponseCode());
                return;
            }

            if (connection.getResponseCode() == 403) {
                // GitHub API rate limit exceeded
                ChatUtils.INSTANCE.sendMessage("Unfortunately, the GitHub API rate limit has been exceeded. The mod will not be able to update right now.", true);
                logger.warn("[SCT]: GitHub API rate limit exceeded. Please try again later.");
                return;
            }

            JsonArray releases = getJsonArray(connection);

            String latestStable = null;
            String latestBeta = null;

            for (JsonElement element : releases) {
                JsonObject release = element.getAsJsonObject();
                boolean isPreRelease = release.get("prerelease").getAsBoolean();
                String versionTag = release.get("tag_name").getAsString();

                if (!isPreRelease && (latestStable == null || isNewerVersion(versionTag, latestStable))) {
                    latestStable = versionTag;
                }

                if (isPreRelease && (latestBeta == null || isNewerVersion(versionTag, latestBeta))) {
                    latestBeta = versionTag;
                }
            }

            String selectedVersion = null;

            if (updateSetting == 1) {
                selectedVersion = latestStable;
            } else if (updateSetting == 2) {
                if (latestStable != null && latestBeta != null) {
                    selectedVersion = isNewerVersion(latestStable, latestBeta) ? latestStable : latestBeta;
                } else {
                    selectedVersion = latestStable != null ? latestStable : latestBeta;
                }
            }

            // Stable-only logic
            if (updateSetting == 1 && selectedVersion != null && selectedVersion.contains("-")) {
                latestVersion = null;
                return;
            }

            // Compare to current
            if (selectedVersion != null && isNewerVersion(selectedVersion, currentVersion)) {
                latestVersion = selectedVersion;
            } else {
                latestVersion = null;
            }

        } catch (Exception e) {
            logger.error("[SCT]: An error occurred while checking for updates", e);
        }
    }

    public static boolean isNewerVersion(String candidateVersion, String currentVersion) {
        Version c = Version.parse(candidateVersion);
        Version cur = Version.parse(currentVersion);
        return c.compareTo(cur) > 0;
    }

    private static JsonArray getJsonArray(HttpURLConnection connection) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            JsonElement jsonElement = JsonParser.parseString(response.toString());
            return jsonElement.getAsJsonArray();
        }
    }

    private record Version(List<Integer> numericParts, String preLabel, int preNumber) implements Comparable<Version> {

        public static Version parse(String raw) {
                String version = raw.startsWith("v") ? raw.substring(1) : raw;
                String[] split = version.split("-", 2);

                List<Integer> numeric = Arrays.stream(split[0].split("\\."))
                        .map(s -> {
                            try {
                                return Integer.parseInt(s);
                            } catch (NumberFormatException e) {
                                return 0;
                            }
                        })
                        .collect(Collectors.toList());

                String preLabel = null;
                int preNum = 0;

                if (split.length == 2) {
                    Matcher matcher = Pattern.compile("([a-zA-Z]+)(\\d*)").matcher(split[1]);
                    if (matcher.matches()) {
                        preLabel = matcher.group(1).toLowerCase();
                        preNum = matcher.group(2).isEmpty() ? 1 : Integer.parseInt(matcher.group(2));
                    } else {
                        preLabel = split[1].toLowerCase();
                        preNum = 0;
                    }
                }

                return new Version(numeric, preLabel, preNum);
            }

            @Override
            public int compareTo(Version other) {
                int length = Math.max(this.numericParts.size(), other.numericParts.size());
                for (int i = 0; i < length; i++) {
                    int a = i < this.numericParts.size() ? this.numericParts.get(i) : 0;
                    int b = i < other.numericParts.size() ? other.numericParts.get(i) : 0;
                    if (a != b) return Integer.compare(a, b);
                }

                if (this.preLabel == null && other.preLabel != null) return 1;
                if (this.preLabel != null && other.preLabel == null) return -1;
                if (this.preLabel != null && other.preLabel != null) {
                    int cmp = this.preLabel.compareTo(other.preLabel);
                    if (cmp != 0) return cmp;
                    return Integer.compare(this.preNumber, other.preNumber);
                }

                return 0;
            }
        }
}

