package io.github.chindeaone.collectiontracker.api

import com.mojang.authlib.minecraft.MinecraftSessionService
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager
import io.github.chindeaone.collectiontracker.utils.PlayerData
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils.sendMessage
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration.Companion.minutes

object ApiManager {
    const val API_URL = "https://api.skyblockcollections.com/v3"
    const val AGENT_BASE = "SCT"

    private val logger: Logger = LogManager.getLogger(ApiManager::class.java)

    val session: MinecraftSessionService get() = Minecraft.getInstance().services().sessionService()

    val agent: String get() = "$AGENT_BASE/${SkyblockCollectionTracker.VERSION}"

    var serverId: String? = null

    private var lastTokenRequest = 0L

    // Same logic as SkyblockPv
    fun authenticateMojang(notify: Boolean = false): CompletableFuture<String?> {
        serverId = UUID.randomUUID().toString()
        val profile = PlayerData.profileId
        val accessToken = PlayerData.accessToken

        return CompletableFuture
            .supplyAsync {
                session.joinServer(profile, accessToken, serverId)
                logger.info("[SCT]: Registered session with Mojang")
            }
            .thenCompose {
                TokenManager.fetchAndStoreToken(notify)
            }
            .exceptionally { e ->
                logger.error("[SCT]: Failed to authenticate with Mojang servers: ${e.message}", e)
                null
            }
    }

    // tell the backend this player is no longer online
    fun removePlayer() {
        if (TokenManager.token == null) return
        try {
            val headers = mapOf(
                "Authorization" to "Bearer ${TokenManager.token}",
                "X-NAME" to PlayerData.cachedName
            )

            invalidateSession("player-logout", headers)
            logger.info("[SCT]: Successfully invalidated session on the backend")
        } catch (e: Exception) {
            logger.error("[SCT]: Failed to invalidate session on the backend: ${e.message}", e)
        }
    }

    // manual fetch
    fun fetchToken() {
        if (TokenManager.token != null) {
            sendMessage("§cToken already exists.", true)
            return
        }

        val now = System.currentTimeMillis()
        if (now - lastTokenRequest < 1.minutes.inWholeMilliseconds) {
            sendMessage("§cPlease wait before requesting a new token.", true)
            return
        }
        lastTokenRequest = now

        CompletableFuture.runAsync { authenticateMojang(true) }
    }

    fun requestAsync(
        path: String,
        headers: Map<String, String> = emptyMap()
    ): CompletableFuture<HttpResponse<String>> {
        val request = buildRequest(path, "GET", headers)
        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
    }

    fun postAsync(
        path: String,
        headers: Map<String, String> = emptyMap()
    ): CompletableFuture<HttpResponse<String>> {
        val request = buildRequest(path, "POST", headers)
        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
    }

    fun invalidateSession(
        path: String,
        headers: Map<String, String> = emptyMap()
    ) {
        val request = buildRequest(path, "POST", headers)
        HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
    }

    private fun buildRequest(
        path: String,
        method: String,
        headers: Map<String, String>
    ): HttpRequest {
        val builder = HttpRequest.newBuilder(URI.create("$API_URL/$path"))
            .timeout(Duration.ofSeconds(5))
            .header("User-Agent", agent)
            .header("Accept", "application/json")

        headers.forEach { (key, value) ->
            builder.header(key, value)
        }

        when (method) {
            "GET" -> builder.GET()
            "POST" -> builder.POST(HttpRequest.BodyPublishers.noBody())
            else -> error("Unsupported method: $method")
        }

        return builder.build()
    }

    fun checkServer(): CompletableFuture<Boolean> {
        val request = HttpRequest.newBuilder(URI.create("$API_URL/status"))
            .timeout(Duration.ofSeconds(3))
            .header("User-Agent", agent)
            .HEAD()
            .build()

        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.discarding())
            .handle { resp, ex ->
                if (ex != null) {
                    logger.debug("[SCT]: Failed server check.", ex)
                    false
                } else {
                    resp.statusCode() == 200
                }
            }
    }

    val HTTP_CLIENT: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .version(HttpClient.Version.HTTP_2)
        .build()
}