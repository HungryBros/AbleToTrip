package com.hungrybrothers.abletotrip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.hungrybrothers.abletotrip.ui.theme.AbletotripTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hungrybrothers.abletotrip.ui.screen.*
import com.hungrybrothers.abletotrip.ui.navigation.RouteAction



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AbletotripTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigation()
                }
            }
        }
    }
}
// 네비게이션 라우트 이넘(값을 가지는 이넘) -> 라우트액션에서 사용하기위해서
enum class NAV_ROUTE(val routeName: String, val description: String) {
    HOME("HOME", "홈화면"),
    LOGIN("LOGIN", "로그인화면"),
    ADDRESS("ADDRESS", "초기주소입력화면"),
    SEARCH("SEARCH", "검색화면"),
    DETAIL("DETAIL", "상세화면"),
    DEPARTURE("DEPARTURE", "출발화면"),
    TOTAL_ROUTE("TOTAL_ROUTE", "전체경로화면"),
    GUIDE("GUIDE", "길안내화면"),
}
@Composable
fun Navigation() {
    val navController = rememberNavController()
    val routeAction = remember(navController) { RouteAction(navController) }
    NavHost(navController, startDestination = NAV_ROUTE.HOME.routeName) {
        composable(NAV_ROUTE.HOME.routeName) { HomeScreen(navController) }
        composable(NAV_ROUTE.LOGIN.routeName) { LoginScreen(navController) }
        composable(NAV_ROUTE.ADDRESS.routeName) { AddressScreen(navController) }
        composable(NAV_ROUTE.SEARCH.routeName) { SearchScreen(navController) }
        composable(NAV_ROUTE.DETAIL.routeName) { DetailScreen(navController) }
        composable(NAV_ROUTE.DEPARTURE.routeName) { DepartureScreen(navController) }
        composable(NAV_ROUTE.TOTAL_ROUTE.routeName) { TotalRouteScreen(navController) }
        composable(NAV_ROUTE.GUIDE.routeName) { GuideScreen(navController) }
    }
}

