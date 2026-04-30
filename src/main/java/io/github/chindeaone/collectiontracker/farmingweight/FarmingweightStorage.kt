package io.github.chindeaone.collectiontracker.farmingweight

data class FarmingweightStorage(
    val weight: Float = 0f,
    val rank: Int = 0,
    val leaderboard: List<FarmingweightPlayer> = emptyList(),
    val tempLeaderboard: List<FarmingweightPlayer> = emptyList(),

    val topColors: Map<String, String> = emptyMap()
)

data class FarmingweightPlayer(
    val name: String,
    val weight: Float
)

