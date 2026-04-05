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
import com.motioncues.overlay.DotOverlayManager

class MotionCuesService : android.app.Service() {

    private var overlayManager: DotOverlayManager? = null

    override fun onCreate() {
        super.onCreate()
        overlayManager = DotOverlayManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startOverlay()
            ACTION_STOP -> stopOverlay()
        }
        return START_STICKY
    }

    private fun startOverlay() {
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        overlayManager?.show()
    }

    private fun stopOverlay() {
        overlayManager?.hide()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        overlayManager?.hide()
        overlayManager = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, MotionCuesApp.CHANNEL_ID)
            .setContentTitle("Motion Cues")
            .setContentText("Overlay is active")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "com.motioncues.action.START"
        const val ACTION_STOP = "com.motioncues.action.STOP"
    }
}
