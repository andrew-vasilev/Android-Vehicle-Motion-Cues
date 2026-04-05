package com.motioncues.sensors

object SettingsStore {
    var config: SensorConfig = SensorConfig()
        private set

    var onConfigChanged: ((SensorConfig) -> Unit)? = null

    fun updateDotAlpha(alpha: Float) {
        config = config.copy(dotAlpha = alpha)
        onConfigChanged?.invoke(config)
    }

    fun updateDotSize(size: Float) {
        config = config.copy(dotSizeDp = size)
        onConfigChanged?.invoke(config)
    }

    fun updateDotCount(count: Int) {
        config = config.copy(dotCount = count)
        onConfigChanged?.invoke(config)
    }

    fun updateSensitivity(sensitivity: Float) {
        config = config.copy(sensitivity = sensitivity)
        onConfigChanged?.invoke(config)
    }
}
