/**
 * This class is based on the SkyBlock Profile Viewer's implementation.
 */
package io.github.chindeaone.collectiontracker.utils.render.screen.core

import com.mojang.authlib.GameProfile
import io.github.chindeaone.collectiontracker.utils.MinecraftUtils
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.renderer.entity.player.AvatarRenderer
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.network.chat.Component
import org.joml.Quaternionf
import org.joml.Vector3f

class PlayerMannequinWidget(profile: GameProfile, width: Int, height: Int) : AbstractWidget(0, 0, width, height, Component.empty()) {

    private val mannequin = PlayerMannequin(profile)
    private val renderer = MinecraftUtils.entityRenderDispatcher.getRenderer(mannequin) as AvatarRenderer
    private val renderState = AvatarRenderState()

    override fun extractWidgetRenderState(
        context: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float
    ) {
        renderer.extractRenderState(mannequin, renderState, partialTick)

        context.entity(
            renderState,
            40.0f,
            Vector3f(0f, 3f, 1f),
            Quaternionf()
                .rotateY(Math.PI.toFloat())
                .rotateZ(Math.PI.toFloat()),
            null,
            x,
            y,
            x + width,
            y + height
        )
    }

    override fun updateWidgetNarration(output: NarrationElementOutput) {}
}