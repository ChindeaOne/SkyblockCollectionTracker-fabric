package io.github.chindeaone.collectiontracker.utils.world

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.FilterMode
import com.mojang.blaze3d.vertex.VertexConsumer
import io.github.chindeaone.collectiontracker.api.waypointsapi.FetchWaypoints
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.utils.HypixelUtils
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.font.TextRenderable
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.core.BlockPos
import net.minecraft.util.LightCoordsUtil
import net.minecraft.world.phys.AABB
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import java.awt.Color

object BlockOutline {

    fun renderWaypoint(context: LevelRenderContext) {
        if (!HypixelUtils.isOnSkyblock) return
        if (!FetchWaypoints.hasWaypoints) return

        val currentIsland = IslandTracker.currentMiningIsland
        if (currentIsland != "Dwarven Mines" && currentIsland != "Mineshaft") return

        if (currentIsland == "Dwarven Mines" && !ConfigAccess.isMineshaftSpawnRoutesEnabled() && !ConfigAccess.isDwarvenMetalRoutesEnabled() && !ConfigAccess.isPureOresRoutesEnabled()) return
        if (currentIsland == "Mineshaft" && !ConfigAccess.isMineshaftRoutesEnabled()) return

        val camera = context.levelState().cameraRenderState

        WaypointsUtils.updateCurrentIndex()
        val category = WaypointsUtils.currentCategory ?: return
        val allWaypoints = WaypointsUtils.getWaypointsForCategory(category)
        if (allWaypoints.isEmpty()) return

        val currentIndex = WaypointsUtils.currentIndex
        if (currentIndex >= allWaypoints.size) return

        // 2nd previous waypoint -> red (only if we have approached at least 2)
        if (currentIndex >= 2) {
            val (label, pos) = allWaypoints[currentIndex - 2]
            renderBlockOutline(pos, camera, 1f, 0f)
            renderText(pos, label, camera, 0xFFFF0000.toInt())
        }

        // previous waypoint -> yellow (only if we have approached at least 1)
        if (currentIndex >= 1 && currentIndex - 1 < allWaypoints.size) {
            val (label, pos) = allWaypoints[currentIndex - 1]
            renderBlockOutline(pos, camera, 1f, 1f)
            renderText(pos, label, camera, 0xFFFFFF00.toInt())
        }

        // current target waypoint -> green
        if (currentIndex < allWaypoints.size) {
            val (label, pos) = allWaypoints[currentIndex]
            renderBlockOutline(pos, camera, 0f, 1f)
            renderText(pos, label, camera, 0xFF00FF00.toInt())
            drawLinetoBlock(pos, camera)
        }
    }

    private fun renderBlockOutline(
        pos: BlockPos,
        camera: CameraRenderState,
        r: Float,
        g: Float,
    ) {
        val vc : VertexConsumer = Renderer.getBuffer(CustomPipelines.LINE_THROUGH_WALLS)

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
                .setLineWidth(2f)
            vc.addVertex(matrix, v2[0], v2[1], v2[2])
                .setColor(r, g, 0f, 1f)
                .setNormal(edgeNormal.x(), edgeNormal.y(), edgeNormal.z())
                .setLineWidth(2f)
        }
    }

    private fun renderText(
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
        // set a background color based on Skyblocker's code
        val glyphs: Font.PreparedText = fr.prepareText(text, offset, 0f, color, false, 0)
        glyphs.visit(
            object: Font.GlyphVisitor {
                override fun acceptGlyph(glyph: TextRenderable.Styled) {
                    draw(glyph)
                }
                override fun acceptEffect(bakedGlyph: TextRenderable) {
                    draw(bakedGlyph)
                }

                private fun draw(glyph: TextRenderable) {
                    val textureSetup = TextureSetup.singleTextureWithLightmap(
                        glyph.textureView(),
                        RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST)
                    )
                    val vc: VertexConsumer = Renderer.getBuffer(RenderPipelines.TEXT_SEE_THROUGH, textureSetup)

                    glyph.render(matrix, vc, LightCoordsUtil.FULL_BRIGHT, false)
                }
            }
        )
    }

    private fun drawLinetoBlock(
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

        val vc : VertexConsumer = Renderer.getBuffer(CustomPipelines.LINE_THROUGH_WALLS)

        val matrix = Matrix4f().apply {
            translate(sx, sy, sz)
        }
        val normal = Vector3f(ex - sx, ey - sy, ez - sz).normalize()

        vc.addVertex(matrix, sx, sy, sz)
            .setColor(0f, 1f, 0f, 1f)
            .setNormal(normal.x(), normal.y(), normal.z())
            .setLineWidth(2f)

        vc.addVertex(matrix, ex, ey, ez)
            .setColor(0f, 1f, 0f, 1f)
            .setNormal(normal.x(), normal.y(), normal.z())
            .setLineWidth(2f)
    }

    fun renderBlockHighlight(
        pos: BlockPos,
        camera: CameraRenderState,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float = ConfigAccess.getHeatmapOpacity(),
    ) {
        val vc : VertexConsumer = Renderer.getBuffer(CustomPipelines.HIGHLIGHT)

        val minX = pos.x.toFloat() - 0.001f
        val minY = pos.y.toFloat() - 0.001f
        val minZ = pos.z.toFloat() - 0.001f
        val maxX = minX + 1f + 0.002f
        val maxY = minY + 1f + 0.002f
        val maxZ = minZ + 1f + 0.002f

        val posMatrix = Matrix4f().apply {
            translate((-camera.pos.x).toFloat(),(-camera.pos.y).toFloat(),(-camera.pos.z).toFloat())
        }

        drawBox(vc, posMatrix, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, alpha)
    }

    fun renderBox(
        box: AABB,
        camera: CameraRenderState,
        color: Color,
        alpha: Float = 1f,
    ) {
        val vc : VertexConsumer = Renderer.getBuffer(CustomPipelines.HIGHLIGHT)

        val posMatrix = Matrix4f().apply {
            translate((-camera.pos.x).toFloat(),(-camera.pos.y).toFloat(),(-camera.pos.z).toFloat())
        }

        val minX = box.minX.toFloat()
        val minY = box.minY.toFloat()
        val minZ = box.minZ.toFloat()
        val maxX = box.maxX.toFloat()
        val maxY = box.maxY.toFloat()
        val maxZ = box.maxZ.toFloat()

        val r = color.red / 255f
        val g = color.green / 255f
        val b = color.blue / 255f

        drawBox(vc, posMatrix, minX, minY, minZ, maxX, maxY, maxZ, r, g, b, alpha)
        if (ConfigAccess.isDrawLineToPrecisionMiningEnabled()) drawLineToBox(box, camera)
    }

    private fun drawBox(
        vc: VertexConsumer,
        posMatrix: Matrix4f,
        minX: Float, minY: Float, minZ: Float,
        maxX: Float, maxY: Float, maxZ: Float,
        red: Float, green: Float, blue: Float, alpha: Float
    ) {
        val lineWidth = 2f

        // front (+Z)
        vc.addVertex(posMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(0f, 0f, 1f).setLineWidth(lineWidth)
        vc.addVertex(posMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(0f, 0f, 1f).setLineWidth(lineWidth)
        vc.addVertex(posMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(0f, 0f, 1f).setLineWidth(lineWidth)
        vc.addVertex(posMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(0f, 0f, 1f).setLineWidth(lineWidth)

        // back (-Z)
        vc.addVertex(posMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha).setNormal(0f, 0f, -1f).setLineWidth(lineWidth)
        vc.addVertex(posMatrix, minX, minY, minZ).setColor(red, green, blue, alpha).setNormal(0f, 0f, -1f).setLineWidth(lineWidth)
        vc.addVertex(posMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(0f, 0f, -1f).setLineWidth(lineWidth)
        vc.addVertex(posMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(0f, 0f, -1f).setLineWidth(lineWidth)

        // left (-X)
        vc.addVertex(posMatrix, minX, minY, minZ).setColor(red, green, blue, alpha).setNormal(-1f, 0f, 0f).setLineWidth(lineWidth)
        vc.addVertex(posMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(-1f, 0f, 0f).setLineWidth(lineWidth)
        vc.addVertex(posMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(-1f, 0f, 0f).setLineWidth(lineWidth)
        vc.addVertex(posMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(-1f, 0f, 0f).setLineWidth(lineWidth)

        // right (+X)
        vc.addVertex(posMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(1f, 0f, 0f).setLineWidth(lineWidth)
        vc.addVertex(posMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha).setNormal(1f, 0f, 0f).setLineWidth(lineWidth)
        vc.addVertex(posMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(1f, 0f, 0f).setLineWidth(lineWidth)
        vc.addVertex(posMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(1f, 0f, 0f).setLineWidth(lineWidth)

        // top (+Y)
        vc.addVertex(posMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(0f, 1f, 0f).setLineWidth(lineWidth)
        vc.addVertex(posMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha).setNormal(0f, 1f, 0f).setLineWidth(lineWidth)
        vc.addVertex(posMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(0f, 1f, 0f).setLineWidth(lineWidth)
        vc.addVertex(posMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha).setNormal(0f, 1f, 0f).setLineWidth(lineWidth)

        // bottom (-Y)
        vc.addVertex(posMatrix, minX, minY, minZ).setColor(red, green, blue, alpha).setNormal(0f, -1f, 0f).setLineWidth(lineWidth)
        vc.addVertex(posMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha).setNormal(0f, -1f, 0f).setLineWidth(lineWidth)
        vc.addVertex(posMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(0f, -1f, 0f).setLineWidth(lineWidth)
        vc.addVertex(posMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha).setNormal(0f, -1f, 0f).setLineWidth(lineWidth)
    }

    private fun drawLineToBox(
        box: AABB,
        camera: CameraRenderState,
    ) {
        val vc : VertexConsumer = Renderer.getBuffer(CustomPipelines.LINE_THROUGH_WALLS)

        val o = camera.orientation
        val rotation = Quaternionf(o.x, o.y, o.z, o.w)
        val forward = Vector3f(0f, 0f, -1f).rotate(rotation)

        val sx = forward.x() * 0.5f
        val sy = forward.y() * 0.5f
        val sz = forward.z() * 0.5f

        val ex = ((box.minX + box.maxX) / 2 - camera.pos.x).toFloat()
        val ey = ((box.minY + box.maxY) / 2 - camera.pos.y).toFloat()
        val ez = ((box.minZ + box.maxZ) / 2 - camera.pos.z).toFloat()

        val matrix = Matrix4f().apply {
            translate(sx, sy, sz)
        }
        val normal = Vector3f(ex - sx, ey - sy, ez - sz).normalize()

        vc.addVertex(matrix, sx, sy, sz)
            .setColor(0f, 1f, 0f, 1f)
            .setNormal(normal.x(), normal.y(), normal.z())
            .setLineWidth(2f)

        vc.addVertex(matrix, ex, ey, ez)
            .setColor(0f, 1f, 0f, 1f)
            .setNormal(normal.x(), normal.y(), normal.z())
            .setLineWidth(2f)
    }
}