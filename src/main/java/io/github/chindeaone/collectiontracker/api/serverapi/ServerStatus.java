package io.github.chindeaone.collectiontracker.api.serverapi;

import io.github.chindeaone.collectiontracker.api.URLManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ServerStatus {

    private static final Logger logger = LogManager.getLogger(ServerStatus.class);
    private static final int TIMEOUT = 3000; // 3 seconds
    private static final int READ_TIMEOUT = 1000; // 1 second

    public static boolean checkServer() {
        HttpURLConnection connection = null;
        try {
            URI uri = URI.create(URLManager.STATUS_URL);
            URL url = uri.toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setRequestProperty("User-Agent", URLManager.AGENT);
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);

            return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            logger.error("[SCT]: Error checking server status", e);
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
