package com.motioncues.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.view.WindowManager
import com.motioncues.sensors.SensorConfig

class DotOverlayManager(private val context: Context) {

    private val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: DotOverlayView? = null

    fun show() {
        if (overlayView != null) return

        overlayView = DotOverlayView(context)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        windowManager.addView(overlayView, params)
    }

    fun hide() {
        overlayView?.let { windowManager.removeView(it) }
        overlayView = null
    }

    fun updateOffsets(lateralOffset: Float, longitudinalOffset: Float) {
        overlayView?.updateOffsets(lateralOffset, longitudinalOffset)
    }

    fun updateConfig(config: SensorConfig) {
        overlayView?.updateConfig(config)
    }
}
