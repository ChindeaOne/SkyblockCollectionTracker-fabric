package io.github.chindeaone.collectiontracker.farmingweight

data class FarmingweightStorage(
    val weight: Float = 0f,
    val rank: Int = 0,

    val cropWeight: Map<String, Float> = emptyMap(),
    val bonusWeight: Map<String, Float> = emptyMap(),

    val leaderboard: List<FarmingweightPlayer> = emptyList(),
    val leaderboardRanks: Map<String, Int> = emptyMap(),
    val tempLeaderboard: List<FarmingweightPlayer> = emptyList(),

    val topColors: Map<String, String> = emptyMap()
)

data class FarmingweightPlayer(
    val name: String,
    val weight: Float
)

