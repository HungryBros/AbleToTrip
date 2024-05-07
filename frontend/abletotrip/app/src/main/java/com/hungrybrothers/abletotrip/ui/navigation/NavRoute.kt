package com.hungrybrothers.abletotrip.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.hungrybrothers.abletotrip.ui.screen.AddressScreen
import com.hungrybrothers.abletotrip.ui.screen.DepartureScreen
import com.hungrybrothers.abletotrip.ui.screen.DetailScreen
import com.hungrybrothers.abletotrip.ui.screen.GuideScreen
import com.hungrybrothers.abletotrip.ui.screen.HomeScreen
import com.hungrybrothers.abletotrip.ui.screen.LoginScreen
import com.hungrybrothers.abletotrip.ui.screen.SearchScreen
import com.hungrybrothers.abletotrip.ui.screen.SplashScreen
import com.hungrybrothers.abletotrip.ui.screen.TestNavFile
import com.hungrybrothers.abletotrip.ui.screen.TotalRouteScreen
import com.hungrybrothers.abletotrip.ui.viewmodel.PlaceCompleteViewModel
import com.kakao.sdk.common.KakaoSdk.type

// 네비게이션 라우트 이넘(값을 가지는 이넘) -> 라우트액션에서 사용하기위해서
// 라우트 네임이 키임
enum class NavRoute(val routeName: String, val description: String) {
    SPLASH("SPLASH", "스플래시화면"),
    HOME("HOME", "홈화면"),
    LOGIN("LOGIN", "로그인화면"),
    ADDRESS("ADDRESS", "초기주소입력화면"),
    SEARCH("SEARCH", "검색화면"),
    DETAIL("DETAIL", "상세화면"),
    DEPARTURE("DEPARTURE", "출발화면"),
    TOTAL_ROUTE("TOTAL_ROUTE", "전체경로화면"),
    GUIDE("GUIDE", "길안내화면"),
    TESTNAV("TESTNAV", "네브테스트"),
}

@Composable
fun Navigation(navController: NavHostController) {
    NavHost(navController, startDestination = NavRoute.SPLASH.routeName) {
        composable(NavRoute.SPLASH.routeName) { SplashScreen(navController) }
        auth(navController)
        home(navController)
    }
}

fun NavGraphBuilder.auth(navController: NavController) {
    navigation(startDestination = NavRoute.LOGIN.routeName, route = "AUTHGRAPH") {
        composable(NavRoute.LOGIN.routeName) { LoginScreen(navController) }
        composable(NavRoute.ADDRESS.routeName) {
            val autocompleteViewModel = viewModel<PlaceCompleteViewModel>()
            AddressScreen(navController, autocompleteViewModel)
        }
    }
}

fun NavGraphBuilder.home(navController: NavController) {
    navigation(startDestination = NavRoute.HOME.routeName, route = "HOMEGRAPH") {
        composable(NavRoute.HOME.routeName) { HomeScreen(navController) }
        composable(NavRoute.SEARCH.routeName) {
            SearchScreen(navController)
        }
        composable("${NavRoute.DETAIL.routeName}/{id}") { backStackEntry ->
            DetailScreen(navController, backStackEntry.arguments?.getString("id")?.toInt())
        }
//        composable(NavRoute.DEPARTURE.routeName) {
//            val autocompleteViewModel = viewModel<PlaceCompleteViewModel>()
//            DepartureScreen(navController, autocompleteViewModel)
//        }
        composable(
            route = "DEPARTURE/{latitude}/{longitude}/{address}",
            arguments = listOf(
                navArgument("latitude") { type = NavType.FloatType },
                navArgument("longitude") { type = NavType.FloatType },
                navArgument("address") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val latitude = backStackEntry.arguments?.getFloat("latitude")?.toDouble() ?: 0.0
            val longitude = backStackEntry.arguments?.getFloat("longitude")?.toDouble() ?: 0.0
            val address = backStackEntry.arguments?.getString("address") ?: ""

            val autocompleteViewModel = viewModel<PlaceCompleteViewModel>()
            DepartureScreen(navController, autocompleteViewModel, latitude, longitude, address)
        }
        composable(NavRoute.TOTAL_ROUTE.routeName) { TotalRouteScreen(navController) }
        composable(NavRoute.GUIDE.routeName) { GuideScreen(navController) }
        composable(NavRoute.TESTNAV.routeName) { TestNavFile(navController) }
    }
}

// fun NavController.navigateToSingleTop(route: String) {
//    navigate(route) {
//        popUpTo(graph.findStartDestination().id) {
//            saveState = true
//        }
//        launchSingleTop = true
//        restoreState = true
//    }
// }
