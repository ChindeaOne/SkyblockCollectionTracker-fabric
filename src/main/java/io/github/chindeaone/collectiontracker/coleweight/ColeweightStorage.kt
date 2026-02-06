package io.github.chindeaone.collectiontracker.coleweight

data class ColeweightStorage(
    val coleweight: Float = 0f,
    val rank: Int = 0,
    val percentage: Float = 0f,

    val experience: Map<String, Float> = emptyMap(),
    val powder: Map<String, Float> = emptyMap(),
    val collection: Map<String, Float> = emptyMap(),
    val miscellaneous: Map<String, Float> = emptyMap(),

    val leaderboard: List<ColeweightPlayer> = emptyList(),
    val tempLeaderboard: List<ColeweightPlayer> = emptyList()
)

data class ColeweightPlayer(
    val name: String,
    val coleweight: Float
)