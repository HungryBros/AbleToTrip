package com.hungrybrothers.abletotrip.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.hungrybrothers.abletotrip.ui.components.HeaderBar
import com.hungrybrothers.abletotrip.ui.navigation.NavRoute
import com.hungrybrothers.abletotrip.ui.theme.CustomBackground
import com.hungrybrothers.abletotrip.ui.theme.CustomPrimary
import com.hungrybrothers.abletotrip.ui.theme.CustomTertiary
import com.hungrybrothers.abletotrip.ui.viewmodel.NavigationViewModel
import com.hungrybrothers.abletotrip.ui.viewmodel.Resource
import kotlinx.coroutines.launch

@Composable
fun TotalRouteScreen(navController: NavController) {
    val navigationViewModel: NavigationViewModel = viewModel()
    Surface(modifier = Modifier, color = CustomBackground) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            HeaderBar(navController = navController, true)
            TotalRouteGoogleMap(modifier = Modifier.weight(7f), navigationViewModel = navigationViewModel)
            TotalRouteBottomBox(
                modifier = Modifier.weight(1f),
                navigationViewModel = navigationViewModel,
                navController = navController,
            )
        }
    }
}

@Composable
fun TotalRouteBottomBox(
    modifier: Modifier,
    navigationViewModel: NavigationViewModel,
    navController: NavController,
) {
    val duration by navigationViewModel.duration.observeAsState(0)
    Row(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(CustomTertiary)
                    .clickable(onClick = { /* TODO: Define what happens when the box is clicked */ }),
            contentAlignment = Alignment.Center,
            content = {
                Text(
                    text = "${duration}분",
                    style =
                        TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = CustomBackground,
                        ),
                )
            },
        )
        Box(
            modifier =
                Modifier
                    .weight(2f)
                    .fillMaxSize()
                    .background(CustomPrimary)
                    .clickable(onClick = { navController.navigate(NavRoute.GUIDE.routeName) }),
            contentAlignment = Alignment.Center,
            content = {
                Text(
                    text = "따라가기",
                    style =
                        TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = CustomBackground,
                        ),
                )
            },
        )
    }
}

fun parseCoordinates(jsonData: String): List<LatLng> {
    val gson = Gson() // Gson 인스턴스를 사용하여 JSON을 파싱합니다.
    val type = object : TypeToken<List<List<Double>>>() {}.type
    val rawCoordinates: List<List<Double>> = gson.fromJson(jsonData, type)

    // 좌표 목록을 LatLng 객체 목록으로 변환합니다.
    return rawCoordinates.map { LatLng(it[0], it[1]) }
}

@Composable
fun TotalRouteGoogleMap(
    modifier: Modifier,
    navigationViewModel: NavigationViewModel,
) {
    val polylineDataList by navigationViewModel.polylineDataList.observeAsState(initial = emptyList())

    val navigationData by navigationViewModel.getNavigationData().observeAsState()

    navigationData?.let { resource ->
        when (resource.status) {
            Resource.Status.SUCCESS -> {
                val data = resource.data
                // navigationData에서 필요한 작업을 수행하세요
                Log.d("TotalRouteGoogleMap", "${data?.is_bus_exist}")
            }
            Resource.Status.ERROR -> {
                val errorMessage = resource.message
                // 오류 처리
            }
            Resource.Status.LOADING -> {
                // 로딩 중 처리
            }
        }
    }
    val mystartpoint = LatLng(37.501286, 127.0396029)
    val myendpoint = LatLng(37.579617, 126.977041)

    val multicameraState =
        LatLngBounds.Builder()
            .include(mystartpoint)
            .include(myendpoint)
            .build()

    val cameraPositionState =
        rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(multicameraState.center, 10f)
        }
    val coroutineScope = rememberCoroutineScope()
//    println("receive data : $decodedpolyline")
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        onMapLoaded = {
            coroutineScope.launch {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(multicameraState, 130))
                println("polylineOptionsList data : $polylineDataList")
            }
        },
    ) {
        polylineDataList.forEach { polylineData ->
            println("walk data : $polylineData")
            Polyline(
                points = polylineData.points,
                color = polylineData.color,
                width = 25f,
            )
        }

        Marker(
            state = rememberMarkerState(position = mystartpoint),
            title = "출발지",
            snippet = "Here is the start point",
        )
        Marker(
            state = rememberMarkerState(position = myendpoint),
            title = "도착지",
            snippet = "Here is the end point",
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTotalRouteScreen() {
    // rememberNavController를 사용하여 Preview에서 NavController를 제공합니다.
    TotalRouteScreen(navController = rememberNavController())
}
