package com.mevi.pomodoro

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.mevi.pomodoro.ui.theme.PomodoroTheme

class MainActivity : ComponentActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            // Aquí puedes manejar si el usuario aceptó o no
        }

        setContent {
            PomodoroTheme {
                PermissionHandler(permissionLauncher)
            }
        }
    }
}

@Composable
fun PermissionHandler(
    permissionLauncher: ActivityResultLauncher<Array<String>>
) {
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission =
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                showDialog = true
            }
        }
    }

    if (showDialog) {
        NotificationPermissionDialog(
            onRequestPermission = {
                showDialog = false
                permissionLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.POST_NOTIFICATIONS,
                        android.Manifest.permission.VIBRATE
                    )
                )
            },
            onDismiss = {
                showDialog = false
            }
        )
    }

    // Aquí va tu app ya normal
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        PomodoroScreen()
    }
}

@Composable
fun NotificationPermissionDialog(
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text("Permitir notificaciones")
        },
        text = {
            Text(
                "Para que tu Pomodoro pueda sonar y vibrar aunque la pantalla esté apagada, " +
                        "necesitamos permiso de notificaciones. Esto garantiza que recibas alertas cuando " +
                        "termine cada ciclo."
            )
        },
        confirmButton = {
            TextButton(onClick = onRequestPermission) {
                Text("Permitir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Ahora no")
            }
        }
    )
}