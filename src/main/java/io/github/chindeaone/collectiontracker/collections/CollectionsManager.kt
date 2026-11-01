package io.github.chindeaone.collectiontracker.collections

object CollectionsManager {

    enum class CollectionTier(private val displayName: String, val shortName: String) {
        BASE("Base", "base"),
        ENCHANTED("Enchanted", "ench"),
        SUPER_ENCHANTED("Super Enchanted", "super ench");

        override fun toString(): String = displayName
    }

    var collections = linkedMapOf<String, Set<String>>()

    val collectionList: List<String>
        get() = collections.values.flatten()

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
        for ((key, value) in collections) {
            if (key != "Miscellaneous" && value.contains(collectionName)) {
                return true
            }
        }
        return false
    }

    fun isRiftCollection(collectionName: String): Boolean =
        collections.getOrDefault("Rift", emptySet()).contains(collectionName)

    fun isAnyRiftTracked(list: Collection<String>): Boolean =
        list.any(::isRiftCollection)

    fun isAnyNonRiftTracked(list: Collection<String>): Boolean =
        list.any { !isRiftCollection(it) }

    fun resetCollections() {
        collectionSource = null
        collectionType = null
    }

    fun resetMultiCollections() {
        multiCollectionSource.clear()
        multiCollectionTypes.clear()
    }
}