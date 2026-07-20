package io.github.chindeaone.collectiontracker.api.tokenapi

object TokenManager {

    @Volatile
    var token: String? = null
        private set

    fun fetchAndStoreToken(notify: Boolean = false) {
        token = TokenFetcher.fetchToken(notify)
    }
}