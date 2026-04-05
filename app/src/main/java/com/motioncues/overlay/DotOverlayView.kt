package com.motioncues.overlay

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.motioncues.sensors.SensorConfig

class DotOverlayView(context: Context) : View(context) {

    private var config = SensorConfig()
    private var lateralOffset: Float = 0f
    private var longitudinalOffset: Float = 0f

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = (config.dotAlpha * 255).toInt()
    }

    private val isDarkTheme: Boolean
        get() = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        dotPaint.color = if (isDarkTheme) Color.WHITE else Color.BLACK
        dotPaint.alpha = (config.dotAlpha * 255).toInt()

        val dotRadiusPx = config.dotSizeDp * resources.displayMetrics.density
        val edgeMarginPx = config.edgeMarginDp * resources.displayMetrics.density
        val dotSpacingPx = config.dotSpacingDp * resources.displayMetrics.density

        val w = width.toFloat()
        val h = height.toFloat()

        val dotsPerSide = ((h - 2 * edgeMarginPx) / dotSpacingPx).toInt().coerceAtLeast(1)

        val latPx = lateralOffset * resources.displayMetrics.density
        val lonPx = longitudinalOffset * resources.displayMetrics.density

        for (i in 0..dotsPerSide) {
            val baseY = edgeMarginPx + i * dotSpacingPx
            val y = (baseY + lonPx).coerceIn(edgeMarginPx, h - edgeMarginPx)

            val leftX = (edgeMarginPx + latPx).coerceIn(edgeMarginPx, w / 2f)
            canvas.drawCircle(leftX, y, dotRadiusPx, dotPaint)

            val rightX = (w - edgeMarginPx - latPx).coerceIn(w / 2f, w - edgeMarginPx)
            canvas.drawCircle(rightX, y, dotRadiusPx, dotPaint)
        }
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
