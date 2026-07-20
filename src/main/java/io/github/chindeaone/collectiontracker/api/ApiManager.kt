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
    const val API_URL = "https://skyblockcollections.com/v2"
    const val AGENT_BASE = "SCT"

    private val logger: Logger = LogManager.getLogger(ApiManager::class.java)

    val session: MinecraftSessionService
        get() = Minecraft.getInstance().services().sessionService()

    val agent: String
        get() = "$AGENT_BASE/${SkyblockCollectionTracker.VERSION}"

    var serverId: String? = null
        private set

    private var lastTokenRequest = 0L

    // Same logic as SkyblockPv
    fun authenticateMojang(notify: Boolean = false) {
        serverId = UUID.randomUUID().toString()
        val profile = PlayerData.profileId
        val accessToken = PlayerData.accessToken

        try {
            session.joinServer(profile, accessToken, serverId)
            logger.info("Registered session with Mojang")

            TokenManager.fetchAndStoreToken(notify)
        } catch (e: Exception) {
            logger.error("[SCT]: Failed to authenticate with Mojang servers: ${e.message}", e)
        }
    }

    // manual fetch
    @JvmStatic
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

    fun request(
        path: String,
        headers: List<Pair<String, String>> = emptyList()
    ): HttpResponse<String> {
        val request = buildRequest(path, "GET", headers)
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
    }

    fun requestAsync(
        path: String,
        headers: List<Pair<String, String>> = emptyList()
    ): CompletableFuture<HttpResponse<String>> {
        val request = buildRequest(path, "GET", headers)
        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
    }

    fun post(
        path: String,
        headers: List<Pair<String, String>> = emptyList()
    ): HttpResponse<String> {
        val request = buildRequest(path, "POST", headers)
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
    }

    private fun buildRequest(
        path: String,
        method: String,
        headers: List<Pair<String, String>>
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

    fun checkServer(path: String): CompletableFuture<Boolean> {
        val request = HttpRequest.newBuilder(URI.create("$API_URL/$path"))
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