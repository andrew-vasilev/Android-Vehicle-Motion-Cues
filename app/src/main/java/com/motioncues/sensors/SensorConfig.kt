package com.motioncues.sensors

data class SensorConfig(
    val dotSizeDp: Float = 6f,
    val dotAlpha: Float = 0.4f,
    val dotSpacingDp: Float = 40f,
    val edgeMarginDp: Float = 20f,
    val sensitivity: Float = 1f,
    val lowPassAlpha: Float = 0.2f,
    val springStiffness: Float = 300f,
    val springDamping: Float = 0.7f,
    val maxOffsetDp: Float = 24f,
)
