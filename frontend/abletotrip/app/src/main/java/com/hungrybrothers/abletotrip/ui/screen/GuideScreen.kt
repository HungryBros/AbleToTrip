package com.hungrybrothers.abletotrip.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.hungrybrothers.abletotrip.R
import com.hungrybrothers.abletotrip.ui.navigation.NavRoute
import com.hungrybrothers.abletotrip.ui.theme.CustomBackground
import com.hungrybrothers.abletotrip.ui.viewmodel.NavigationViewModel
import com.hungrybrothers.abletotrip.ui.viewmodel.PolylineData
import com.hungrybrothers.abletotrip.ui.viewmodel.Resource
import com.hungrybrothers.abletotrip.ui.viewmodel.RestroomViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(
    navController: NavController,
    navigationViewModel: NavigationViewModel,
) {
    val scaffoldState = rememberBottomSheetScaffoldState()

    val openDialog = remember { mutableStateOf(false) }

    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                openDialog.value = false
            },
            title = {
                Text(
                    text = "탐색 종료",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(text = "경로 탐색을 종료하시겠어요?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                        navController.navigate(NavRoute.HOME.routeName)
                    },
                ) {
                    Text("종료하기")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                    },
                ) {
                    Text("머무르기")
                }
            },
        )
    }

    Surface(modifier = Modifier, color = CustomBackground) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            GuideBottomSheet(
                modifier = Modifier,
                scaffoldState = scaffoldState,
                navigationViewModel = navigationViewModel,
                openDialogState = openDialog,
            )
        }
    }
}

@Composable
fun GoogleMapGuide(
    modifier: Modifier,
    navigationViewModel: NavigationViewModel,
    viewModel: RestroomViewModel = viewModel(),
    openDialogState: MutableState<Boolean>,
) {
    var isRestroom by remember { mutableStateOf(false) }
    val restrooms by viewModel.restrooms.observeAsState(emptyList())
    val error by viewModel.error.observeAsState(null)

    // LiveData를 Compose 상태로 변환
    val navigationData by navigationViewModel.navigationData.observeAsState()
    val polylineDataList by navigationViewModel.polylineDataList.observeAsState(initial = emptyList())
    val walkDataList1 by navigationViewModel.walkDataList1.observeAsState(PolylineData(emptyList(), Color.Blue))
    val walkDataList2 by navigationViewModel.walkDataList2.observeAsState(PolylineData(emptyList(), Color.Blue))

    // 지도 상태 관리를 위한 remember
    var uiSettings by remember { mutableStateOf(com.google.maps.android.compose.MapUiSettings()) }

    // `LiveData`를 관찰하여 동적으로 업데이트되는 지점
    val departureResource by navigationViewModel.departureData.observeAsState(Resource.loading(null))
    val arrivalResource by navigationViewModel.arrivalData.observeAsState(Resource.loading(null))

    // `LatLng` 값으로부터 마커 상태를 선언
    val startLatLng = departureResource.data ?: LatLng(0.0, 0.0)
    val endLatLng = arrivalResource.data ?: LatLng(0.0, 0.0)

    val startMarkerState = rememberMarkerState(position = startLatLng)
    val endMarkerState = rememberMarkerState(position = endLatLng)

    LaunchedEffect(Unit) {
        viewModel.fetchRestrooms()
        uiSettings = uiSettings.copy(zoomControlsEnabled = false)
    }

    var gpsPoint by remember { mutableStateOf(LatLng(37.501286, 127.0396029)) }
    val context = LocalContext.current
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    // 위치 리스너 먼저 선언
    val locationListener =
        LocationListener { location ->
            Log.d("LocationUpdates", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")
            gpsPoint = LatLng(location.latitude, location.longitude)
        }

    // 권한 요청 처리
    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            permissions.entries.forEach {
                if (it.key == Manifest.permission.ACCESS_FINE_LOCATION && it.value == true) {
                    // 권한이 승인된 후 위치 업데이트 요청
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1f, locationListener)
                    }
                }
            }
        }

    // 디버깅을 위해 `polylineDataList` 출력
    println("guide check : $polylineDataList")

    LaunchedEffect(key1 = true) {
        permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
        )
    }

    val bounds =
        LatLngBounds.Builder()
            .include(startLatLng)
            .include(endLatLng)
            .include(gpsPoint)
            .build()

    val gpsMarkerState = remember(gpsPoint) { MarkerState(position = gpsPoint) }

    val cameraPositionState =
        rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(bounds.center, 13f)
        }
    LaunchedEffect(gpsPoint) {
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(gpsPoint, 20f))
        println("restroom check : $restrooms")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = modifier,
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings,
        ) {
//            println("data check check : in $walkDataList2")
            Polyline(
                points = walkDataList1.points,
                color = walkDataList1.color,
                width = 40f,
            )
            polylineDataList.forEach { polylineData ->
                println("walk data : $polylineData")
                Polyline(
                    points = polylineData.points,
                    color = polylineData.color,
                    width = 40f,
                )
            }
            Polyline(
                points = walkDataList2.points,
                color = walkDataList2.color,
                width = 40f,
            )

            Marker(
                state = startMarkerState,
                title = "출발지",
                snippet = "Here is the start point",
            )
            Marker(
                state = endMarkerState,
                title = "도착지",
                snippet = "Here is the end point",
            )
            Marker(
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
                state = gpsMarkerState,
                title = "현재위치",
                snippet = "Here is the GPS point",
            )
            if (isRestroom) {
                restrooms.forEach { restroom ->
                    val position = LatLng(restroom.coordinate.latitude, restroom.coordinate.longitude)
                    Marker(
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW),
                        state = com.google.maps.android.compose.rememberMarkerState(position = position),
                        title = restroom.station_fullname,
                        snippet = "Floor: ${restroom.floor}, Location: ${restroom.restroom_location}",
                    )
                }
            }
        }
        val containerColor = if (isRestroom) Color.Gray else Color.Red
        // 오른쪽 상단에 고정된 빨간색 화장실 원형 버튼
        FloatingActionButton(
            onClick = { isRestroom = !isRestroom },
            modifier =
                Modifier
                    .align(Alignment.TopEnd) // 우측 상단에 위치
                    .padding(top = 88.dp, end = 16.dp),
            shape = CircleShape,
            containerColor = containerColor,
            contentColor = Color.White,
        ) {
            val restroomIcon: Painter = painterResource(id = R.drawable.family_restroom)

            Image(
                painter = restroomIcon,
                contentDescription = "disabled restroom Icon",
            )
        }

        // 오른쪽 상단에 고정된 종료 원형 버튼
        FloatingActionButton(
            onClick = { openDialogState.value = !openDialogState.value },
            modifier =
                Modifier
                    .align(Alignment.TopEnd) // 우측 상단에 위치
                    .padding(top = 16.dp, end = 16.dp),
            shape = CircleShape,
            containerColor = Color.White,
            contentColor = Color.Black,
        ) {
            val closeIcon: Painter = painterResource(id = R.drawable.close)

            Image(
                painter = closeIcon,
                contentDescription = "Close Icon",
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideBottomSheet(
    modifier: Modifier,
    scaffoldState: BottomSheetScaffoldState,
    navigationViewModel: NavigationViewModel,
    openDialogState: MutableState<Boolean>,
) {
    val detailRouteInfo by navigationViewModel.detailRouteInfo.observeAsState(emptyList())

    Box(modifier = modifier.fillMaxSize()) {
        // BottomSheetScaffold로 바텀시트 구현
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContent = {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    detailRouteInfo.forEach { routeInfo ->
                        val iconResource =
                            if (routeInfo.type == "subway") {
                                R.drawable.subway
                            } else {
                                R.drawable.walk
                            }
                        item {
                            routeInfo.info.forEach { detail ->
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(100.dp)
                                            .padding(16.dp)
                                            .drawBottomBorder(borderColor = Color.LightGray, borderWidth = 1f),
                                ) {
                                    Row {
                                        Icon(
                                            painter = painterResource(id = iconResource),
                                            contentDescription = "${routeInfo.type} Icon",
                                            modifier = Modifier.size(24.dp).padding(end = 8.dp), // 아이콘 크기 조절
                                            tint = Color.Unspecified, // 원래 아이콘 색상 유지
                                        )
                                        Text(
                                            text = detail,
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            sheetPeekHeight = 60.dp, // 시트가 보일 때의 최소 높이
        ) {
            GoogleMapGuide(
                modifier = Modifier,
                navigationViewModel = navigationViewModel,
                openDialogState = openDialogState,
            )
        }
    }
}

fun Modifier.drawBottomBorder(
    borderColor: Color,
    borderWidth: Float,
): Modifier =
    this.then(
        Modifier.drawBehind {
            val strokeWidth = borderWidth
            val y = size.height - strokeWidth / 2
            drawLine(
                color = borderColor,
                start = androidx.compose.ui.geometry.Offset(0f, y),
                end = androidx.compose.ui.geometry.Offset(size.width, y),
                strokeWidth = strokeWidth,
            )
        },
    )

@Preview(showBackground = true)
@Composable
fun PreviewGuideScreen() {
    // rememberNavController를 사용하여 Preview에서 NavController를 제공합니다.
    GuideScreen(navController = rememberNavController(), navigationViewModel = NavigationViewModel())
}
