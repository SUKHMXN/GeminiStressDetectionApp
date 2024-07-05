package com.varunkumar.geminiapi.presentation.screens

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.varunkumar.geminiapi.presentation.HealthSensors
import com.varunkumar.geminiapi.presentation.features.home_feature.HomeState
import com.varunkumar.geminiapi.presentation.features.home_feature.HomeViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SenseScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    onDoneButtonClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.isFaceDetected) {
        Dialog(
            onDismissRequest = viewModel::onChangeAlertDialog
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null)
                Spacer(modifier = Modifier.height(5.dp))
                Text(text = "No face Detected")
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            viewModel.onChangeImageUri(bitmap)
        }
    }

    val shape = RoundedCornerShape(20.dp)
    val fModifier = Modifier.fillMaxWidth()

    Scaffold(
        modifier = modifier,
        containerColor = Color(0xffF2DBCE),
        topBar = {
            TopAppBar(
                modifier = fModifier,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                title = {
                    Text(
                        text = "How you have felt today?",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                contentPadding = PaddingValues(horizontal = 16.dp),
                containerColor = Color.Transparent
            ) {
                Button(
                    modifier = fModifier
                        .height(TextFieldDefaults.MinHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    onClick = {
                        if (state.image != null) {
                            viewModel.predictStress()

                            if (state.isFaceDetected) {
                                onDoneButtonClick()
                            } else viewModel.onChangeAlertDialog()
                        } else viewModel.onChangeAlertDialog()
                    }
                ) {
                    Text(
                        text = "Predict",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) {
        Column(
            modifier = modifier
                .padding(horizontal = 16.dp)
                .padding(it),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TopImageDetectionBox(
                modifier = fModifier
                    .weight(0.6f)
                    .clip(shape)
                    .background(if (state.image == null) Color.LightGray else Color.Black)
                    .clickable {
                        cameraLauncher.launch()
                    },
                state = state
            )

            BottomSliderBox(
                modifier = modifier
                    .weight(0.4f),
                viewModel = viewModel,
                state = state
            )
        }
//        it
//        CameraCaptureScreen()
    }
}

@Composable
fun CameraCaptureScreen() {
    val context = LocalContext.current
    val lifeCycleOwner = LocalLifecycleOwner.current
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                cameraProviderFuture.addListener({
                    val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build()
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    imageCapture = ImageCapture.Builder().build()

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifeCycleOwner, cameraSelector, preview, imageCapture
                        )
                    } catch (exc: Exception) {
                        Log.e("CameraX", "Use case binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        }

    val captureImage = {
        val outputOptions = ImageCapture.OutputFileOptions.Builder(createTempFile()).build()
        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraX", "Image capture failed", exception)
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val bitmap = BitmapFactory.decodeFile(outputFileResults.savedUri?.path)
                    // Use the bitmap
                }
            })
    }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { context ->
            PreviewView(context).apply {

            }
        }, modifier = Modifier.fillMaxSize())
        Button(onClick = { captureImage }) {
            Text("Capture Image")
        }
    }
}

@Composable
private fun TopImageDetectionBox(
    modifier: Modifier = Modifier,
    state: HomeState
) {
    val fModifier = Modifier.fillMaxWidth()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (state.image != null) {
            AsyncImage(
                modifier = fModifier,
                model = state.image,
                contentDescription = "Image"
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    tint = MaterialTheme.colorScheme.surface,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    color = MaterialTheme.colorScheme.surface,
                    text = "Photo is necessary for stress evaluation.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun BottomSliderBox(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    state: HomeState
) {
    val fModifier = Modifier.fillMaxWidth()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CustomSlider(
                modifier = fModifier,
                viewModel = viewModel,
                sliderPosition = state.sensorValues.snoringRate,
                sensor = HealthSensors.SnoringRateSensors
            )

            CustomSlider(
                modifier = fModifier,
                viewModel = viewModel,
                sliderPosition = state.sensorValues.sleepHours,
                sensor = HealthSensors.HoursOfSleepSensors
            )

            CustomSlider(
                modifier = fModifier,
                viewModel = viewModel,
                sliderPosition = state.sensorValues.respirationRate,
                sensor = HealthSensors.RespirationRateSensors
            )
        }
    }
}

private fun saveImageToExternalStorage(context: Context, bitmap: Bitmap): Uri? {
    val filename = "${System.currentTimeMillis()}.png"
    var fos: OutputStream? = null
    var imageUri: Uri? = null

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        context.contentResolver?.also { resolver ->
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
            imageUri =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = imageUri?.let {
                resolver.openOutputStream(it)
            }
        }
    } else {
        val imagesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File(imagesDir, filename)
        fos = FileOutputStream(image)
        imageUri = Uri.fromFile(image)
    }

    fos?.use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
    }

    return imageUri
}

//private fun detectFaceFromBitmap(
//    bitmap: Bitmap
//): Bitmap? {
//    val inputImage = InputImage.fromBitmap(bitmap, 0)
//    val options = FaceDetectorOptions.Builder()
//        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
//        .build()
//    val detector = FaceDetection.getClient(options)
//    var outputBitmap: Bitmap? = null
//
//    detector.process(inputImage)
//        .addOnSuccessListener { faces ->
//            if (faces.isNotEmpty()) {
//                val face = faces[0]
//                val bounds = face.boundingBox
//                outputBitmap = cropAndResizeBitmap(bitmap, bounds, 200, 200)
//                Log.d("face", "$outputBitmap")
//            }
//        }
//        .addOnFailureListener { e ->
//            Log.e("error", "$outputBitmap")
//        }
//
//    Log.d("face", "$outputBitmap")
//    return outputBitmap
//}

@Composable
fun TimerBox(
    modifier: Modifier = Modifier,
    time: String
) {
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .padding(end = 10.dp)
            .clip(shape)
            .background(Color.Red)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = time, color = MaterialTheme.colorScheme.primary)
    }
}