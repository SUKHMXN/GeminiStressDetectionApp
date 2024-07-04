package com.varunkumar.geminiapi.presentation.features.home_feature

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varunkumar.geminiapi.model.StressModelApi
import com.varunkumar.geminiapi.presentation.HealthSensors
import com.varunkumar.geminiapi.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val stressModelApi: StressModelApi,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    fun sliderChange(newValue: Float, sensor: HealthSensors) {
        when (sensor) {
            is HealthSensors.SnoringRateSensors -> {
                _state.update {
                    it.copy(sensorValues = it.sensorValues.copy(snoringRate = newValue))
                }
            }

            is HealthSensors.RespirationRateSensors -> {
                _state.update {
                    it.copy(sensorValues = it.sensorValues.copy(respirationRate = newValue))
                }
            }

            is HealthSensors.HoursOfSleepSensors -> {
                _state.update {
                    it.copy(sensorValues = it.sensorValues.copy(sleepHours = newValue))
                }
            }
        }
    }

    fun onChangeImageUri(newBitmap: Bitmap?) {
        _state.update { it.copy(image = newBitmap) }
        savedStateHandle["image"] = newBitmap
    }

    fun predictStress() {
        _state.update { it.copy(stateResult = Result.Loading()) }

        viewModelScope.launch {
            try {
                val response = stressModelApi.getStressLevel(
                    snoringRange = _state.value.sensorValues.snoringRate,
                    respirationRate = _state.value.sensorValues.respirationRate,
                    sleep = _state.value.sensorValues.sleepHours
                )

                _state.update {
                    it.copy(
                        stateResult = Result.Success(
                            response.body()?.stressLevel ?: "Not Defined"
                        )
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(stateResult = Result.Error(e.message)) }
                Log.d("error", e.toString())
            }
        }
    }
}

data class SensorValues(
    var snoringRate: Float = 50f,
    var respirationRate: Float = 50f,
    var sleepHours: Float = 50f
)