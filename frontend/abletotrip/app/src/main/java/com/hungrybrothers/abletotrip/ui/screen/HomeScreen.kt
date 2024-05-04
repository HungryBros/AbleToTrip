package com.hungrybrothers.abletotrip.ui.screen

import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.hungrybrothers.abletotrip.R
import com.hungrybrothers.abletotrip.ui.components.CategorySecond
import com.hungrybrothers.abletotrip.ui.components.HeaderBar
import com.hungrybrothers.abletotrip.ui.datatype.Attraction
import com.hungrybrothers.abletotrip.ui.datatype.Catalog2Attraction
import com.hungrybrothers.abletotrip.ui.navigation.NavRoute
import com.hungrybrothers.abletotrip.ui.network.AttractionsRepository
import com.hungrybrothers.abletotrip.ui.network.Catalog2Repository
import com.hungrybrothers.abletotrip.ui.viewmodel.Catalog2ViewModel
import com.hungrybrothers.abletotrip.ui.viewmodel.HomeViewModel
import java.util.Locale

data class IconData(
    val label: String,
    val icon: Painter,
)

@Composable
fun HomeScreen(
    navController: NavController, // 화면 간 이동을 위한 NavController
) {
    val homeViewModel: HomeViewModel =
        remember {
            val repository = AttractionsRepository()
            HomeViewModel(repository)
        }
    val catalog2ViewModel: Catalog2ViewModel =
        remember {
            val repository = Catalog2Repository()
            Catalog2ViewModel(repository)
        }
    val context = LocalContext.current
    val searchText = remember { mutableStateOf("") }
    val categories =
        listOf(
            IconData("park", painterResource(id = R.drawable.park)),
            IconData("tour", painterResource(id = R.drawable.sunrise)),
            IconData("leisure", painterResource(id = R.drawable.leisure)),
            IconData("sports", painterResource(id = R.drawable.framed_picture)),
            IconData("beauty", painterResource(id = R.drawable.palette)),
            IconData("perform", painterResource(id = R.drawable.ticket)),
            IconData("exhibit", painterResource(id = R.drawable.stadium)),
        )

    // 권한 요청을 처리할 런처 설정
    val locationPermissionRequest =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted: Boolean ->
                if (isGranted) {
                    // 권한이 허가되면 위치 업데이트 시작
                    startLocationUpdates(context, homeViewModel)
                } else {
                    // 권한이 거부되면 기본 위치로 데이터 요청
                    homeViewModel.loadPlaceData("37.5665", "126.9780")
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
                startLocationUpdates(context, homeViewModel)
            }
            else -> {
                // 권한이 허가되지 않았다면 권한 요청
                locationPermissionRequest.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
    // UI시작
    val selectedCategories = remember { mutableStateOf(mutableListOf<String>()) }
    println("Catalog2DataselectedCategories : $selectedCategories")

    Column {
        HeaderBar(navController = navController, false)
        InSearchBar(
            text = searchText,
            onSearch = {
                // 검색 로직 구현, 예를 들어 데이터베이스 쿼리나 API 호출
                Log.d("Search", "Searching for: ${searchText.value}")
                // 검색 결과 화면으로 이동하거나 검색 결과를 표시
            },
            placeholder = "검색창",
        )
        CategorySelector(categories, selectedCategories.value) { updatedSelectedCategories ->
            val selectedString = updatedSelectedCategories.joinToString("-")
            catalog2ViewModel.fetchCatalog2Data("37.5665", "126.9780", selectedString, 1)
            selectedCategories.value = updatedSelectedCategories.toMutableList()
        }
        when {
            selectedCategories.value.isNotEmpty() -> {
                DisplayCustomAttractionsScreen(catalog2ViewModel, navController)
                Log.d("Catalog2Data화면 커스텀", "커스텀 스크린: ${selectedCategories.value}")
            }
            else -> {
                DisplayAttractionsScreen(homeViewModel, navController)
                Log.d("Catalog2Data화면 일반", "일반 스크린")
                Log.d("Catalog2Data화면 일반", "일반 스크린: ${selectedCategories.value}")
            }
        }
    }
}

@Composable
fun InSearchBar(
    text: MutableState<String>,
    onSearch: () -> Unit,
    placeholder: String,
) {
    OutlinedTextField(
        value = text.value,
        onValueChange = { newValue -> text.value = newValue },
        placeholder = { Text(text = placeholder) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions =
            KeyboardActions(onSearch = {
                onSearch()
            }),
    )
}

// 카탈로그 선택부분 (상단)
@Composable
fun CategorySelector(
    categories: List<IconData>,
    initialSelectedCategories: List<String>,
    onSelectCategory: (List<String>) -> Unit,
) {
    val selectedState = remember { mutableStateOf(initialSelectedCategories.toList()) }

    LazyRow {
        items(categories) { category ->
            val isSelected = category.label in selectedState.value
            CategorySecond(
                icon = category.icon,
                label = category.label,
                isSelected = isSelected,
                onSelect = {
                    val newSelectedState = selectedState.value.toMutableList()
                    if (isSelected) {
                        newSelectedState.remove(category.label)
                    } else {
                        newSelectedState.add(category.label)
                    }
                    selectedState.value = newSelectedState
                    onSelectCategory(newSelectedState)
                },
            )
        }
    }
}

// 카탈로그 선택시 화면 구성 - newattraction 포함
@Composable
fun DisplayCustomAttractionsScreen(
    viewModel: Catalog2ViewModel,
    navController: NavController,
) {
    val attractionsData by viewModel.catalog2Data.collectAsState()

    // Null 체크 후 attractions 리스트를 LazyColumn에 전달
    if (attractionsData != null) {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
        ) {
            items(attractionsData!!.attractions) { attraction ->
                Log.d("Catalog", "로드 완료$attraction")
                NewAttractionItem(attraction, navController)
            }
        }
    } else {
        Log.e("Catalog", "로스실패")
        // 데이터가 null이면 로딩 표시 또는 비어 있는 상태 표시
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("데이터를 불러오는 중...")
        }
    }
}

// 카탈로그 선택시 화면의 카드
@Composable
fun NewAttractionItem(
    attraction: Catalog2Attraction,
    navController: NavController,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .clickable(onClick = { navController.navigate("${NavRoute.DETAIL.routeName}/${attraction.id}") }),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = attraction.image_url),
                contentDescription = "Attraction Image",
                modifier =
                    Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = attraction.attraction_name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "${attraction.si}, ${attraction.gu}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
                Text(
                    text = "거리: ${attraction.distance}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

// 기본화면 구성
@Composable
fun DisplayAttractionsScreen(
    viewModel: HomeViewModel,
    navController: NavController,
) {
    val attractionsData by viewModel.placeData.observeAsState()

    if (attractionsData?.attractions?.isNotEmpty() == true) {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
        ) {
            attractionsData!!.attractions.forEach { (category, attractions) ->
                item {
                    // 카테고리 제목과 더보기 버튼을 같은 행에 배치
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text =
                                category.replace('-', ' ')
                                    .replaceFirstChar {
                                        if (it.isLowerCase()) {
                                            it.titlecase(
                                                Locale.ROOT,
                                            )
                                        } else {
                                            it.toString()
                                        }
                                    },
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.weight(1f), // 카테고리 제목이 더 많은 공간을 차지
                        )
                        // 더보기 버튼
                        TextButton(
                            onClick = { /* TODO: 네비게이션 로직 추가 */ },
                        ) {
                            Text(text = "더보기 >", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // 관광지가 있을 경우 LazyRow로 표시, 없을 경우 아름다운 메시지 표시
                    if (attractions.isNotEmpty()) {
                        LazyRow(modifier = Modifier.padding(horizontal = 8.dp)) {
                            items(attractions) { attraction ->
                                AttractionItem(attraction, navController)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    } else {
                        // 목록이 없을 경우의 메시지를 아름답게 표시
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "근처에 해당 목록이 없습니다.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier =
                                    Modifier
                                        .align(Alignment.Center)
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(8.dp),
                                        )
                                        .padding(8.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

// 기본화면 구성의 카드형식
@Composable
fun AttractionItem(
    attraction: Attraction,
    navController: NavController,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .clickable(onClick = { navController.navigate("${NavRoute.DETAIL.routeName}/${attraction.id}") }),
        elevation =
            CardDefaults.elevatedCardElevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp,
                focusedElevation = 3.dp,
                hoveredElevation = 3.dp,
            ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = rememberAsyncImagePainter(model = attraction.image_url),
                contentDescription = "Attraction Image",
                modifier =
                    Modifier
                        .size(100.dp)
                        .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.height(16.dp))
            Text(text = attraction.attraction_name, style = MaterialTheme.typography.titleMedium)
            Text(text = "${attraction.si}, ${attraction.gu}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "거리: ${attraction.distance}m", style = MaterialTheme.typography.bodySmall)
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
            interval = 30000 // 활성 위치 업데이트를 위한 원하는 간격을 밀리초 단위로 설정
            fastestInterval = 30000 // 위치 업데이트를 받을 수 있는 가장 빠른 간격을 설정
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
