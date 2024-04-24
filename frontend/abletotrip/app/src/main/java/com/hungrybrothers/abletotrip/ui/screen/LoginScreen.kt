package com.hungrybrothers.abletotrip.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.hungrybrothers.abletotrip.KakaoAuthViewModel
import com.hungrybrothers.abletotrip.ui.navigation.NavRoute

@Composable
fun LoginScreen(navController: NavController) {
    val viewModel: KakaoAuthViewModel = viewModel()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = {
            viewModel.handleKakaoLogin()
        }) {
            Text(text = "카카오 로그인하기")
        }
        // 만약 로그인 상태가 true라면 네비게이션을 수행합니다.
        // 로그인 상태가 true라면 네비게이션을 수행합니다.
        val loggedIn = viewModel.loggedIn.observeAsState().value
        if (loggedIn == true) {
            LaunchedEffect(loggedIn) { // 여기에서 key1을 loggedIn으로 변경합니다.
                navController.navigate(NavRoute.ADDRESS.routeName) {
                    // 기존에 쌓인 화면들을 클리어하고 새 화면을 탑 스택에 올립니다.
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    // rememberNavController를 사용하여 Preview에서 NavController를 제공합니다.
    LoginScreen(navController = rememberNavController())
}
