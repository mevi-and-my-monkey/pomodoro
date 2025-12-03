package com.mevi.pomodoro

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
// Importación que faltaba
import com.mevi.pomodoro.PomodoroState

class PomodoroViewModel : ViewModel() {

    private var timerService: TimerService? = null
    private var isBound = false

    private val _time = MutableStateFlow(25 * 60)
    val time = _time.asStateFlow()

    private val _pomodoroState = MutableStateFlow(PomodoroState.WORKING)
    val pomodoroState = _pomodoroState.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            isBound = true

            // Suscribirse a los cambios del servicio
            viewModelScope.launch {
                timerService?.time?.collect { _time.value = it }
            }
            viewModelScope.launch {
                timerService?.pomodoroState?.collect { _pomodoroState.value = it }
            }
            viewModelScope.launch {
                timerService?.isRunning?.collect { _isRunning.value = it }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            timerService = null
            isBound = false
        }
    }

    fun startStop() {
        timerService?.startStop()
    }

    fun reset() {
        timerService?.resetTimer()
    }

    fun bindService(context: Context) {
        Intent(context, TimerService::class.java).also { intent ->
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun unbindService(context: Context) {
        if (isBound) {
            context.unbindService(connection)
            isBound = false
        }
    }
}