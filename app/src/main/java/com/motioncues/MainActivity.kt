package com.motioncues

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.motioncues.service.MotionCuesService
import com.motioncues.ui.screens.MainScreen
import com.motioncues.ui.screens.OverlayMode
import com.motioncues.ui.theme.MotionCuesTheme

class MainActivity : ComponentActivity() {

    private var hasOverlayPermission: Boolean = false
    private var hasNotificationPermission: Boolean = false
    private var hasActivityRecognitionPermission: Boolean = false

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshPermissions()
        render()
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        refreshPermissions()
        render()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        refreshPermissions()
        render()
    }

    override fun onResume() {
        super.onResume()
        refreshPermissions()
        render()
    }

    private fun refreshPermissions() {
        hasOverlayPermission = Settings.canDrawOverlays(this)
        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
        hasActivityRecognitionPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun allPermissionsGranted(): Boolean =
        hasOverlayPermission && hasNotificationPermission && hasActivityRecognitionPermission

    private fun requestPermissions() {
        if (!hasOverlayPermission) {
            overlayPermissionLauncher.launch(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        }
        val runtimePerms = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            runtimePerms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (!hasActivityRecognitionPermission) {
            runtimePerms.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        if (runtimePerms.isNotEmpty()) {
            permissionLauncher.launch(runtimePerms.toTypedArray())
        }
    }

    private fun startService() {
        startForegroundService(
            Intent(this, MotionCuesService::class.java).apply {
                action = MotionCuesService.ACTION_START
            }
        )
    }

    private fun startAutoService() {
        startForegroundService(
            Intent(this, MotionCuesService::class.java).apply {
                action = MotionCuesService.ACTION_START_AUTO
            }
        )
    }

    private fun stopService() {
        startService(
            Intent(this, MotionCuesService::class.java).apply {
                action = MotionCuesService.ACTION_STOP
            }
        )
    }

    private fun render() {
        setContent {
            MotionCuesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        allPermissionsGranted = allPermissionsGranted(),
                        hasOverlayPermission = hasOverlayPermission,
                        onRequestPermissions = { requestPermissions() },
                        onStartService = { startService() },
                        onStartAutoService = { startAutoService() },
                        onStopService = { stopService() },
                    )
                }
            }
        }
    }
}
