package com.motioncues.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Display
import android.view.Surface
import android.view.WindowManager

class SensorDataProcessor(
    private val context: Context,
    private val onOffsetsChanged: (lateral: Float, longitudinal: Float) -> Unit,
) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val linearAccelSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val gravitySensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
    private val accelerometerSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var useFallbackAccel = false

    private var filteredLateral = 0f
    private var filteredLongitudinal = 0f

    private var gravityX = 0f
    private var gravityY = 0f
    private var gravityZ = 0f

    private var velocityLateral = 0f
    private var velocityLongitudinal = 0f

    private var lastTimestamp = 0L
    private var lastEmitTimestamp = 0L

    private val emitIntervalNs = 33_000_000L

    private val display: Display
        get() = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            .defaultDisplay

    fun start() {
        if (linearAccelSensor != null) {
            sensorManager.registerListener(
                this, linearAccelSensor, SensorManager.SENSOR_DELAY_UI
            )
            useFallbackAccel = false
        } else if (accelerometerSensor != null && gravitySensor != null) {
            sensorManager.registerListener(
                this, gravitySensor, SensorManager.SENSOR_DELAY_UI
            )
            sensorManager.registerListener(
                this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI
            )
            useFallbackAccel = true
        } else if (accelerometerSensor != null) {
            sensorManager.registerListener(
                this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI
            )
            useFallbackAccel = true
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        filteredLateral = 0f
        filteredLongitudinal = 0f
        velocityLateral = 0f
        velocityLongitudinal = 0f
        lastTimestamp = 0L
        lastEmitTimestamp = 0L
    }

    override fun onSensorChanged(event: SensorEvent) {
        val config = SettingsStore.config
        val alpha = config.lowPassAlpha

        when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                val (lat, lon) = remapAxes(event.values)
                applyFilter(lat, lon, alpha, config)
            }
            Sensor.TYPE_GRAVITY -> {
                gravityX = event.values[0]
                gravityY = event.values[1]
                gravityZ = event.values[2]
            }
            Sensor.TYPE_ACCELEROMETER -> {
                if (useFallbackAccel) {
                    val ax = event.values[0] - gravityX
                    val ay = event.values[1] - gravityY
                    val az = event.values[2] - gravityZ
                    val (lat, lon) = remapAxes(floatArrayOf(ax, ay, az))
                    applyFilter(lat, lon, alpha, config)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun remapAxes(values: FloatArray): Pair<Float, Float> {
        val rotation = display.rotation
        val x = values[0]
        val y = values[1]
        val z = values[2]

        return when (rotation) {
            Surface.ROTATION_0 -> Pair(x, y)
            Surface.ROTATION_90 -> Pair(y, -x)
            Surface.ROTATION_180 -> Pair(-x, -y)
            Surface.ROTATION_270 -> Pair(-y, x)
            else -> Pair(x, y)
        }
    }

    private fun applyFilter(
        rawLateral: Float,
        rawLongitudinal: Float,
        alpha: Float,
        config: SensorConfig,
    ) {
        filteredLateral = filteredLateral + alpha * (rawLateral - filteredLateral)
        filteredLongitudinal = filteredLongitudinal + alpha * (rawLongitudinal - filteredLongitudinal)

        val now = System.nanoTime()
        if (lastTimestamp == 0L) {
            lastTimestamp = now
            return
        }

        val dt = (now - lastTimestamp) / 1_000_000_000f
        lastTimestamp = now
        if (dt <= 0f || dt > 0.5f) return

        val stiffness = config.springStiffness
        val damping = config.springDamping
        val sensitivity = config.sensitivity
        val maxOffset = config.maxOffsetDp

        val forceLat = filteredLateral * sensitivity * 40f
        velocityLateral += (forceLat - stiffness * velocityLateral * dt - damping * velocityLateral) * dt

        val forceLon = filteredLongitudinal * sensitivity * 40f
        velocityLongitudinal += (forceLon - stiffness * velocityLongitudinal * dt - damping * velocityLongitudinal) * dt

        val lateralOffset = velocityLateral.coerceIn(-maxOffset, maxOffset)
        val longitudinalOffset = velocityLongitudinal.coerceIn(-maxOffset, maxOffset)

        if (now - lastEmitTimestamp >= emitIntervalNs) {
            lastEmitTimestamp = now
            onOffsetsChanged(lateralOffset, longitudinalOffset)
        }
    }
}
