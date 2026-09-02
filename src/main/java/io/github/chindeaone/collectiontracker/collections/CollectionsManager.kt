package io.github.chindeaone.collectiontracker.collections

import io.github.chindeaone.collectiontracker.commands.CollectionTracker.collectionList

object CollectionsManager {
    var collections = linkedMapOf<String, Set<String>>()
    var collectionSource: String? = null
    var collectionType: String? = null

    var multiCollectionSource: MutableList<String> = mutableListOf()
    var multiCollectionTypes: MutableMap<String, String> = mutableMapOf()

    fun isValidCollection(collectionName: String): Boolean {
        for (collectionSet in collections.values) {
            if (collectionSet.contains(collectionName)) {
                return true
            }
        }
        return false
    }

    fun isCollection(collectionName: String): Boolean {
        for (entry in collections.entries) {
            if (entry.key != "Miscellaneous" && entry.value.contains(collectionName)) {
                return true
            }
        }
        return false
    }

    val allCollections: MutableList<String>
        get() {
            val allCollections: MutableList<String> = mutableListOf()
            for (collectionSet in collections.values) {
                allCollections.addAll(collectionSet)
            }
            return allCollections
        }

    fun isRiftCollection(collectionName: String): Boolean {
        return collections
            .getOrDefault("Rift", mutableSetOf())
            .contains(collectionName)
    }

    fun hasAnyRiftCollection(): Boolean {
        val riftCollections = collections.getOrDefault("Rift", mutableSetOf())

        return collectionList.stream().anyMatch { o: String? -> riftCollections.contains(o) }
    }

    fun hasAllRiftCollections(): Boolean {
        val riftCollections = collections.getOrDefault("Rift", mutableSetOf())

        return riftCollections.containsAll(collectionList)
    }

    fun resetCollections() {
        collectionSource = null
        collectionType = null
    }

    fun resetMultiCollections() {
        multiCollectionSource.clear()
        multiCollectionTypes.clear()
    }
}