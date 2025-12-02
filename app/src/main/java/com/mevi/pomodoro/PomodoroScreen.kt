package com.mevi.pomodoro

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PomodoroScreen(pomodoroViewModel: PomodoroViewModel = viewModel()) {
    val time by pomodoroViewModel.time
    val pomodoroState by pomodoroViewModel.pomodoroState
    val isRunning by pomodoroViewModel.isRunning

    val isInitialComposition = remember { mutableStateOf(true) }
    val toneGen = remember { ToneGenerator(AudioManager.STREAM_ALARM, 100) }

    DisposableEffect(Unit) {
        onDispose {
            toneGen.release()
        }
    }

    LaunchedEffect(pomodoroState) {
        if (isInitialComposition.value) {
            isInitialComposition.value = false
        } else {
            val toneType = when (pomodoroState) {
                PomodoroState.BREAK -> ToneGenerator.TONE_CDMA_HIGH_L
                PomodoroState.WORKING -> ToneGenerator.TONE_SUP_RADIO_ACK
            }
            toneGen.startTone(toneType, 200)
        }
    }

    val imageResource = when (pomodoroState) {
        PomodoroState.WORKING -> R.drawable.pomodoro_dog_one
        PomodoroState.BREAK -> R.drawable.pomodoro_dog_three
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = imageResource),
            contentDescription = "Cute Maltese dog",
            modifier = Modifier.size(450.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        when (pomodoroState) {
            PomodoroState.WORKING -> {
                CircularTimer(
                    time = time,
                    isRunning = isRunning,
                    onStartStop = { pomodoroViewModel.startStop() },
                    onReset = { pomodoroViewModel.reset() }
                )
            }
            PomodoroState.BREAK -> {
                Text(
                    text = "Time for a break!",
                    style = TextStyle(
                        fontFamily = FontFamily.Cursive,
                        fontSize = 32.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun CircularTimer(
    time: Int,
    isRunning: Boolean,
    onStartStop: () -> Unit,
    onReset: () -> Unit
) {
    val animatedProgress by animateFloatAsState(targetValue = time / (25f * 60f), label = "")
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(300.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 20f
            drawArc(
                color = Color.LightGray,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "%02d:%02d".format(time / 60, time % 60), fontSize = 60.sp)
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onStartStop, colors = ButtonDefaults.buttonColors(containerColor = primaryColor)) {
                Text(text = if (isRunning) "Stop" else "Start")
            }
            Button(onClick = onReset, colors = ButtonDefaults.buttonColors(containerColor = primaryColor)) {
                Text(text = "Reset")
            }
        }
    }
}
