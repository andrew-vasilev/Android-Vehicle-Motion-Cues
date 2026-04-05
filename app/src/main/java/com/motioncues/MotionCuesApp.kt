package com.motioncues

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class MotionCuesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Motion Cues Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps the motion cues overlay running"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "motion_cues_service"
    }
}
