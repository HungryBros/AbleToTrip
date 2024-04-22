package com.hungrybrothers.abletotrip.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.hungrybrothers.abletotrip.NAV_ROUTE

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Welcome to the Home Screen!")

//        // 각 스크린으로 이동하는 버튼 추가
//        Button(
//            onClick = { navController.navigate(NAV_ROUTE.LOGIN.routeName) },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text(text = "Go to Login Screen")
//        }
//
//        Button(
//            onClick = { navController.navigate(NAV_ROUTE.ADDRESS.routeName) },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text(text = "Go to Address Input Screen")
//        }
//
//        Button(
//            onClick = { navController.navigate(NAV_ROUTE.SEARCH.routeName) },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text(text = "Go to Search Screen")
//        }
//
//        Button(
//            onClick = { navController.navigate(NAV_ROUTE.DETAIL.routeName) },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text(text = "Go to Detail Screen")
//        }
//
//        Button(
//            onClick = { navController.navigate(NAV_ROUTE.DEPARTURE.routeName) },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text(text = "Go to Departure Screen")
//        }
//
//        Button(
//            onClick = { navController.navigate(NAV_ROUTE.TOTAL_ROUTE.routeName) },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text(text = "Go to Total Route Screen")
//        }
//
//        Button(
//            onClick = { navController.navigate(NAV_ROUTE.GUIDE.routeName) },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text(text = "Go to Guide Screen")
//        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    // rememberNavController를 사용하여 Preview에서 NavController를 제공합니다.
    HomeScreen(navController = rememberNavController())
}
