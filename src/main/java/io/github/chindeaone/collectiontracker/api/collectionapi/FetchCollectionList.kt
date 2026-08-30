package io.github.chindeaone.collectiontracker.api.collectionapi

import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.LinkedHashSet

object FetchCollectionList {

    private val logger: Logger = LogManager.getLogger(FetchCollectionList::class.java)
    @Volatile
    var hasCollectionList: Boolean = false

    fun fetchCollectionList() {
        ApiManager.requestAsync("collections")
            .thenAccept { response ->
                if (response.statusCode() == 200) {
                    val json = JsonParser.parseString(response.body()).asJsonObject

                    for ((category, itemsArray) in json.entrySet()) {
                        val items = LinkedHashSet<String>()
                        for (item in itemsArray.asJsonArray) {
                            items.add(item.asString)
                        }
                        CollectionsManager.collections[category] = items
                    }

                    hasCollectionList = true
                    logger.info("[SCT]: Successfully received the collection list.")
                } else {
                    logger.error("[SCT]: Failed to fetch collection list. HTTP {}", response.statusCode())
                }
            }.exceptionally { e ->
                logger.error("[SCT]: Error while receiving the collection list", e)
                null
            }
    }
}