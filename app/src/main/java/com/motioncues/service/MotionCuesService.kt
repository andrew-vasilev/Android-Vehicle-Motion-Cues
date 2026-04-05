package com.motioncues.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.motioncues.MainActivity
import com.motioncues.MotionCuesApp
import com.motioncues.activity.ActivityRecognitionManager
import com.motioncues.overlay.DotOverlayManager
import com.motioncues.sensors.SensorDataProcessor
import com.motioncues.sensors.SettingsStore

class MotionCuesService : android.app.Service() {

    private var overlayManager: DotOverlayManager? = null
    private var sensorProcessor: SensorDataProcessor? = null
    private var activityRecognition: ActivityRecognitionManager? = null

    private var isAutoMode = false
    private var isOverlayShown = false

    private val configListener: (com.motioncues.sensors.SensorConfig) -> Unit = { config ->
        overlayManager?.updateConfig(config)
    }

    override fun onCreate() {
        super.onCreate()
        overlayManager = DotOverlayManager(this)
        sensorProcessor = SensorDataProcessor(this) { lateral, longitudinal ->
            overlayManager?.updateOffsets(lateral, longitudinal)
        }
        activityRecognition = ActivityRecognitionManager(this)
        activityRecognition?.onVehicleStateChanged = { inVehicle ->
            if (inVehicle && !isOverlayShown) {
                showOverlay()
            } else if (!inVehicle && isOverlayShown && isAutoMode) {
                hideOverlay()
            }
        }
        SettingsStore.onConfigChanged = configListener
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                isAutoMode = false
                startForegroundAndShow()
            }
            ACTION_START_AUTO -> {
                isAutoMode = true
                startForegroundAndListen()
            }
            ACTION_STOP -> stopOverlay()
            ACTION_UPDATE_CONFIG -> {
                overlayManager?.updateConfig(SettingsStore.config)
            }
        }
        return START_STICKY
    }

    private fun startForegroundAndShow() {
        val notification = createNotification("Overlay is active")
        startForegroundCompat(notification)
        activityRecognition?.stop()
        activityRecognition?.stopListening()
        showOverlay()
    }

    private fun startForegroundAndListen() {
        val notification = createNotification("Auto mode — waiting for vehicle")
        startForegroundCompat(notification)
        activityRecognition?.start()
        activityRecognition?.startListening()
    }

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun showOverlay() {
        if (isOverlayShown) return
        overlayManager?.updateConfig(SettingsStore.config)
        overlayManager?.show()
        sensorProcessor?.start()
        isOverlayShown = true
        updateNotification(if (isAutoMode) "Auto — in vehicle" else "Overlay is active")
    }

    private fun hideOverlay() {
        if (!isOverlayShown) return
        sensorProcessor?.stop()
        overlayManager?.hide()
        isOverlayShown = false
        if (isAutoMode) {
            updateNotification("Auto mode — waiting for vehicle")
        }
    }

    private fun stopOverlay() {
        activityRecognition?.stop()
        activityRecognition?.stopListening()
        sensorProcessor?.stop()
        overlayManager?.hide()
        isOverlayShown = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateNotification(text: String) {
        val notification = createNotification(text)
        val nm = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        activityRecognition?.stop()
        activityRecognition?.stopListening()
        sensorProcessor?.stop()
        overlayManager?.hide()
        SettingsStore.onConfigChanged = null
        overlayManager = null
        sensorProcessor = null
        activityRecognition = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun createNotification(text: String = "Overlay is active"): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, MotionCuesApp.CHANNEL_ID)
            .setContentTitle("Motion Cues")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "com.motioncues.action.START"
        const val ACTION_START_AUTO = "com.motioncues.action.START_AUTO"
        const val ACTION_STOP = "com.motioncues.action.STOP"
        const val ACTION_UPDATE_CONFIG = "com.motioncues.action.UPDATE_CONFIG"
    }
}
