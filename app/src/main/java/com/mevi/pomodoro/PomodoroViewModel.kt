package com.mevi.pomodoro

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class PomodoroState {
    WORKING,
    BREAK
}

class PomodoroViewModel : ViewModel() {

    private val _pomodoroState = mutableStateOf(PomodoroState.WORKING)
    val pomodoroState: State<PomodoroState> = _pomodoroState

    private val _time = mutableStateOf(25 * 60)
    val time: State<Int> = _time

    private val _isRunning = mutableStateOf(false)
    val isRunning: State<Boolean> = _isRunning

    private var timerJob: Job? = null

    fun startStop() {
        if (_isRunning.value) {
            timerJob?.cancel()
            _isRunning.value = false
        } else {
            _isRunning.value = true
            timerJob = viewModelScope.launch {
                while (_time.value > 0) {
                    delay(1000)
                    _time.value--
                }
                // When time is up, change state
                if (_pomodoroState.value == PomodoroState.WORKING) {
                    _pomodoroState.value = PomodoroState.BREAK
                    _time.value = 5 * 60 // 5 minutes break
                } else {
                    _pomodoroState.value = PomodoroState.WORKING
                    _time.value = 25 * 60 // 25 minutes work
                }
                _isRunning.value = false
            }
        }
    }

    fun reset() {
        timerJob?.cancel()
        _isRunning.value = false
        _pomodoroState.value = PomodoroState.WORKING
        _time.value = 25 * 60
    }
}
