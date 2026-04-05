package com.motioncues.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class OverlayMode { OFF, ON, AUTO }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    allPermissionsGranted: Boolean,
    hasOverlayPermission: Boolean,
    onRequestPermissions: () -> Unit,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
) {
    var mode by remember { mutableStateOf(OverlayMode.OFF) }
    var sensitivity by remember { mutableFloatStateOf(1f) }
    var dotAlpha by remember { mutableFloatStateOf(0.4f) }
    var dotSize by remember { mutableFloatStateOf(6f) }

    LaunchedEffect(mode) {
        when (mode) {
            OverlayMode.ON -> onStartService()
            OverlayMode.OFF -> onStopService()
            OverlayMode.AUTO -> onStartService()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Motion Cues") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PermissionCard(
                allGranted = allPermissionsGranted,
                onRequest = onRequestPermissions,
            )

            Text("Mode", style = MaterialTheme.typography.titleMedium)
            ModeSelector(mode = mode, onModeChange = { mode = it })

            Text("Settings", style = MaterialTheme.typography.titleMedium)
            SettingSlider(
                label = "Sensitivity",
                value = sensitivity,
                onValueChange = { sensitivity = it },
                valueRange = 0.2f..2f,
            )
            SettingSlider(
                label = "Dot Opacity",
                value = dotAlpha,
                onValueChange = { dotAlpha = it },
                valueRange = 0.1f..0.9f,
            )
            SettingSlider(
                label = "Dot Size",
                value = dotSize,
                onValueChange = { dotSize = it },
                valueRange = 2f..16f,
            )
        }
    }
}

@Composable
private fun PermissionCard(
    allGranted: Boolean,
    onRequest: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (allGranted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (allGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (allGranted) "All permissions granted"
                    else "Permissions required",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            if (!allGranted) {
                Button(onClick = onRequest) {
                    Text("Grant")
                }
            }
        }
    }
}

@Composable
private fun ModeSelector(
    mode: OverlayMode,
    onModeChange: (OverlayMode) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OverlayMode.entries.forEach { m ->
            val selected = mode == m
            if (selected) {
                Button(
                    onClick = { onModeChange(m) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(m.name)
                }
            } else {
                OutlinedButton(
                    onClick = { onModeChange(m) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                ) {
                    Text(m.name)
                }
            }
        }
    }
}

@Composable
private fun SettingSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label)
            Text(
                String.format("%.1f", value),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
