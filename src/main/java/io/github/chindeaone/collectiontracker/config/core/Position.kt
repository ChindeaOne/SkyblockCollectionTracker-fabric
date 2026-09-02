package io.github.chindeaone.collectiontracker.config.core

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.utils.rendering.ScaleUtils.scaledHeight
import io.github.chindeaone.collectiontracker.utils.rendering.ScaleUtils.scaledWidth
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import java.lang.reflect.Field
import kotlin.math.roundToInt

class Position(@field:Expose var x: Int, @field:Expose var y: Int) {
    @Expose
    var scale: Float = 1.0f
        private set

    @Expose
    var width: Int = 100
        private set

    @Expose
    var height: Int = 20
        private set

    @Transient
    var link: Field? = null

    fun setLink(configLink: ConfigLink) {
        try {
            link = configLink.owner.java.getDeclaredField(configLink.field)
        } catch (_: NoSuchFieldException) {
            System.err.println("Failed to set ConfigLink for " + configLink.field + " in " + configLink.owner)
        }
    }

    fun setPosition(x: Int, y: Int) {
        val screenWidth = scaledWidth
        val screenHeight = scaledHeight

        val yPadding = 4
        val scaledYPadding = (yPadding * scale).roundToInt()

        val scaledWidth = (width * scale).roundToInt()
        val scaledHeight = (height * scale).roundToInt()

        var maxX = screenWidth - scaledWidth
        var maxY = screenHeight - (scaledHeight + scaledYPadding)

        if (maxX < 0) maxX = 0
        if (maxY < scaledYPadding) maxY = scaledYPadding

        this.x = Math.clamp(x.toLong(), 0, maxX)
        this.y = Math.clamp(y.toLong(), scaledYPadding, maxY)
    }

    fun setScaling(scale: Float) {
        this.scale = scale
        setPosition(x, y)
    }

    fun setDimensions(width: Int, height: Int) {
        this.width = width
        this.height = height
        setPosition(x, y)
    }
}
