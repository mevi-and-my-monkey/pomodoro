package com.mevi.pomodoro

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mevi.pomodoro.R

@Composable
fun PomodoroScreen(pomodoroViewModel: PomodoroViewModel = viewModel()) {
    val time by pomodoroViewModel.time
    val pomodoroState by pomodoroViewModel.pomodoroState
    val isRunning by pomodoroViewModel.isRunning

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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
                BreakScreen()
            }
        }
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

@Composable
fun BreakScreen() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Time for a break!", fontSize = 30.sp)
        Spacer(modifier = Modifier.height(20.dp))
        // Placeholder for the cute dog image. 
        // Recommended: Use a soft, dreamy, warm-lit illustration or vector art.
        Image(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = "Cute dog")
    }
}
