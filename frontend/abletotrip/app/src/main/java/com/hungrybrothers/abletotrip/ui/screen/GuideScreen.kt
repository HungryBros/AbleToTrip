package com.hungrybrothers.abletotrip.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
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
    var dottedPolylineList1 by remember { mutableStateOf(listOf<LatLng>()) }
    var dottedPolylineList2 by remember { mutableStateOf(listOf<LatLng>()) }

    LaunchedEffect(true) {
        val lastWalkPoint = walkDataList1.points.lastOrNull()
        val firstPolylinePoint = polylineDataList.firstOrNull()?.points?.firstOrNull()

        val lastPolylinePoint = polylineDataList.lastOrNull()?.points?.lastOrNull()
        val firstWalkPoint = walkDataList2.points.firstOrNull()
        println("dotted check : $polylineDataList")

        println("dotted check : $lastWalkPoint")
        println("dotted check : $firstPolylinePoint")
        println("dotted check : $lastPolylinePoint")
        println("dotted check : $firstWalkPoint")
        if (lastWalkPoint != null && firstPolylinePoint != null && lastPolylinePoint != null && firstWalkPoint != null) {
            // 새로운 polyline 생성
            dottedPolylineList1 = listOf(lastWalkPoint, firstPolylinePoint)
            dottedPolylineList2 = listOf(lastPolylinePoint, firstWalkPoint)
            println("dotted check : $dottedPolylineList1")
            println("dotted check : $dottedPolylineList2")
        }
    }

    // 지도 상태 관리를 위한 remember
    var uiSettings by remember { mutableStateOf(com.google.maps.android.compose.MapUiSettings()) }

    // `LiveData`를 관찰하여 동적으로 업데이트되는 지점
    val departureResource by navigationViewModel.departureData.observeAsState(Resource.loading(null))
    val arrivalResource by navigationViewModel.arrivalData.observeAsState(Resource.loading(null))

    // `LatLng` 값으로부터 마커 상태를 선언
    val startLatLng = departureResource.data ?: LatLng(37.501286, 127.0396029)
    val endLatLng = arrivalResource.data ?: LatLng(37.501286, 127.0396029)

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
                if (it.key == Manifest.permission.ACCESS_FINE_LOCATION && it.value) {
                    // 권한이 승인된 후 위치 업데이트 요청
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0,
                            1f,
                            locationListener,
                        )
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

    var gpsButtonClicked by remember { mutableStateOf(false) }
    LaunchedEffect(gpsPoint, gpsButtonClicked) {
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(gpsPoint, 17f))
        println("restroom check : $restrooms")
    }

    var bearing by remember { mutableStateOf(0f) }

    LaunchedEffect(gpsPoint, endLatLng) {
        bearing = calculateBearing(gpsPoint, endLatLng)
        cameraPositionState.position =
            CameraPosition(
                gpsPoint, // target
                17f, // zoom
                45f, // tilt
                bearing, // bearing
            )
        cameraPositionState.animate(CameraUpdateFactory.newCameraPosition(cameraPositionState.position))
//        Log.d("Camera", "Azimuth: $azimuth")
    }

    // 아이콘을 크기 조정하여 생성하는 함수
    fun getResizedBitmapDescriptor(
        context: Context,
        resourceId: Int,
        width: Int,
        height: Int,
    ): BitmapDescriptor {
        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    }

    val arrowIcon = getResizedBitmapDescriptor(context, R.drawable.arrow, 20, 20)

    val dotPattern = listOf(Dot(), Gap(10f))

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
            addArrowsToPolyline(walkDataList1.points, arrowIcon)
            Polyline(
                points = dottedPolylineList1,
                color = Color.Blue,
                width = 40f,
                pattern = dotPattern,
            )
            polylineDataList.forEach { polylineData ->
                println("walk data : $polylineData")
                Polyline(
                    points = polylineData.points,
                    color = polylineData.color,
                    width = 40f,
                )
                addArrowsToPolyline(polylineData.points, arrowIcon)
            }
            Polyline(
                points = dottedPolylineList2,
                color = Color.Blue,
                width = 40f,
                pattern = dotPattern,
            )
            Polyline(
                points = walkDataList2.points,
                color = walkDataList2.color,
                width = 40f,
            )
            addArrowsToPolyline(walkDataList2.points, arrowIcon)
            Marker(
                icon = BitmapDescriptorFactory.fromResource(R.drawable.departurepin),
                state = startMarkerState,
                title = "출발지",
            )
            Marker(
                icon = BitmapDescriptorFactory.fromResource(R.drawable.arrivalpin),
                state = endMarkerState,
                title = "도착지",
            )
            Marker(
                icon = BitmapDescriptorFactory.fromResource(R.drawable.linepoint),
                state = gpsMarkerState,
                title = "현재 위치",
            )
            if (isRestroom) {
                restrooms.forEach { restroom ->
                    val position = LatLng(restroom.coordinate.latitude, restroom.coordinate.longitude)
                    Marker(
                        icon = BitmapDescriptorFactory.fromResource(R.drawable.toilet),
                        state = com.google.maps.android.compose.rememberMarkerState(position = position),
                        title = restroom.station_fullname,
                        snippet = "Floor: ${restroom.floor}, Location: ${restroom.restroom_location}",
                    )
                }
            }
        }
        val containerColor = if (isRestroom) Color.Gray else Color.Red
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
        // 오른쪽 상단에 고정된 GPS 원형 버튼
        FloatingActionButton(
            onClick = { gpsButtonClicked = !gpsButtonClicked },
            modifier =
                Modifier
                    .align(Alignment.TopEnd) // 우측 상단에 위치
                    .padding(top = 160.dp, end = 16.dp),
            shape = CircleShape,
            containerColor = Color.White,
            contentColor = Color.White,
        ) {
            val restroomIcon: Painter = painterResource(id = R.drawable.target)

            Image(
                painter = restroomIcon,
                contentDescription = "disabled restroom Icon",
                modifier = Modifier.padding(top = 12.dp),
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

// 도착지 방향을 가리키는 `bearing` 값 계산 함수
fun calculateBearing(
    start: LatLng,
    end: LatLng,
): Float {
    val startLat = Math.toRadians(start.latitude)
    val startLng = Math.toRadians(start.longitude)
    val endLat = Math.toRadians(end.latitude)
    val endLng = Math.toRadians(end.longitude)

    val dLng = endLng - startLng

    val y = Math.sin(dLng) * Math.cos(endLat)
    val x = Math.cos(startLat) * Math.sin(endLat) - Math.sin(startLat) * Math.cos(endLat) * Math.cos(dLng)

    return ((Math.toDegrees(Math.atan2(y, x)) + 360) % 360).toFloat()
}

@Composable
fun addArrowsToPolyline(
    points: List<LatLng>,
    arrowIcon: BitmapDescriptor,
) {
    val spacing = 8.0 // 화살표 간격을 나타내는 거리 단위 (미터)
    var cumulativeDistance = 0.0

    for (i in 0 until points.size - 1) {
        val start = points[i]
        val end = points[i + 1]

        // 거리 계산
        val distance = calculateDistance(start, end)
        cumulativeDistance += distance

        if (cumulativeDistance >= spacing) {
            // Bearing 계산
            val bearing = (calculateBearing(start, end) + 180) % 360

            // 화살표 마커 추가
            Marker(
                state = rememberMarkerState(position = start),
                icon = arrowIcon,
                rotation = bearing,
            )

            cumulativeDistance = 0.0
        }
    }
}

// 두 점 사이의 거리를 계산하는 함수
fun calculateDistance(
    start: LatLng,
    end: LatLng,
): Double {
    val earthRadius = 6371000.0 // 지구 반경 (미터)

    val dLat = Math.toRadians(end.latitude - start.latitude)
    val dLng = Math.toRadians(end.longitude - start.longitude)

    val a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(start.latitude)) * Math.cos(Math.toRadians(end.latitude)) *
            Math.sin(dLng / 2) * Math.sin(dLng / 2)

    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

    return earthRadius * c
}

@Preview(showBackground = true)
@Composable
fun PreviewGuideScreen() {
    // rememberNavController를 사용하여 Preview에서 NavController를 제공합니다.
    GuideScreen(navController = rememberNavController(), navigationViewModel = NavigationViewModel())
}
