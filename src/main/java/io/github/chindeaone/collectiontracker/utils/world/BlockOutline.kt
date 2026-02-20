package io.github.chindeaone.collectiontracker.utils.world

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.math.Axis
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.utils.HypixelUtils
import io.github.chindeaone.collectiontracker.utils.tab.MiningStatsWidget
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.core.BlockPos
import org.joml.Vector3f

object BlockOutline {

    fun renderWaypoint(context: WorldRenderContext) {
        if (!RenderSystem.isOnRenderThread()) return
        if (!HypixelUtils.isOnSkyblock) return

        val currentIsland = MiningStatsWidget.currentMiningIsland
        if (currentIsland != "Dwarven Mines" && currentIsland != "Mineshaft") return

        if (currentIsland == "Dwarven Mines" && !ConfigAccess.isMineshaftSpawnRoutesEnabled()) return
        if (currentIsland == "Mineshaft" && !ConfigAccess.isMineshaftRoutesEnabled()) return

        val camera = Minecraft.getInstance().gameRenderer.mainCamera
        val matrices = context.matrices()
        val buffers = context.consumers()

        WaypointsUtils.getCurrentTarget()
        val category = WaypointsUtils.currentCategory ?: return
        val allWaypoints = WaypointsUtils.getWaypointsForCategory(category)
        if (allWaypoints.isEmpty()) return

        val currentIndex = WaypointsUtils.currentIndex
        if (currentIndex >= allWaypoints.size) return

        // 2nd previous waypoint -> red (only if we have approached at least 2)
        if (currentIndex >= 2) {
            val (label, pos) = allWaypoints[currentIndex - 2]
            renderBlockOutline(matrices, buffers, pos, camera, 1f, 0f)
            renderText(matrices, buffers, pos, label, camera, 0xFFFF0000.toInt())
        }

        // previous waypoint -> yellow (only if we have approached at least 1)
        if (currentIndex >= 1 && currentIndex - 1 < allWaypoints.size) {
            val (label, pos) = allWaypoints[currentIndex - 1]
            renderBlockOutline(matrices, buffers, pos, camera, 1f, 1f)
            renderText(matrices, buffers, pos, label, camera, 0xFFFFFF00.toInt())
        }

        // current target waypoint -> green
        if (currentIndex < allWaypoints.size) {
            val (label, pos) = allWaypoints[currentIndex]
            renderBlockOutline(matrices, buffers, pos, camera, 0f, 1f)
            renderText(matrices, buffers, pos, label, camera, 0xFF00FF00.toInt())
            drawLinetoBlock(buffers, matrices, pos, camera)
        }
    }

    private fun renderBlockOutline(
        matrices: PoseStack,
        buffers: MultiBufferSource,
        pos: BlockPos,
        camera: Camera,
        r: Float,
        g: Float,
    ) {
        val vc: VertexConsumer = buffers.getBuffer(OutlineTypes.LINE_THROUGH_WALLS)
        matrices.pushPose()
        val cameraPos = camera.position()
        matrices.translate(pos.x - cameraPos.x, pos.y - cameraPos.y, pos.z - cameraPos.z)

        val matrix = matrices.last()

        val vertices = arrayOf(
            floatArrayOf(0f, 0f, 0f), floatArrayOf(1f, 0f, 0f), floatArrayOf(1f, 1f, 0f), floatArrayOf(0f, 1f, 0f),
            floatArrayOf(0f, 0f, 1f), floatArrayOf(1f, 0f, 1f), floatArrayOf(1f, 1f, 1f), floatArrayOf(0f, 1f, 1f)
        )

        // Draw edges
        val edges = arrayOf(
            intArrayOf(0, 1), intArrayOf(1, 2), intArrayOf(2, 3), intArrayOf(3, 0),
            intArrayOf(4, 5), intArrayOf(5, 6), intArrayOf(6, 7), intArrayOf(7, 4),
            intArrayOf(0, 4), intArrayOf(1, 5), intArrayOf(2, 6), intArrayOf(3, 7)
        )

        for (edge in edges) {
            val v1 = vertices[edge[0]]
            val v2 = vertices[edge[1]]

            val normal = Vector3f(v2[0] - v1[0], v2[1] - v1[1], v2[2] - v1[2]).normalize()

            vc.addVertex(matrices.last(), v1[0], v1[1], v1[2])
                .setColor(r, g, 0f, 1f)
                .setNormal(matrix, normal)
                .setLineWidth(2f)
            vc.addVertex(matrices.last(), v2[0], v2[1], v2[2])
                .setColor(r, g, 0f, 1f)
                .setNormal(matrix, normal)
                .setLineWidth(2f)
        }
        matrices.popPose()
    }

    private fun renderText(
        matrices: PoseStack,
        buffers: MultiBufferSource,
        pos: BlockPos,
        text: String,
        camera: Camera,
        color: Int
    ){
        val fr = Minecraft.getInstance().font

        matrices.pushPose()
        val cameraPos = camera.position()
        matrices.translate(pos.x + 0.5 - cameraPos.x, pos.y + 2.25 - cameraPos.y, pos.z + 0.5 - cameraPos.z)

        matrices.mulPose(Axis.YP.rotationDegrees(-camera.yRot()))
        matrices.mulPose(Axis.XP.rotationDegrees(camera.xRot()))

        matrices.scale(-0.03f, -0.03f, 0.03f)

        fr.drawInBatch(
            text,
            -fr.width(text) /2f,
            0f,
            color,
            false,
            matrices.last().pose(),
            buffers,
            Font.DisplayMode.SEE_THROUGH,
            0,
            LightTexture.FULL_BRIGHT
        )
        matrices.popPose()
    }

    private fun drawLinetoBlock(
        buffers: MultiBufferSource,
        poseStack: PoseStack,
        blockPos: BlockPos,
        camera: Camera,
    ) {
        val cameraPos = camera.position()
        val rotation = camera.rotation()

        val forward = Vector3f(0f, 0f, -1f).rotate(rotation)

        val sx = (forward.x() * 0.5f)
        val sy = (forward.y() * 0.5f)
        val sz = (forward.z() * 0.5f)
        val ex = (blockPos.x + 0.5 - cameraPos.x).toFloat()
        val ey = (blockPos.y + 1.0 - cameraPos.y).toFloat()
        val ez = (blockPos.z + 0.5 - cameraPos.z).toFloat()

        val vc = buffers.getBuffer(OutlineTypes.LINE_THROUGH_WALLS)
        val matrix = poseStack.last()

        val normal = Vector3f(ex - sx, ey - sy, ez - sz).normalize()

        vc.addVertex(matrix, sx, sy, sz)
            .setColor(0f, 1f, 0f, 1f)
            .setNormal(matrix, normal)
            .setLineWidth(2.0f)

        vc.addVertex(matrix, ex, ey, ez)
            .setColor(0f, 1f, 0f, 1f)
            .setNormal(matrix, normal)
            .setLineWidth(2.0f)
    }
}