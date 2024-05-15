package com.hungrybrothers.abletotrip.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.hungrybrothers.abletotrip.R
import com.hungrybrothers.abletotrip.ui.navigation.NavRoute
import com.hungrybrothers.abletotrip.ui.theme.CustomBackground
import com.hungrybrothers.abletotrip.ui.theme.CustomBlue
import com.hungrybrothers.abletotrip.ui.theme.CustomPrimary
import com.hungrybrothers.abletotrip.ui.viewmodel.CurrentLocationViewModel
import com.hungrybrothers.abletotrip.ui.viewmodel.NavigationViewModel
import com.hungrybrothers.abletotrip.ui.viewmodel.PolylineData
import com.hungrybrothers.abletotrip.ui.viewmodel.Resource
import com.hungrybrothers.abletotrip.ui.viewmodel.RestroomViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(
    navController: NavController,
    navigationViewModel: NavigationViewModel,
    currentLocationViewModel: CurrentLocationViewModel,
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val context = LocalContext.current
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
                        navController.navigate(NavRoute.HOME.routeName) {
                            popUpTo("HOME")
                        }
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

    Surface(modifier = Modifier.fillMaxSize(), color = CustomBackground) {
        Column(modifier = Modifier.fillMaxSize()) {
            GuideBottomSheet(
                modifier = Modifier,
                scaffoldState = scaffoldState,
                navigationViewModel = navigationViewModel,
                openDialogState = openDialog,
                currentLocationViewModel = currentLocationViewModel,
            )
        }
    }

    // 자주 위치 업데이트 시작
    LaunchedEffect(Unit) {
        startFrequentLocationUpdates(context, currentLocationViewModel)
    }
}

@Composable
fun GoogleMapGuide(
    modifier: Modifier,
    navigationViewModel: NavigationViewModel,
    viewModel: RestroomViewModel = viewModel(),
    openDialogState: MutableState<Boolean>,
    currentLocationViewModel: CurrentLocationViewModel,
) {
    var isRestroom by remember { mutableStateOf(false) }
    val restrooms by viewModel.restrooms.observeAsState(emptyList())
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

        if (lastWalkPoint != null && firstPolylinePoint != null && lastPolylinePoint != null && firstWalkPoint != null) {
            dottedPolylineList1 = listOf(lastWalkPoint, firstPolylinePoint)
            dottedPolylineList2 = listOf(lastPolylinePoint, firstWalkPoint)
        }
    }

    val uiSettings by remember { mutableStateOf(MapUiSettings(zoomControlsEnabled = false)) }

    val departureResource by navigationViewModel.departureData.observeAsState(Resource.loading(null))
    val arrivalResource by navigationViewModel.arrivalData.observeAsState(Resource.loading(null))

    val startLatLng = departureResource.data ?: LatLng(0.0, 0.0)
    val endLatLng = arrivalResource.data ?: LatLng(0.0, 0.0)
    val currentLatitude by currentLocationViewModel.latitude.observeAsState(null)
    val currentLongitude by currentLocationViewModel.longitude.observeAsState(null)
    val gpsPoint =
        currentLatitude?.let { lat ->
            currentLongitude?.let { lng -> LatLng(lat.toDouble(), lng.toDouble()) }
        }

    val startMarkerState = rememberMarkerState(position = startLatLng)
    val endMarkerState = rememberMarkerState(position = endLatLng)
    val gpsMarkerState = rememberMarkerState(position = gpsPoint ?: LatLng(0.0, 0.0))

    LaunchedEffect(Unit) {
        viewModel.fetchRestrooms()
    }

    val bounds =
        LatLngBounds.Builder()
            .include(startLatLng)
            .include(endLatLng)
            .apply { if (gpsPoint != null) include(gpsPoint) }
            .build()

    val cameraPositionState =
        rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(bounds.center, 13f)
        }

    val coroutineScope = rememberCoroutineScope()

    // 카메라 위치 및 줌 레벨 업데이트 - 현재 위치(gpsPoint)가 업데이트될 때마다 카메라 위치와 줌 레벨을 업데이트합니다.
    LaunchedEffect(gpsPoint) {
        if (gpsPoint != null) {
            gpsMarkerState.position = gpsPoint
            coroutineScope.launch {
                val update = CameraUpdateFactory.newLatLngZoom(gpsPoint, 20f)
                cameraPositionState.animate(update, 1000)
            }
        }
    }

    // 카메라 회전 조정 - 현재 위치(gpsPoint)와 가장 가까운 경로 지점을 기준으로 카메라를 회전시킵니다.
    LaunchedEffect(gpsPoint, endLatLng) {
        if (gpsPoint != null) {
            coroutineScope.launch {
                val nearestPoint =
                    findNearestPoint(gpsPoint, walkDataList1.points + polylineDataList.flatMap { it.points })
                val nextPoint =
                    findNextPoint(nearestPoint, walkDataList1.points + polylineDataList.flatMap { it.points })
                if (nearestPoint != null && nextPoint != null) {
                    val bearing = calculateBearing(gpsPoint, nextPoint)
                    val cameraPosition = CameraPosition(gpsPoint, 20f, 55f, bearing)
                    val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
                    cameraPositionState.animate(update, 1000)
                }
            }
        }
    }

    val dotPattern = listOf(Dot(), Gap(10f))

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = modifier,
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings,
        ) {
            Polyline(points = walkDataList1.points, color = walkDataList1.color, width = 40f)
            Polyline(points = dottedPolylineList1, color = Color.Blue, width = 40f, pattern = dotPattern)
            polylineDataList.forEach { polylineData ->
                Polyline(points = polylineData.points, color = polylineData.color, width = 40f)
            }
            Polyline(points = dottedPolylineList2, color = Color.Blue, width = 40f, pattern = dotPattern)
            Polyline(points = walkDataList2.points, color = walkDataList2.color, width = 40f)
            Marker(
                icon = BitmapDescriptorFactory.fromResource(R.drawable.departurepin),
                state = startMarkerState,
                title = "출발지",
                anchor = Offset(0.5f, 0.5f),
            )
            Marker(
                icon = BitmapDescriptorFactory.fromResource(R.drawable.arrivalpin),
                state = endMarkerState,
                title = "도착지",
                anchor = Offset(0.5f, 0.5f),
            )
            if (gpsPoint != null) {
                Marker(
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.linepoint),
                    state = gpsMarkerState,
                    title = "현재 위치",
                    anchor = Offset(0.5f, 0.5f),
                )
            }
            if (isRestroom) {
                restrooms.forEach { restroom ->
                    val position = LatLng(restroom.coordinate.latitude, restroom.coordinate.longitude)
                    Marker(
                        icon = BitmapDescriptorFactory.fromResource(R.drawable.toilet),
                        state = rememberMarkerState(position = position),
                        title = restroom.station_fullname,
                        snippet = "Floor: ${restroom.floor}, Location: ${restroom.restroom_location}",
                    )
                }
            }
        }
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End,
        ) {
            FloatingActionButton(
                onClick = { openDialogState.value = !openDialogState.value },
                shape = CircleShape,
                containerColor = Color.White,
                contentColor = Color.Black,
                modifier = Modifier.padding(top = 16.dp),
            ) {
                val closeIcon: Painter = painterResource(id = R.drawable.close)
                Image(painter = closeIcon, contentDescription = "Close Icon", modifier = Modifier.padding(8.dp))
            }

            val containerColor = if (isRestroom) CustomBlue else Color.Gray
            FloatingActionButton(
                onClick = { isRestroom = !isRestroom },
                shape = CircleShape,
                containerColor = containerColor,
                contentColor = Color.White,
            ) {
                val familyRestroomIcon: Painter = painterResource(id = R.drawable.family_restroom)
                Image(
                    painter = familyRestroomIcon,
                    contentDescription = "Disabled Restroom Icon",
                    modifier = Modifier.size(45.dp),
                )
            }

            var isPressed by remember { mutableStateOf(false) }

            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        isPressed = true // 클릭 시 isPressed를 true로 설정
                        gpsPoint?.let {
                            val nearestPoint =
                                findNearestPoint(it, walkDataList1.points + polylineDataList.flatMap { it.points })
                            val nextPoint =
                                @Suppress("ktlint:standard:max-line-length")
                                findNextPoint(
                                    nearestPoint,
                                    walkDataList1.points + polylineDataList.flatMap { it.points },
                                )
                            if (nearestPoint != null && nextPoint != null) {
                                val bearing = calculateBearing(it, nextPoint)
                                val cameraPosition = CameraPosition(it, 20f, 55f, bearing)
                                val update = CameraUpdateFactory.newCameraPosition(cameraPosition)
                                cameraPositionState.animate(update, 1000)
                            }
                        }
                        isPressed = false // 작업이 완료된 후 isPressed를 false로 설정
                    }
                },
                shape = CircleShape,
                containerColor = if (isPressed) CustomPrimary else Color.White,
                contentColor = Color.White,
            ) {
                val targetIcon: Painter = painterResource(id = R.drawable.target2)
                Image(painter = targetIcon, contentDescription = "GPS Icon")
            }
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
    currentLocationViewModel: CurrentLocationViewModel,
) {
    val detailRouteInfo by navigationViewModel.detailRouteInfo.observeAsState(emptyList())

    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContent = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(300.dp).background(Color.White),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    detailRouteInfo.forEach { routeInfo ->
                        val iconResource = if (routeInfo.type == "subway") R.drawable.subway else R.drawable.walk
                        item {
                            routeInfo.info.forEach { detail ->
                                Box(
                                    modifier =
                                        Modifier.fillMaxWidth().height(100.dp).padding(16.dp).drawBottomBorder(
                                            borderColor = Color.LightGray,
                                            borderWidth = 1f,
                                        ),
                                ) {
                                    Row {
                                        Icon(
                                            painter = painterResource(id = iconResource),
                                            contentDescription = "${routeInfo.type} Icon",
                                            modifier = Modifier.size(24.dp).padding(end = 8.dp),
                                            tint = Color.Unspecified,
                                        )
                                        Text(text = detail, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            sheetPeekHeight = 30.dp,
        ) {
            GoogleMapGuide(
                modifier = Modifier,
                navigationViewModel = navigationViewModel,
                openDialogState = openDialogState,
                currentLocationViewModel = currentLocationViewModel,
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
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokeWidth,
            )
        },
    )

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

fun findNearestPoint(
    current: LatLng,
    path: List<LatLng>,
): LatLng? {
    return path.minByOrNull { distanceBetween(current, it) }
}

fun findNextPoint(
    currentPoint: LatLng?,
    path: List<LatLng>,
): LatLng? {
    currentPoint ?: return null
    val currentIndex = path.indexOf(currentPoint)
    return if (currentIndex != -1 && currentIndex < path.size - 1) {
        path[currentIndex + 1]
    } else if (currentIndex > 0) {
        path[currentIndex - 1]
    } else {
        null
    }
}

fun distanceBetween(
    point1: LatLng,
    point2: LatLng,
): Double {
    val lat1 = Math.toRadians(point1.latitude)
    val lng1 = Math.toRadians(point1.longitude)
    val lat2 = Math.toRadians(point2.latitude)
    val lng2 = Math.toRadians(point2.longitude)

    val dLat = lat2 - lat1
    val dLng = lng2 - lng1

    val a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(lat1) * Math.cos(lat2) *
            Math.sin(dLng / 2) * Math.sin(dLng / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

    val radius = 6371e3 // 지구의 반지름 (미터)
    return radius * c
}

@Preview(showBackground = true)
@Composable
fun PreviewGuideScreen() {
    GuideScreen(
        navController = rememberNavController(),
        navigationViewModel = NavigationViewModel(),
        currentLocationViewModel = CurrentLocationViewModel(),
    )
}

fun startFrequentLocationUpdates(
    context: Context,
    currentLocationViewModel: CurrentLocationViewModel,
) {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    val locationRequest =
        LocationRequest.create().apply {
            interval = 10000 // 10초
            fastestInterval = 5000 // 5초
            priority = Priority.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 5000 // 5초
        }

    val locationCallback =
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.locations.isEmpty()) return

                locationResult.locations.lastOrNull()?.let { location ->
                    val latitude = location.latitude.toString()
                    val longitude = location.longitude.toString()
                    Log.d("LocationUpdates", "Updated Latitude: $latitude, Longitude: $longitude")
                    currentLocationViewModel.setLocation(latitude, longitude)
                }
            }
        }

    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
}
