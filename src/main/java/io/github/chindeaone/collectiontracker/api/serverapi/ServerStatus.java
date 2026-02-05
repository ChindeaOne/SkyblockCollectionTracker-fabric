package io.github.chindeaone.collectiontracker.api.serverapi;

import io.github.chindeaone.collectiontracker.api.URLManager;
import io.github.chindeaone.collectiontracker.api.collectionapi.FetchCollectionList;
import io.github.chindeaone.collectiontracker.api.collectionapi.FetchGemstoneList;
import io.github.chindeaone.collectiontracker.api.colors.FetchColors;
import io.github.chindeaone.collectiontracker.api.npcpriceapi.FetchNpcPrices;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static io.github.chindeaone.collectiontracker.api.URLManager.HTTP_CLIENT;

public class ServerStatus {

    private static final Logger logger = LogManager.getLogger(ServerStatus.class);

    public static CompletableFuture<Boolean> checkServerAsync() {
        URI uri = URI.create(URLManager.STATUS_URL);

        HttpRequest request = HttpRequest.newBuilder(uri)
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .timeout(Duration.ofSeconds(3))
                .header("User-Agent", URLManager.AGENT)
                .build();

        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .handle((resp, ex) -> {
                    if (ex != null) {
                        logger.debug("[SCT]: Async check failed", ex);
                        return false;
                    }
                    return resp.statusCode() == 200;
                });
    }

    public static void checkServerAsync(Executor callbackExecutor, Consumer<Boolean> callback) {
        checkServerAsync()
                .thenAcceptAsync(result -> {
                    try {
                        callback.accept(result);
                    } catch (Exception e) {
                        logger.error("[SCT]: Error in server status callback", e);
                    }
                }, callbackExecutor);
    }

    public static synchronized boolean hasData() {
        return FetchColors.hasColors && FetchNpcPrices.hasNpcPrice && FetchCollectionList.hasCollectionList && FetchGemstoneList.hasGemstoneList;
    }
}