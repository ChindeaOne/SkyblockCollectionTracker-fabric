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

    public static void checkForUpdates(String updateSetting) {
        try {
            String currentVersion = SkyblockCollectionTracker.VERSION;
            URI uri = URI.create(API_URL);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

            if (connection.getResponseCode() == 403) {
                // GitHub API rate limit exceeded
                ChatUtils.INSTANCE.sendMessage("Unfortunately, the GitHub API rate limit has been exceeded. The mod will not be able to update right now.", true);
                logger.warn("[SCT]: GitHub API rate limit exceeded. Please try again later.");
                return;
            }

            if (connection.getResponseCode() != 200) {
                logger.error("[SCT]: Failed to check for updates. HTTP Response Code: {}", connection.getResponseCode());
                return;
            }

            JsonArray releases = getJsonArray(connection);

            String latestStableTag = null;
            String latestBetaTag = null;

            for (JsonElement element : releases) {
                JsonObject release = element.getAsJsonObject();
                boolean isPreRelease = release.get("prerelease").getAsBoolean();
                String versionTag = release.get("tag_name").getAsString();

                if (!isPreRelease && (latestStableTag == null || isNewerReleaseTag(versionTag, latestStableTag))) {
                    latestStableTag = versionTag;
                }

                if (isPreRelease && (latestBetaTag == null || isNewerReleaseTag(versionTag, latestBetaTag))) {
                    latestBetaTag = versionTag;
                }
            }

//            Testing only
//            latestStableTag = "v1.0.8+1.21.10";
//            latestBetaTag = "v1.0.9-beta2+1.21.10";

            String selectedTag = null;

            if (updateSetting.equals("RELEASE")) {
                selectedTag = latestStableTag;
            } else if (updateSetting.equals("BETA")) {
                if (latestStableTag != null && latestBetaTag != null) {
                    selectedTag = isNewerReleaseTag(latestStableTag, latestBetaTag) ? latestStableTag : latestBetaTag;
                } else {
                    selectedTag = latestStableTag != null ? latestStableTag : latestBetaTag;
                }
            }

            // Stable-only logic
            if (updateSetting.equals("RELEASE") && selectedTag != null && normalizeComparableVersion(selectedTag).contains("-")) {
                latestVersion = null;
                return;
            }

            // Compare to current
            if (selectedTag != null) {
                String selectedComparable = normalizeComparableVersion(selectedTag);
                if (isNewerReleaseTag(selectedComparable, currentVersion)) latestVersion = selectedComparable;
                else latestVersion = null;
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

    private static boolean isNewerReleaseTag(String candidateTag, String currectTag) {
        String c = normalizeComparableVersion(candidateTag);
        String cur = normalizeComparableVersion(currectTag);
        return isNewerVersion(c, cur);
    }

    private static String normalizeComparableVersion(String raw) {
        if (raw == null) return "";
        String v = raw.trim();
        if (v.startsWith("v") || v.startsWith("V")) v = v.substring(1);
        int plus = v.indexOf('+');
        if (plus >= 0) v = v.substring(0, plus);
        return v;
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

