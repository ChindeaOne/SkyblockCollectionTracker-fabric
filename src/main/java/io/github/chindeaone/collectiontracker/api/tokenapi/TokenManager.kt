package io.github.chindeaone.collectiontracker.api.tokenapi;

public class TokenManager {

    private static volatile String token;

    public static void fetchAndStoreToken() throws Exception {
        TokenFetcher tokenFetcher = new TokenFetcher();
        token = tokenFetcher.fetchToken();
    }

    public static String getToken() {
        return token;
    }
}