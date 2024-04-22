package com.hungrybrothers.abletotrip.ui.navigation

import androidx.navigation.NavHostController
import com.hungrybrothers.abletotrip.NAV_ROUTE

class RouteAction(private val navHostController: NavHostController) {
    fun navTo(route: NAV_ROUTE) {
        navHostController.navigate(route.routeName) {
            // 옵션을 추가하여, 예를 들어 이전 스택을 클리어할 수 있습니다.
            popUpTo(navHostController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun goBack() {
        navHostController.navigateUp()
    }
}
