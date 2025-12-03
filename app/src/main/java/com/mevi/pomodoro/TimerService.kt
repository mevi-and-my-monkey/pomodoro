package com.mevi.pomodoro

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerService : Service() {

    private val binder = TimerBinder()
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _time = MutableStateFlow(25 * 60)
    val time = _time.asStateFlow()

    private val _pomodoroState = MutableStateFlow(PomodoroState.WORKING)
    val pomodoroState = _pomodoroState.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "PomodoroChannelV2"
    }

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = createNotification("Pomodoro session is running...")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        return START_STICKY
    }

    fun startStop() {
        if (_isRunning.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _isRunning.value = true
        timerJob = scope.launch {
            while (_time.value > 0) {
                delay(1000)
                _time.value--
                updateNotification()
            }

            vibrateStrong()
            playNotificationSound()

            if (_pomodoroState.value == PomodoroState.WORKING) {
                _pomodoroState.value = PomodoroState.BREAK
                _time.value = 5 * 60
            } else {
                _pomodoroState.value = PomodoroState.WORKING
                _time.value = 25 * 60
            }

            _isRunning.value = false
            updateNotification()
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _isRunning.value = false
    }

    fun resetTimer() {
        pauseTimer()
        _pomodoroState.value = PomodoroState.WORKING
        _time.value = 25 * 60
        updateNotification()
    }

    // ============================================================
    //  NOTIFICACIONES
    // ============================================================

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pomodoro Timer",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)

                setSound(
                    soundUri,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )

                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pomodoro")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setAutoCancel(false)
            .build()
    }

    private fun updateNotification() {
        val stateName = _pomodoroState.value.name.lowercase().replaceFirstChar { it.uppercase() }
        val text = "%s - %02d:%02d".format(stateName, _time.value / 60, _time.value % 60)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, createNotification(text))
    }

    // ============================================================
    //  EFECTOS: VIBRACIÓN + SONIDO
    // ============================================================

    @SuppressLint("ServiceCast")
    private fun vibrateStrong() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 500, 200, 500, 200, 700),
                    -1 // No repetir
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 500, 200, 500, 200, 700), -1)
        }
    }

    private fun playNotificationSound() {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(applicationContext, uri)
            ringtone.play()
        } catch (_: Exception) {
        }
    }
}