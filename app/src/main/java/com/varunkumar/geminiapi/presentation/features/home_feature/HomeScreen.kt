package com.varunkumar.geminiapi.presentation.features.home_feature

import android.content.Intent
import android.net.Uri
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.varunkumar.geminiapi.presentation.Routes
import com.varunkumar.geminiapi.presentation.features.sign_in_feature.UserData
import com.varunkumar.geminiapi.presentation.viewModels.AppViewModel
import com.varunkumar.geminiapi.ui.theme.GeminiApiTheme
import com.varunkumar.geminiapi.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    userData: UserData?,
    viewModel: HomeViewModel,
    onImageClick: () -> Unit
) {
    val fModifier = Modifier.fillMaxWidth()
    val state by viewModel.state.collectAsState()

    GeminiApiTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.primary,
            floatingActionButton = {
                FloatingActionButton(
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 0.dp
                    ),
                    shape = CircleShape,
                    onClick = { navController.navigate(Routes.Chat.route) }
                ) {
                    Icon(imageVector = Icons.Outlined.Chat, contentDescription = null)
                }
            },
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.Black
                    ),
                    title = { Text(text = "Home") },
                    actions = {
                        userData?.profilePictureUrl?.let {
                            AsyncImage(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        onImageClick()
                                    },
                                model = userData?.profilePictureUrl,
                                contentDescription = "Profile",
                                contentScale = ContentScale.Fit
                            )

                            Spacer(modifier = Modifier.width(10.dp))
                        }
                    }
                )
            }
        ) {
            Column(
                modifier = modifier
                    .padding(it)
                    .padding(16.dp),
            ) {
                StressBox(
                    modifier = fModifier
                        .weight(0.3f),
                    state = state,
                    onScanButtonClick = {
                        navController.navigate(Routes.Welcome.route)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                AnimatedVisibility(visible = state.stateResult is Result.Success) {
                    Column(
                        modifier = Modifier
                            .weight(0.7f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Recommendations",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

//                        YouTubeVideoPlayer("https://www.youtube.com/watch?v=lrhPTqholcc")

                        ArticleCard(
                            modifier = fModifier
                        )

                        ArticleCard(
                            modifier = fModifier
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendTab(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = "Recommendations",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black
        )

        Box(
            modifier = modifier
                .background(Color.LightGray)
        ) {
            Text(text = "Article")
        }
    }
}

@Composable
fun ArticleCard(
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.LightGray,
            contentColor = Color.Black
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.Red)
        ) {
            AsyncImage(
                model = "https://api.api-ninjas.com/v1/randomimage?category=nature",
                contentDescription = null
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "Learn about atrial fibrillation")
        }
    }
}

@Composable
fun StressBox(
    state: HomeState,
    modifier: Modifier = Modifier,
    onScanButtonClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ScaleBox")
    val scaleAnimation by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f, // Adjust the zoom factor as needed
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Column(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0xFFFFBBBB))
            .clickable { onScanButtonClick() }
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (state.stateResult) {
            is Result.Loading -> {
                Text(
                    text = "Loading",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            is Result.Error -> {
                Text(
                    text = state.stateResult.msg ?: "Error",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            is Result.Success -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Stress",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = state.stateResult.data ?: "Result",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            else -> {}
        }

        Text(text = "Scan Stress", style = MaterialTheme.typography.titleLarge, color = Color.Black)
    }
}

@Composable
fun YouTubeVideoPlayer(videoId: String) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.pluginState = WebSettings.PluginState.ON
                webViewClient = WebViewClient()
                loadUrl(videoId)
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

//@Composable
//fun YouTubeThumbnail(
//    videoId: String,
//    thumbnailUrl: String
//) {
//    val context = LocalContext.current
//
//    Box(
//        modifier = Modifier
//            .size(200.dp)
//            .clickable {
//                val intent = Intent(
//                    Intent.ACTION_VIEW,
//                    Uri.parse("$videoId")
//                )
//                context.startActivity(intent)
//            }
//    ) {
//        AndroidView(
//            factory = { ctx ->
//                android.widget.ImageView(ctx).apply {
//                    Picasso.get()
//                        .load(thumbnailUrl)
//                        .into(this)
//                }
//            },
//            modifier = Modifier.fillMaxWidth()
//        )
//    }
//}

@Preview
@Composable
private fun PreHome() {
}