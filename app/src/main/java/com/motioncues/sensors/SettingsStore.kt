package com.motioncues.sensors

import com.motioncues.overlay.DotOverlayManager

object SettingsStore {
    var config: SensorConfig = SensorConfig()
        private set

    var onConfigChanged: ((SensorConfig) -> Unit)? = null

    fun update(update: SensorConfig.() -> SensorConfig) {
        config = config.update()
        onConfigChanged?.invoke(config)
    }

    fun updateDotAlpha(alpha: Float) {
        config = config.copy(dotAlpha = alpha)
        onConfigChanged?.invoke(config)
    }

    fun updateDotSize(size: Float) {
        config = config.copy(dotSizeDp = size)
        onConfigChanged?.invoke(config)
    }

    fun updateSensitivity(sensitivity: Float) {
        config = config.copy(sensitivity = sensitivity)
        onConfigChanged?.invoke(config)
    }
}
