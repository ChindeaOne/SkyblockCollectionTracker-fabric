package io.github.chindeaone.collectiontracker.api.tokenapi

object TokenManager {

    @Volatile
    var token: String? = null
        private set

    fun fetchAndStoreToken() {
        token = TokenFetcher.fetchToken()
    }
}