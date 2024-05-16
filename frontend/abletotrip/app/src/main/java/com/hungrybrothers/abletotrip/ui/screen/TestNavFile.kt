package com.hungrybrothers.abletotrip.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.hungrybrothers.abletotrip.ui.navigation.NavRoute

@Composable
fun TestNavFile(navController: NavController) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = "테스트!!!!!!!!!!!!!!!!!!!!!")
        Button(onClick = {
            navController.navigate(NavRoute.LOGIN.routeName)
        }) {
            Text("로그인 하러가 임마")
        }
        Button(onClick = {
            navController.navigateUp()
        }) {
            Text("뒤로가기 버튼임")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTestNavFile() {
    // rememberNavController를 사용하여 Preview에서 NavController를 제공합니다.
    TestNavFile(navController = rememberNavController())
}
