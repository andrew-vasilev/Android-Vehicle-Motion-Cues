package com.motioncues.activity

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity

class ActivityRecognitionManager(private val context: Context) {

    private val tag = "ActivityRecognition"

    private val transitionRequest = ActivityTransitionRequest(
        listOf(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.IN_VEHICLE)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build(),
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.IN_VEHICLE)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build(),
        )
    )

    private val pendingIntent: PendingIntent by lazy {
        PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(context, TransitionReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    var onVehicleStateChanged: ((inVehicle: Boolean) -> Unit)? = null

    private var isInVehicle = false

    fun start() {
        val filter = IntentFilter(ACTION_TRANSITION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(TransitionReceiver(), filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(TransitionReceiver(), filter)
        }

        ActivityRecognition.getClient(context)
            .requestActivityTransitionUpdates(transitionRequest, pendingIntent)
            .addOnSuccessListener { Log.d(tag, "Transition updates registered") }
            .addOnFailureListener { e -> Log.e(tag, "Failed to register: ${e.message}") }
    }

    fun stop() {
        try {
            ActivityRecognition.getClient(context)
                .removeActivityTransitionUpdates(pendingIntent)
            context.unregisterReceiver(TransitionReceiver())
        } catch (_: Exception) {
        }
    }

    class TransitionReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != ACTION_TRANSITION) return

            val result = com.google.android.gms.location.ActivityTransitionResult.extractResult(intent)
            result?.transitionEvents?.forEach { event ->
                val inVehicle = event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER
                val intentBroadcast = Intent(ACTION_VEHICLE_STATE)
                intentBroadcast.setPackage(context.packageName)
                intentBroadcast.putExtra(EXTRA_IN_VEHICLE, inVehicle)
                context.sendBroadcast(intentBroadcast)
            }
        }
    }

    inner class VehicleStateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_VEHICLE_STATE) {
                val entered = intent.getBooleanExtra(EXTRA_IN_VEHICLE, false)
                isInVehicle = entered
                onVehicleStateChanged?.invoke(entered)
            }
        }
    }

    private var stateReceiver: VehicleStateReceiver? = null

    fun startListening() {
        stateReceiver = VehicleStateReceiver()
        val filter = IntentFilter(ACTION_VEHICLE_STATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(stateReceiver!!, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(stateReceiver!!, filter)
        }
    }

    fun stopListening() {
        stateReceiver?.let {
            try { context.unregisterReceiver(it) } catch (_: Exception) {}
        }
        stateReceiver = null
    }

    companion object {
        const val ACTION_TRANSITION = "com.motioncues.TRANSITION"
        const val ACTION_VEHICLE_STATE = "com.motioncues.VEHICLE_STATE"
        const val EXTRA_IN_VEHICLE = "in_vehicle"
        const val REQUEST_CODE = 1001
    }
}
