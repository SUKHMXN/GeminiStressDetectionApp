package com.varunkumar.geminiapi.presentation.features.home_feature

import android.graphics.Bitmap
import com.varunkumar.geminiapi.presentation.Routes
import com.varunkumar.geminiapi.utils.Result

data class HomeState(
    val stateResult: Result<String> = Result.Idle(),
    val selectedRoute: Routes = Routes.Home,
    val sensorValues: SensorValues = SensorValues(),
    val image: Bitmap? = null
)