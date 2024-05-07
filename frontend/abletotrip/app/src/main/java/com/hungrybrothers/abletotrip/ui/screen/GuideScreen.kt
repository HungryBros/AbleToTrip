package com.hungrybrothers.abletotrip.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import com.hungrybrothers.abletotrip.ui.theme.CustomBackground
import com.hungrybrothers.abletotrip.ui.viewmodel.NavigationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(navController: NavController) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val navigationViewModel: NavigationViewModel = viewModel()
    Surface(modifier = Modifier, color = CustomBackground) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            GuideBottomSheet(
                modifier = Modifier,
                scaffoldState = scaffoldState,
                navigationViewModel = navigationViewModel,
            )
        }
    }
}

@Composable
fun GoogleMapGuide(
    modifier: Modifier,
    navigationViewModel: NavigationViewModel,
) {
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

    LaunchedEffect(key1 = true) {
        permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
        )
    }

//    val decodedpolyline = parseCoordinates(jsonData)
    val mystartpoint = LatLng(37.501286, 127.0396029)
    val myendpoint = LatLng(37.579617, 126.977041)

    val bounds =
        LatLngBounds.Builder()
            .include(mystartpoint)
            .include(myendpoint)
            .include(gpsPoint)
            .build()

    val gpsMarkerState = remember(gpsPoint) { MarkerState(position = gpsPoint) }

    val cameraPositionState =
        rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(bounds.center, 13f)
        }
    LaunchedEffect(gpsPoint) {
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(gpsPoint, 20f))
    }

    val polylineDataList by navigationViewModel.polylineDataList.observeAsState(initial = emptyList())

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
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
        Marker(
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
            state = gpsMarkerState,
            title = "현재위치",
            snippet = "Here is the GPS point",
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideBottomSheet(
    modifier: Modifier,
    scaffoldState: BottomSheetScaffoldState,
    navigationViewModel: NavigationViewModel,
) {
    val detailRouteInfo by navigationViewModel.detailRouteInfo.observeAsState()
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
                detailRouteInfo?.forEach { routeInfo ->
                    routeInfo.info.forEach { detail ->
                        item {
                            Text(
                                text = "${routeInfo.type}  $detail",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        },
        sheetPeekHeight = 60.dp, // Sheet가 보일 때의 최소 높이
    ) {
        GoogleMapGuide(modifier = Modifier, navigationViewModel = NavigationViewModel())
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGuideScreen() {
    // rememberNavController를 사용하여 Preview에서 NavController를 제공합니다.
    GuideScreen(navController = rememberNavController())
}
