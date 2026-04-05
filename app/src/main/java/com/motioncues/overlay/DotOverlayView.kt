package com.motioncues.overlay

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.motioncues.sensors.SensorConfig

class DotOverlayView(context: Context) : View(context.applicationContext) {

    private var config = SensorConfig()
    private var lateralOffset: Float = 0f
    private var longitudinalOffset: Float = 0f

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = (config.dotAlpha * 255).toInt()
    }

    private val density: Float
        get() = resources.displayMetrics.density

    private val isDarkTheme: Boolean
        get() = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        dotPaint.color = if (isDarkTheme) Color.WHITE else Color.BLACK
        dotPaint.alpha = (config.dotAlpha * 255).toInt()

        val dotRadiusPx = config.dotSizeDp * density
        val edgeMarginPx = config.edgeMarginDp * density
        val dotSpacingPx = config.dotSpacingDp * density

        val w = width.toFloat()
        val h = height.toFloat()

        val dotsPerSide = config.dotCount

        val latPx = lateralOffset * density
        val lonPx = longitudinalOffset * density

        val usableHeight = h - 2 * edgeMarginPx
        val spacing = if (dotsPerSide > 1) usableHeight / (dotsPerSide - 1) else 0f

        for (i in 0 until dotsPerSide) {
            val baseY = edgeMarginPx + i * spacing
            val y = (baseY + lonPx).coerceIn(edgeMarginPx, h - edgeMarginPx)

            val leftX = (edgeMarginPx + latPx).coerceIn(edgeMarginPx, w / 2f)
            canvas.drawCircle(leftX, y, dotRadiusPx, dotPaint)

            val rightX = (w - edgeMarginPx - latPx).coerceIn(w / 2f, w - edgeMarginPx)
            canvas.drawCircle(rightX, y, dotRadiusPx, dotPaint)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        requestLayout()
        invalidate()
    }

    fun updateOffsets(lateral: Float, longitudinal: Float) {
        lateralOffset = lateral
        longitudinalOffset = longitudinal
        invalidate()
    }

    fun updateConfig(newConfig: SensorConfig) {
        config = newConfig
        dotPaint.alpha = (newConfig.dotAlpha * 255).toInt()
        invalidate()
    }
}
