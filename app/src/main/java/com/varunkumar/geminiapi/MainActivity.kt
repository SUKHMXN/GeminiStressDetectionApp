package com.varunkumar.geminiapi

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.identity.Identity
import com.varunkumar.geminiapi.presentation.Routes
import com.varunkumar.geminiapi.presentation.features.home_feature.HomeScreen
import com.varunkumar.geminiapi.presentation.features.home_feature.HomeViewModel
import com.varunkumar.geminiapi.presentation.features.profile_feature.ProfileScreen
import com.varunkumar.geminiapi.presentation.features.sign_in_feature.GoogleAuthClient
import com.varunkumar.geminiapi.presentation.features.sign_in_feature.SignInScreen
import com.varunkumar.geminiapi.presentation.features.sign_in_feature.SignInViewModel
import com.varunkumar.geminiapi.presentation.features.chat_feature.ChatScreen
import com.varunkumar.geminiapi.presentation.screens.SenseScreen
import com.varunkumar.geminiapi.presentation.viewModels.AppViewModel
import com.varunkumar.geminiapi.presentation.features.chat_feature.ChatViewModel
import com.varunkumar.geminiapi.presentation.features.sign_in_feature.UserData
import com.varunkumar.geminiapi.presentation.viewModels.LoginViewModel
import com.varunkumar.geminiapi.ui.theme.GeminiApiTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var googleAuthClient: GoogleAuthClient

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //TODO enable this later
        enableEdgeToEdge()

        if (!hasRequiredPermission()) {
            ActivityCompat.requestPermissions(this, permissions, 0)
        }

        setContent {
            GeminiApiTheme {
                val modifier = Modifier.fillMaxSize()
                val navController = rememberNavController()
                val loginViewModel = hiltViewModel<LoginViewModel>()
                val chatViewModel = hiltViewModel<ChatViewModel>()
                val appViewModel = hiltViewModel<AppViewModel>()
                val homeViewModel = hiltViewModel<HomeViewModel>()
                val signInViewModel = hiltViewModel<SignInViewModel>()

                val snackBarHostState = remember {
                    SnackbarHostState()
                }

                Scaffold(
                    snackbarHost = {
//                        Snackbar(snackbarData = snackBarHostState.currentSnackbarData)
                    }
                ) {
                    Column(
                        modifier = Modifier
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Routes.Login.route
                        ) {
                            composable(Routes.Welcome.route) {
                                SenseScreen(
                                    modifier = modifier,
                                    viewModel = homeViewModel,
                                    onDoneButtonClick = {
                                        navController.navigate(Routes.Home.route)
                                    }
                                )
                            }

                            composable(Routes.Home.route) {
                                HomeScreen(
                                    modifier = modifier,
                                    userData = googleAuthClient.getSignedInUser(),
                                    navController = navController,
                                    viewModel = homeViewModel,
                                    onImageClick = {
                                        navController.navigate(Routes.Profile.route)
                                    }
                                )
                            }

                            composable(Routes.Stats.route) {
//            SliderScreen(
//                modifier = modifier,
//                viewModel = senseViewModel,
//                onSaveButtonClick = {},
//                onCancelButtonClick = {
//                    navController.navigateUp()
//                }
//            )
                            }

                            composable(Routes.Login.route) {
                                val state by signInViewModel.state.collectAsStateWithLifecycle()

                                val launcher =
                                    rememberLauncherForActivityResult(contract = ActivityResultContracts.StartIntentSenderForResult()) { result ->
                                        if (result.resultCode == RESULT_OK) {
                                            lifecycleScope.launch {
                                                val signInResult =
                                                    googleAuthClient.signInWithIntent(
                                                        intent = result.data ?: return@launch
                                                    )
                                                signInViewModel.onSignInResult(signInResult)
                                            }
                                        }
                                    }

                                LaunchedEffect(state.isSignInSuccessful) {
                                    if (state.isSignInSuccessful) {
                                        lifecycleScope.launch {
                                            snackBarHostState.showSnackbar(
                                                message = "Sign in successful",
                                                duration = SnackbarDuration.Short
                                            )
                                        }

                                        navController.navigate(Routes.Home.route)
                                    }
                                }

                                SignInScreen(
                                    modifier = modifier,
                                    state = state,
                                    onSignInClick = {
                                        lifecycleScope.launch {
                                            val signInIntentSender = googleAuthClient.signIn()
                                            launcher.launch(
                                                IntentSenderRequest.Builder(
                                                    signInIntentSender ?: return@launch
                                                ).build()
                                            )
                                        }
                                    }
                                )

//            LoginScreen(
//                modifier = modifier,
//                viewModel = loginViewModel,
//                onLoginButtonClick = {
//                    navController.navigate(Routes.Home.route)
//                }
//            )
                            }

                            composable(Routes.Profile.route) {
//                        DashboardScreen(
//                            modifier = modifier,
//                            navController = navController,
//                            appViewModel = appViewModel
//                        )

                                ProfileScreen(
                                    userData = googleAuthClient.getSignedInUser(),
                                    onSignOut = {
                                        lifecycleScope.launch {
                                            googleAuthClient.signOut()

                                            Toast.makeText(
                                                applicationContext,
                                                "Signed Out",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                )
                            }

                            composable(Routes.Chat.route) {
                                ChatScreen(
                                    modifier = modifier,
                                    viewModel = chatViewModel,
                                    onBackButtonClick = {
                                        navController.navigateUp()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun hasRequiredPermission(): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(
                applicationContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        private val permissions = arrayOf(
            Manifest.permission.CAMERA
        )
    }
}