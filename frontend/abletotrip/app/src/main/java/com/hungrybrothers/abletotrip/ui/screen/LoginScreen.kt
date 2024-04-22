package com.hungrybrothers.abletotrip.ui.screen

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

@Composable
fun LoginScreen(navController: NavController) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Please log in to continue.")
    }
    Button(onClick = {
        navController.navigateUp()
    }) {
        Text("Go back")
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    // rememberNavController를 사용하여 Preview에서 NavController를 제공합니다.
    LoginScreen(navController = rememberNavController())
}
