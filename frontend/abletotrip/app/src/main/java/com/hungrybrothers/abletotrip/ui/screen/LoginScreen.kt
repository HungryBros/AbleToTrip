package com.hungrybrothers.abletotrip.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.hungrybrothers.abletotrip.KakaoAuthViewModel

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
        Button(onClick = { /*TODO*/ }) {
            Text(text = "카카오 로그아웃하기")
        }

        Text(text = "카카오 로그인 여부", textAlign = TextAlign.Center, fontSize = 20.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    // rememberNavController를 사용하여 Preview에서 NavController를 제공합니다.
    LoginScreen(navController = rememberNavController())
}
