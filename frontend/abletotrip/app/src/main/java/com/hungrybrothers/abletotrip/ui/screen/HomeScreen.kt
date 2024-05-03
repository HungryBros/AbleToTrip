package com.hungrybrothers.abletotrip.ui.screen

import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.hungrybrothers.abletotrip.ui.datatype.Attraction
import com.hungrybrothers.abletotrip.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    navController: NavController, // 화면 간 이동을 위한 NavController
    viewModel: HomeViewModel = viewModel(), // 데이터 처리와 비즈니스 로직을 관리하는 ViewModel
) {
    val context = LocalContext.current // Composable 함수가 실행되는 현재 컨텍스트를 가져옴

    // 권한 요청을 처리할 런처 설정
    val locationPermissionRequest =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted: Boolean ->
                if (isGranted) {
                    // 권한이 허가되면 위치 업데이트 시작
                    startLocationUpdates(context, viewModel)
                } else {
                    // 권한이 거부되면 기본 위치로 데이터 요청
                    viewModel.loadPlaceData("37.5665", "126.9780")
                }
            },
        )

    LaunchedEffect(Unit) {
        when (
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            )
        ) {
            PackageManager.PERMISSION_GRANTED -> {
                // 위치 권한이 이미 허가되었다면 위치 업데이트 즉시 시작
                startLocationUpdates(context, viewModel)
            }
            else -> {
                // 권한이 허가되지 않았다면 권한 요청
                locationPermissionRequest.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
    // UI시작
    DisplayAttractionsScreen(viewModel)
}

@Composable
fun DisplayAttractionsScreen(viewModel: HomeViewModel) {
    // observeAsState()를 사용하며 기본값 null을 제공합니다.
    val attractionsData by viewModel.placeData.observeAsState()

    // attractionsData의 null 체크와 isEmpty 체크를 명시적으로 수행
    if (attractionsData?.attractions?.isNotEmpty() == true) {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
        ) {
            attractionsData!!.attractions.forEach { (category, attractions) ->
                item {
                    Text(
                        text = category.replace('-', ' ').capitalize(),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    attractions.forEach { attraction ->
                        AttractionItem(attraction)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator() // 로딩 인디케이터 표시
        }
    }
}

@Composable
fun AttractionItem(attraction: Attraction) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(4.dp),
        elevation =
            CardDefaults.elevatedCardElevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp, // 예를 들어, 카드가 눌렸을 때의 elevation
                focusedElevation = 3.dp, // 포커스가 맞춰졌을 때의 elevation
                hoveredElevation = 3.dp, // 호버링 상태일 때의 elevation
            ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = attraction.image_url),
                contentDescription = "Attraction Image",
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(text = attraction.attraction_name, style = MaterialTheme.typography.titleMedium)
                Text(text = "${attraction.si}, ${attraction.gu}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Distance: ${attraction.distance}m", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// 시작위치 업데이트
fun startLocationUpdates(
    context: Context,
    viewModel: HomeViewModel,
) {
    if (ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // 권한이 없다면 로직을 종료하거나 권한 요청을 진행
        return
    }

    // 컨텍스트에 대한 위치 서비스 클라이언트를 가져옴
    val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    // 위치 요청을 구성
    val locationRequest =
        LocationRequest.create().apply {
            interval = 300000 // 활성 위치 업데이트를 위한 원하는 간격을 밀리초 단위로 설정
            fastestInterval = 300000 // 위치 업데이트를 받을 수 있는 가장 빠른 간격을 설정
            priority = Priority.PRIORITY_HIGH_ACCURACY // 최고 정확도 설정
        }

    // 위치 업데이트를 받을 콜백 생성
    val locationCallback =
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.locations.isEmpty()) return // 위치 결과가 없으면 아무것도 하지 않고 반환

                // 마지막 위치 정보를 사용
                locationResult.locations.lastOrNull()?.let { location ->
                    viewModel.loadPlaceData(location.latitude.toString(), location.longitude.toString())
                }
            }
        }

    // 정의한 설정으로 위치 업데이트 요청
    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    // rememberNavController를 사용하여 미리보기에서 NavController 제공
    HomeScreen(navController = rememberNavController())
}
