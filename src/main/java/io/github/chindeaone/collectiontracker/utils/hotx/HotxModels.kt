package io.github.chindeaone.collectiontracker.utils.hotx

import net.minecraft.resources.Identifier

enum class HotxModels (private val itemName: String) {
    COAL_BLOCK("coal_block"),
    EMERALD_BLOCK("emerald_block"),
    REDSTONE_BLOCK("redstone_block"),
    COAL("coal"),
    EMERALD("emerald"),
    DIAMOND("diamond"),
    PALE_OAK_BUTTON("pale_oak_button"),
    STRIPPED_OAK_LOG("stripped_oak_log"),
    OAK_LOG("oak_log"),
    STRIPPED_MANGROVE_LOG("stripped_mangrove_log"),
    PALE_OAK_SAPLING("pale_oak_sapling"),
    OAK_SAPLING("oak_sapling");

    fun getIdentifierFromModel() = Identifier.withDefaultNamespace(itemName)
}