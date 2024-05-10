package com.hungrybrothers.abletotrip.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.hungrybrothers.abletotrip.KakaoAuthViewModel
import com.hungrybrothers.abletotrip.R
import com.hungrybrothers.abletotrip.ui.navigation.NavRoute
import io.ktor.http.HttpStatusCode

@Composable
fun LoginScreen(navController: NavController) {
    val viewModel: KakaoAuthViewModel = viewModel()
    val loggedIn = viewModel.loggedIn.observeAsState().value
    val loginResult = viewModel.loginResult.observeAsState().value

    LaunchedEffect(loggedIn, loginResult) {
        if (loggedIn == true) {
            when (loginResult) {
                HttpStatusCode.Created, HttpStatusCode.Accepted -> {
                    navController.navigate(NavRoute.ADDRESS.routeName) {
                        popUpTo("AUTHGRAPH") {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
                HttpStatusCode.OK -> {
                    navController.navigate(NavRoute.HOME.routeName) {
                        popUpTo("AUTHGRAPH") {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
                else -> {}
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.easytriplogo),
                    contentDescription = "EasyTrip Logo",
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 40.dp),
                )
                Image(
                    modifier = Modifier.padding(40.dp),
                    painter = painterResource(id = R.drawable.wheelchair),
                    contentDescription = "Accessibility Symbol",
                )
            }

            ImageAsButton(onClick = { viewModel.handleKakaoLogin() })
        }
    }
}

@Composable
fun ImageAsButton(onClick: () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(48.dp)
                .clickable(onClick = onClick)
                .clip(RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.kakao_login_medium_wide),
            contentDescription = "Kakao Login Button",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    LoginScreen(navController = rememberNavController())
}
