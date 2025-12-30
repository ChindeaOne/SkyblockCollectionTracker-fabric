package io.github.chindeaone.collectiontracker.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class URLManager {
    public static final String TOKEN_URL;
    public static final String TRACKED_COLLECTION_URL;
    public static final String AVAILABLE_COLLECTIONS_URL;
    public static final String AVAILABLE_GEMSTONES_URL;
    public static final String NPC_PRICES_URL;
    public static final String BAZAAR_URL;
    public static final String STATUS_URL;
    public static final String AGENT;

    static {
        Properties props = new Properties();
        try (InputStream in = URLManager.class.getClassLoader().getResourceAsStream("assets/skyblockcollectiontracker/url.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load urls.properties", e);
        }
        TOKEN_URL = props.getProperty("TOKEN_URL");
        TRACKED_COLLECTION_URL = props.getProperty("TRACKED_COLLECTION_URL");
        AVAILABLE_COLLECTIONS_URL = props.getProperty("AVAILABLE_COLLECTIONS_URL");
        AVAILABLE_GEMSTONES_URL = props.getProperty("AVAILABLE_GEMSTONES_URL");
        NPC_PRICES_URL = props.getProperty("NPC_PRICES_URL");
        BAZAAR_URL = props.getProperty("BAZAAR_URL");
        STATUS_URL = props.getProperty("STATUS_URL");
        AGENT = props.getProperty("AGENT");
    }
}