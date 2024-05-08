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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
fun TotalRouteScreen(
    navController: NavController,
    departure: String?,
    arrival: String?,
) {
    val navigationViewModel: NavigationViewModel = viewModel()
    Surface(modifier = Modifier, color = CustomBackground) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            HeaderBar(navController = navController, true)
            TotalRouteGoogleMap(
                modifier = Modifier.weight(7f),
                navigationViewModel = navigationViewModel,
                departure = departure,
                arrival = arrival,
            )
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
    departure: String?,
    arrival: String?,
) {
    // 네비게이션 데이터를 가져오기 위한 첫 호출
    LaunchedEffect(Unit) {
        navigationViewModel.fetchNavigationData(departure = departure, arrival = arrival)
    }

    // LiveData를 Compose 상태로 변환
    val navigationData by navigationViewModel.navigationData.observeAsState()
    val polylineDataList by navigationViewModel.polylineDataList.observeAsState(initial = emptyList())

    // `LiveData`를 관찰하여 동적으로 업데이트되는 지점
    val departureResource by navigationViewModel.departureData.observeAsState(Resource.loading(null))
    val arrivalResource by navigationViewModel.arrivalData.observeAsState(Resource.loading(null))

    // 초기값을 0.0으로 설정하고, `_departureData`와 `_arrivalData`에 맞게 업데이트
    var mystartpoint by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    var myendpoint by remember { mutableStateOf(LatLng(0.0, 0.0)) }

    // 마커 상태 선언
    val startMarkerState = rememberMarkerState(position = mystartpoint)
    val endMarkerState = rememberMarkerState(position = myendpoint)

    // `departureResource`와 `arrivalResource`의 상태에 따라 업데이트
    LaunchedEffect(departureResource) {
        if (departureResource.status == Resource.Status.SUCCESS) {
            departureResource.data?.let {
                mystartpoint = it
                startMarkerState.position = it
                println("start end : $mystartpoint")
            }
        }
    }

    LaunchedEffect(arrivalResource) {
        if (arrivalResource.status == Resource.Status.SUCCESS) {
            arrivalResource.data?.let {
                myendpoint = it
                endMarkerState.position = it
                println("start end : $myendpoint")
            }
        }
    }

    // navigationData의 상태에 따른 UI 처리
    navigationData?.let { resource ->
        when (resource.status) {
            Resource.Status.SUCCESS -> {
                val data = resource.data
                // navigationData에서 필요한 작업을 수행하세요
                Log.d("TotalRouteGoogleMap", "${data?.is_bus_exist}")

                Log.d("TotalRouteGoogleMap", "Start Point: $mystartpoint, End Point: $myendpoint")
            }
            Resource.Status.ERROR -> {
                val errorMessage = resource.message
                // 오류 처리
                Log.e("TotalRouteGoogleMap", "Error: $errorMessage")
            }
            Resource.Status.LOADING -> {
                // 로딩 중 처리
                Log.d("TotalRouteGoogleMap", "Loading navigation data...")
            }
        }
    }

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

    // `mystartpoint`나 `myendpoint`가 변경될 때마다 카메라 위치를 업데이트
    LaunchedEffect(mystartpoint, myendpoint) {
        coroutineScope.launch {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(multicameraState, 130))
            println("polylineOptionsList data : $polylineDataList")
        }
    }

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
            state = startMarkerState,
            title = "출발지",
            snippet = departure,
        )
        Marker(
            state = endMarkerState,
            title = "도착지",
            snippet = arrival,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTotalRouteScreen() {
    // rememberNavController를 사용하여 Preview에서 NavController를 제공합니다.
    TotalRouteScreen(navController = rememberNavController(), departure = null, arrival = null)
}
