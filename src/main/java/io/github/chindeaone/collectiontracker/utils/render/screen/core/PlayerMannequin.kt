/**
 * This class is based on the SkyBlock Profile Viewer's implementation.
 */
package io.github.chindeaone.collectiontracker.utils.render.screen.core

import com.mojang.authlib.GameProfile
import io.github.chindeaone.collectiontracker.utils.MinecraftUtils
import net.minecraft.client.entity.ClientMannequin
import net.minecraft.world.entity.player.PlayerSkin
import net.minecraft.world.item.component.ResolvableProfile

class PlayerMannequin(gameProfile: GameProfile): ClientMannequin(MinecraftUtils.level!!, MinecraftUtils.playerSkinRenderCache) {

    private val playerProfile = ResolvableProfile.createResolved(gameProfile)

    private var playerSkin: PlayerSkin = DEFAULT_SKIN

    init {
        MinecraftUtils.playerSkinRenderCache.lookup(playerProfile).whenComplete { skin, _ ->
            skin.ifPresent { skin -> playerSkin = skin.playerSkin() }
        }
    }

    override fun getProfile(): ResolvableProfile = playerProfile

    override fun getSkin(): PlayerSkin = playerSkin
}