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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.hungrybrothers.abletotrip.KakaoAuthViewModel
import com.hungrybrothers.abletotrip.ui.navigation.NavRoute
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

    Splash()
}

@Composable
fun Splash() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "",
    )
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors = listOf(Color.Blue, Color.Cyan),
                        ),
                ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Welcome to AbleToTrip",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.alpha(alpha),
            )
            Spacer(modifier = Modifier.height(20.dp))
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSplashScreen() {
    SplashScreen(navController = rememberNavController())
}
