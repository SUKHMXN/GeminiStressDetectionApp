package com.varunkumar.geminiapi.presentation.features.profile_feature

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Output
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.varunkumar.geminiapi.presentation.features.sign_in_feature.UserData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    userData: UserData?,
    onSignOut: () -> Unit
) {
    val userDataInfo = UserDataInfo()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.primary,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.Black
                ),
                title = { Text(text = "Profile") },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(imageVector = Icons.Default.Output, contentDescription = null)
                    }
                }
            )
        }
    ) {
        val fModifier = Modifier.fillMaxWidth()

        Row(
            modifier = fModifier
                .padding(it)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
//            if (userData?.profilePictureUrl != null) {
//                AsyncImage(
//                    modifier = Modifier
//                        .clip(CircleShape)
//                        .size(50.dp),
//                    model = userData.profilePictureUrl,
//                    contentDescription = "Profile",
//                    contentScale = ContentScale.Crop
//                )
//            }
//
//            if (userData?.username != null) {
//                Text(text = userData.username)
//            }

            UserInfoCard(
                modifier = fModifier,
                userData = userData,
                userInfo = userDataInfo
            )

//            ProfileHeader(modifier = fModifier) {
//                navController.navigate(Routes.Login.route)
//            }
//
//            ProfileStats(modifier = fModifier, metrics = metrics)
        }
    }
}

@Composable
fun UserInfoCard(
    modifier: Modifier = Modifier,
    userData: UserData?,
    userInfo: UserDataInfo?
) {
    ListItem(
        modifier = modifier.clip(RoundedCornerShape(20.dp)),
        leadingContent = {
            AsyncImage(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                model = userData?.profilePictureUrl,
                contentDescription = "profile image",
                contentScale = ContentScale.Crop
            )
        },
        headlineContent = {
            userData?.username?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        supportingContent = {
            userInfo?.let { info ->
                InfoGrid(
                    modifier = Modifier.fillMaxWidth(),
                    userInfo = info
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.secondary
        ),
        shadowElevation = 30.dp
    )
}

@Composable
fun InfoGrid(
    modifier: Modifier = Modifier,
    userInfo: UserDataInfo
) {
    Column(
        modifier = modifier.padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        val infoBoxModifier = Modifier
            .weight(0.5f)
            .clip(RoundedCornerShape(30.dp))
            .background(Color.LightGray)
            .padding(10.dp)

        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfoBox(
                modifier = infoBoxModifier,
                icon = Icons.Default.Logout,
                value = userInfo.sex
            )

            InfoBox(
                modifier = infoBoxModifier,
                icon = Icons.Default.Timeline,
                value = userInfo.age.toString()
            )
        }

        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfoBox(
                modifier = infoBoxModifier,
                icon = Icons.Default.Height,
                value = userInfo.height.toString()
            )

            InfoBox(
                modifier = infoBoxModifier,
                icon = Icons.Default.Person,
                value = userInfo.weight.toString()
            )
        }
    }
}

@Composable
fun InfoBox(
    icon: ImageVector,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = "")
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = value, fontWeight = FontWeight.Bold)
    }
}

data class UserDataInfo(
    val sex: String = "Male",
    val age: Int = 19,
    val weight: Int = 60,
    val height: Int = 170
)