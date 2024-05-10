package com.hungrybrothers.abletotrip.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.hungrybrothers.abletotrip.KakaoAuthViewModel
import com.hungrybrothers.abletotrip.R
import com.hungrybrothers.abletotrip.ui.navigation.NavRoute
import com.hungrybrothers.abletotrip.ui.theme.CustomPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val SPTIME: Long = 3000

@Composable
fun SplashScreen(navController: NavController) {
    val viewModel: KakaoAuthViewModel = viewModel()
    val loggedIn = viewModel.loggedIn.observeAsState().value
    val loginResult = viewModel.loginResult.observeAsState().value

    LaunchedEffect(loggedIn, loginResult) {
        delay(SPTIME)
        withContext(Dispatchers.Main) {
            when {
                loggedIn == true && loginResult == io.ktor.http.HttpStatusCode.OK -> {
                    navController.navigate(NavRoute.HOME.routeName) {
                        popUpTo(NavRoute.SPLASH.routeName) { inclusive = true }
                    }
                }
                loggedIn == true && loginResult == io.ktor.http.HttpStatusCode.Accepted -> {
                    navController.navigate(NavRoute.ADDRESS.routeName) {
                        popUpTo(NavRoute.SPLASH.routeName) { inclusive = true }
                    }
                }
                else -> {
                    navController.navigate(NavRoute.LOGIN.routeName) {
                        popUpTo(NavRoute.SPLASH.routeName) { inclusive = true }
                    }
                }
            }
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Splash()
    }
}

@Composable
fun Splash() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(color = CustomPrimary),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(start = 40.dp, end = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.easytriplogowhite),
                tint = Color.Unspecified,
                contentDescription = "Logo",
                modifier = Modifier.padding(bottom = 32.dp),
            )
            Icon(
                modifier = Modifier.padding(32.dp),
                painter = painterResource(id = R.drawable.wheelchairwhite),
                tint = Color.Unspecified,
                contentDescription = "wheelchair",
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp,
            )
        }

        Box(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.bottomlogo),
                tint = Color.Unspecified,
                contentDescription = "Logo",
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSplashScreen() {
    SplashScreen(navController = rememberNavController())
}
