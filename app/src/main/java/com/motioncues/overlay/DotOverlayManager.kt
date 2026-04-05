package com.motioncues.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.view.WindowManager
import com.motioncues.sensors.SensorConfig

class DotOverlayManager(private val context: Context) {

    private val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: DotOverlayView? = null

    private var currentConfig: SensorConfig = SensorConfig()
    private var currentLateral: Float = 0f
    private var currentLongitudinal: Float = 0f

    fun show() {
        if (overlayView != null) return
        addOverlay()
    }

    private fun addOverlay() {
        overlayView = DotOverlayView(context).apply {
            updateConfig(currentConfig)
            updateOffsets(currentLateral, currentLongitudinal)
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        windowManager.addView(overlayView, params)
    }

    fun recreate() {
        overlayView?.let { windowManager.removeView(it) }
        overlayView = null
        addOverlay()
    }

    fun hide() {
        overlayView?.let { windowManager.removeView(it) }
        overlayView = null
    }

    fun updateOffsets(lateralOffset: Float, longitudinalOffset: Float) {
        currentLateral = lateralOffset
        currentLongitudinal = longitudinalOffset
        overlayView?.updateOffsets(lateralOffset, longitudinalOffset)
    }

    fun updateConfig(config: SensorConfig) {
        currentConfig = config
        overlayView?.updateConfig(config)
    }
}
