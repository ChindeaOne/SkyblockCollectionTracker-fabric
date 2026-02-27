package io.github.chindeaone.collectiontracker.utils.world

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexConsumer
import io.github.chindeaone.collectiontracker.api.waypointsapi.FetchWaypoints
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.utils.HypixelUtils
import io.github.chindeaone.collectiontracker.utils.tab.MiningStatsWidget
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.state.CameraRenderState
import net.minecraft.core.BlockPos
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

object BlockOutline {

    fun renderWaypoint(context: WorldRenderContext) {
        if (!RenderSystem.isOnRenderThread()) return
        if (!HypixelUtils.isOnSkyblock) return
        if (!FetchWaypoints.hasWaypoints) return

        val currentIsland = MiningStatsWidget.currentMiningIsland
        if (currentIsland != "Dwarven Mines" && currentIsland != "Mineshaft") return

        if (currentIsland == "Dwarven Mines" && !ConfigAccess.isMineshaftSpawnRoutesEnabled()) return
        if (currentIsland == "Mineshaft" && !ConfigAccess.isMineshaftRoutesEnabled()) return

        val camera = context.worldState().cameraRenderState
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
            renderBlockOutline(buffers, pos, camera, 1f, 0f)
            renderText(buffers, pos, label, camera, 0xFFFF0000.toInt())
        }

        // previous waypoint -> yellow (only if we have approached at least 1)
        if (currentIndex >= 1 && currentIndex - 1 < allWaypoints.size) {
            val (label, pos) = allWaypoints[currentIndex - 1]
            renderBlockOutline(buffers, pos, camera, 1f, 1f)
            renderText(buffers, pos, label, camera, 0xFFFFFF00.toInt())
        }

        // current target waypoint -> green
        if (currentIndex < allWaypoints.size) {
            val (label, pos) = allWaypoints[currentIndex]
            renderBlockOutline(buffers, pos, camera, 0f, 1f)
            renderText(buffers, pos, label, camera, 0xFF00FF00.toInt())
            drawLinetoBlock(buffers,pos, camera)
        }
    }

    private fun renderBlockOutline(
        buffers: MultiBufferSource,
        pos: BlockPos,
        camera: CameraRenderState,
        r: Float,
        g: Float,
    ) {
        val vc: VertexConsumer = buffers.getBuffer(OutlineTypes.LINE_THROUGH_WALLS)

        val matrix = Matrix4f().apply {
                translate((pos.x - camera.pos.x).toFloat(), (pos.y - camera.pos.y).toFloat(), (pos.z - camera.pos.z).toFloat())
        }

        val vertices = arrayOf(
            floatArrayOf(0f, 0f, 0f), floatArrayOf(1f, 0f, 0f), floatArrayOf(1f, 1f, 0f), floatArrayOf(0f, 1f, 0f),
            floatArrayOf(0f, 0f, 1f), floatArrayOf(1f, 0f, 1f), floatArrayOf(1f, 1f, 1f), floatArrayOf(0f, 1f, 1f)
        )

        val edges = arrayOf(
            intArrayOf(0, 1), intArrayOf(1, 2), intArrayOf(2, 3), intArrayOf(3, 0),
            intArrayOf(4, 5), intArrayOf(5, 6), intArrayOf(6, 7), intArrayOf(7, 4),
            intArrayOf(0, 4), intArrayOf(1, 5), intArrayOf(2, 6), intArrayOf(3, 7)
        )

        for (edge in edges) {
            val v1 = vertices[edge[0]]
            val v2 = vertices[edge[1]]

            val v1f = Vector3f(v1[0], v1[1], v1[2])
            val v2f = Vector3f(v2[0], v2[1], v2[2])
            val edgeNormal = Vector3f(v2f).sub(v1f).normalize()

            vc.addVertex(matrix, v1[0], v1[1], v1[2])
                .setColor(r, g, 0f, 1f)
                .setNormal(edgeNormal.x(), edgeNormal.y(), edgeNormal.z())
                /*? if = 1.21.11 {*/.setLineWidth(2f) /*?}*/
            vc.addVertex(matrix, v2[0], v2[1], v2[2])
                .setColor(r, g, 0f, 1f)
                .setNormal(edgeNormal.x(), edgeNormal.y(), edgeNormal.z())
                /*? if = 1.21.11 {*/.setLineWidth(2f) /*?}*/
        }
    }

    private fun renderText(
        buffers: MultiBufferSource,
        pos: BlockPos,
        text: String,
        camera: CameraRenderState,
        color: Int
    ){
        val matrix = Matrix4f().apply {
            translate(((pos.x  + 0.5 - camera.pos.x).toFloat()), ((pos.y + 2.25 - camera.pos.y).toFloat()), ((pos.z + 0.5 - camera.pos.z).toFloat()))
            rotate(camera.orientation)
            scale(0.03f, -0.03f, 0.03f)
        }

        val fr: Font = Minecraft.getInstance().font
        val offset = -fr.width(text) / 2f
        val glyphs: Font.PreparedText = fr.prepareText(text, offset, 0f, color, false,LightTexture.FULL_BRIGHT)
        glyphs.visit(
            Font.GlyphVisitor.forMultiBufferSource(
                buffers,
                matrix,
                Font.DisplayMode.SEE_THROUGH,
                LightTexture.FULL_BRIGHT
            )
        )
    }

    private fun drawLinetoBlock(
        buffers: MultiBufferSource,
        blockPos: BlockPos,
        camera: CameraRenderState,
    ) {
        val o = camera.orientation
        val rotation = Quaternionf(o.x, o.y, o.z, o.w)
        val forward = Vector3f(0f, 0f, -1f).rotate(rotation)

        val sx = (forward.x() * 0.5f)
        val sy = (forward.y() * 0.5f)
        val sz = (forward.z() * 0.5f)
        val ex = (blockPos.x + 0.5 - camera.pos.x).toFloat()
        val ey = (blockPos.y + 1.0 - camera.pos.y).toFloat()
        val ez = (blockPos.z + 0.5 - camera.pos.z).toFloat()

        val vc = buffers.getBuffer(OutlineTypes.LINE_THROUGH_WALLS)

        val matrix = Matrix4f().apply {
            translate(sx, sy, sz)
        }
        val normal = Vector3f(ex - sx, ey - sy, ez - sz).normalize()

        vc.addVertex(matrix, sx, sy, sz)
            .setColor(0f, 1f, 0f, 1f)
            .setNormal(normal.x(), normal.y(), normal.z())
        /*? if = 1.21.11 {*/.setLineWidth(2f) /*?}*/

        vc.addVertex(matrix, ex, ey, ez)
            .setColor(0f, 1f, 0f, 1f)
            .setNormal(normal.x(), normal.y(), normal.z())
        /*? if = 1.21.11 {*/.setLineWidth(2f) /*?}*/
    }

    fun renderBlockHighlight(
        buffers: MultiBufferSource,
        pos: BlockPos,
        camera: CameraRenderState,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float = 1f,
    ) {
        val vc: VertexConsumer = buffers.getBuffer(OutlineTypes.HIGHLIGHT)

        val minX = pos.x.toFloat() - 0.001f
        val minY = pos.y.toFloat() - 0.001f
        val minZ = pos.z.toFloat() - 0.001f
        val maxX = minX + 1f + 0.002f
        val maxY = minY + 1f + 0.002f
        val maxZ = minZ + 1f + 0.002f

        val posMatrix = Matrix4f().apply {
            translate((-camera.pos.x).toFloat(),(-camera.pos.y).toFloat(),(-camera.pos.z).toFloat())
        }
        // front (+Z)
        vc.addVertex(posMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha)
        vc.addVertex(posMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha)
        vc.addVertex(posMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha)
        vc.addVertex(posMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha)

        // back (-Z)
        vc.addVertex(posMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha)
        vc.addVertex(posMatrix, minX, minY, minZ).setColor(red, green, blue, alpha)
        vc.addVertex(posMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha)
        vc.addVertex(posMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha)

        // left (-X)
        vc.addVertex(posMatrix, minX, minY, minZ).setColor(red, green, blue, alpha)
        vc.addVertex(posMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha)
        vc.addVertex(posMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha)
        vc.addVertex(posMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha)

        // right (+X)
        vc.addVertex(posMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha)
        vc.addVertex(posMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha)
        vc.addVertex(posMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha)
        vc.addVertex(posMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha)

        // top (+Y)
        vc.addVertex(posMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha)
        vc.addVertex(posMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha)
        vc.addVertex(posMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha)
        vc.addVertex(posMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha)

        // bottom (-Y)
        vc.addVertex(posMatrix, minX, minY, minZ).setColor(red, green, blue, alpha)
        vc.addVertex(posMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha)
        vc.addVertex(posMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha)
        vc.addVertex(posMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha)
    }
}