package io.github.chindeaone.collectiontracker.api.tokenapi

import java.util.concurrent.CompletableFuture

object TokenManager {

    @Volatile
    var token: String? = null
        private set

    fun fetchAndStoreToken(notify: Boolean = false): CompletableFuture<String?> {
        return TokenFetcher.fetchToken(notify).thenApply { fetchedToken ->
            if (fetchedToken != null) {
                token = fetchedToken
            }
            fetchedToken
        }
    }
}