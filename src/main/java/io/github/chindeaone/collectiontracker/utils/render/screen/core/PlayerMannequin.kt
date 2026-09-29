/**
 * This class is based on the SkyBlock Profile Viewer's implementation.
 */
package io.github.chindeaone.collectiontracker.utils.render.screen.core

import com.mojang.authlib.GameProfile
import io.github.chindeaone.collectiontracker.utils.MinecraftUtils
import net.minecraft.client.entity.ClientMannequin
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.EntityAttachment
import net.minecraft.world.entity.player.PlayerSkin
import net.minecraft.world.item.component.ResolvableProfile

class PlayerMannequin(gameProfile: GameProfile, private val nameTag: Component): ClientMannequin(MinecraftUtils.level!!, MinecraftUtils.playerSkinRenderCache) {

    private val playerProfile = ResolvableProfile.createResolved(gameProfile)

    private var playerSkin: PlayerSkin = DEFAULT_SKIN

    init {
        MinecraftUtils.playerSkinRenderCache.lookup(playerProfile).whenComplete { skin, _ ->
            skin.ifPresent { skin -> playerSkin = skin.playerSkin() }
        }
    }

    fun setRenderState(renderState: AvatarRenderState) {
        renderState.nameTag = nameTag
        renderState.nameTagAttachment = attachments.get(EntityAttachment.NAME_TAG, 0, yRot)
    }

    override fun getProfile(): ResolvableProfile = playerProfile

    override fun getSkin(): PlayerSkin = playerSkin

    override fun shouldShowName() = true
}