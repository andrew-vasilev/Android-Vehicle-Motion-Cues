package com.motioncues.sensors

data class SensorConfig(
    val dotSizeDp: Float = 6f,
    val dotAlpha: Float = 0.4f,
    val dotSpacingDp: Float = 40f,
    val edgeMarginDp: Float = 20f,
    val sensitivity: Float = 1.5f,
    val lowPassAlpha: Float = 0.35f,
    val springStiffness: Float = 120f,
    val springDamping: Float = 0.6f,
    val maxOffsetDp: Float = 50f,
)
