package com.varunkumar.geminiapi.presentation.features.home_feature

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.varunkumar.geminiapi.model.StressModelApi
import com.varunkumar.geminiapi.presentation.HealthSensors
import com.varunkumar.geminiapi.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val stressModelApi: StressModelApi,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    @OptIn(ExperimentalCoroutinesApi::class)
    val state = _state.flatMapLatest {
        Log.d("state change nothing", _state.value.toString())
        _state
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), HomeState())

    val showAlert = MutableStateFlow(false)

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

    fun onChangeAlertDialog() {
        showAlert.value = !showAlert.value
    }

    fun onChangeImageUri(newBitmap: Bitmap?) {
        viewModelScope.launch {
            _state.update { it.copy(image = newBitmap) }
            savedStateHandle["image"] = newBitmap

            // TODO use this image
            _state.value.image?.let { bitmap ->
                detectFaceFromBitmap(bitmap)
            }
        }
    }

    fun predictStress() {
        _state.update { it.copy(stateResult = Result.Loading()) }

        if (_state.value.isFaceDetected) {
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
        } else {
            _state.update {
                it.copy(
                    stateResult = Result.Error("No face detected")
                )
            }
        }
    }

    private suspend fun detectFaceFromBitmap(inputBitmap: Bitmap?): Bitmap? {
        var outputBitmap: Bitmap? = null

        return inputBitmap?.let { bitmap ->
            val inputImage = InputImage.fromBitmap(inputBitmap, 0)
            val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .build()
            val detector = FaceDetection.getClient(options)

            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        val face = faces[0]
                        val bounds = face.boundingBox
                        outputBitmap = cropAndResizeBitmap(bitmap, bounds, 200, 200)
                        _state.update { it.copy(isFaceDetected = true) }
                        Log.d("nothing face", "$outputBitmap")
                    }
                }
                .addOnFailureListener { e ->
                    _state.update { it.copy(isFaceDetected = false) }
                    Log.e("error", "$e")
                }.await()

            outputBitmap
        }
    }

    private fun cropAndResizeBitmap(bitmap: Bitmap, bounds: Rect, width: Int, height: Int): Bitmap {
        val croppedBitmap = Bitmap.createBitmap(
            bitmap,
            bounds.left,
            bounds.top,
            bounds.width(),
            bounds.height()
        )

        return Bitmap.createScaledBitmap(croppedBitmap, width, height, true)
    }
}

data class SensorValues(
    var snoringRate: Float = 50f,
    var respirationRate: Float = 50f,
    var sleepHours: Float = 50f
)