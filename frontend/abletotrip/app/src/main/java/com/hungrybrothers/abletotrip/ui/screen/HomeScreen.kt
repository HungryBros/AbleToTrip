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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.hungrybrothers.abletotrip.KakaoAuthViewModel
import com.hungrybrothers.abletotrip.R
import com.hungrybrothers.abletotrip.ui.components.CategorySecond
import com.hungrybrothers.abletotrip.ui.components.HeaderBar
import com.hungrybrothers.abletotrip.ui.components.SearchBar
import com.hungrybrothers.abletotrip.ui.datatype.Attraction
import com.hungrybrothers.abletotrip.ui.datatype.Catalog2Attraction
import com.hungrybrothers.abletotrip.ui.navigation.NavRoute
import com.hungrybrothers.abletotrip.ui.network.AttractionsRepository
import com.hungrybrothers.abletotrip.ui.network.Catalog2Repository
import com.hungrybrothers.abletotrip.ui.network.ShowMoreInfoRepository
import com.hungrybrothers.abletotrip.ui.viewmodel.Catalog2ViewModel
import com.hungrybrothers.abletotrip.ui.viewmodel.CurrentLocationViewModel
import com.hungrybrothers.abletotrip.ui.viewmodel.HomeViewModel
import com.hungrybrothers.abletotrip.ui.viewmodel.ShowMoreViewModel
import java.util.Locale

data class IconData(
    val label: String,
    val icon: Painter,
)

@Composable
fun HomeScreen(
    navController: NavController, // 화면 간 이동을 위한 NavController
    currentLocationViewModel: CurrentLocationViewModel,
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
    val showMoreViewModel: ShowMoreViewModel =
        remember {
            val repository = ShowMoreInfoRepository()
            ShowMoreViewModel(repository)
        }

    val context = LocalContext.current
    val kakaoAuthViewModel: KakaoAuthViewModel = viewModel()
    var searchText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
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
                    startLocationUpdates(context, currentLocationViewModel)
                } else {
                    currentLocationViewModel.setLocation(null, null)
                }
            },
        )

    LaunchedEffect(Unit) {
        when (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            PackageManager.PERMISSION_GRANTED -> startLocationUpdates(context, currentLocationViewModel)
            else -> locationPermissionRequest.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Observe location data without default values
    val latitude by currentLocationViewModel.latitude.observeAsState(null)
    val longitude by currentLocationViewModel.longitude.observeAsState(null)

    // UI 시작
    val selectedCategories = remember { mutableStateOf(mutableListOf<String>()) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (latitude != null && longitude != null) {
            Column(
                modifier =
                    Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HeaderBar(navController = navController, false)
                SearchBar(
                    text = searchText,
                    onValueChange = { newSearchText ->
                        searchText = newSearchText
                    },
                    onSearch = {
                        if (searchText.isNotEmpty()) {
                            navController.navigate("${NavRoute.SEARCH.routeName}/$searchText")
                        }
                    },
                    placeholder = "검색어를 입력해주세요.",
                    onClear = {
                        searchText = ""
                        keyboardController?.hide() // 키보드를 숨깁니다.
                    },
                )

                CategorySelector(categories, selectedCategories.value) { updatedSelectedCategories ->
                    val selectedString = updatedSelectedCategories.joinToString("-")
                    catalog2ViewModel.fetchCatalog2Data(latitude!!, longitude!!, selectedString, 1)
                    selectedCategories.value = updatedSelectedCategories.toMutableList()
                }
                when {
                    selectedCategories.value.isNotEmpty() ->
                        DisplayCustomAttractionsScreen(
                            catalog2ViewModel,
                            navController,
                        )
                    else -> DisplayAttractionsScreen(homeViewModel, navController, latitude!!, longitude!!)
                }
            }
        } else {
            // Show a loading screen or message if location data is not ready
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        LogoutButton(navController, kakaoAuthViewModel, Modifier.align(Alignment.BottomEnd))
    }
}

@Composable
fun LogoutButton(
    navController: NavController,
    kakaoAuthViewModel: KakaoAuthViewModel,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        TextButton(
            onClick = {
                kakaoAuthViewModel.clearAuthData()
                navController.navigate(NavRoute.LOGIN.routeName) {
                    popUpTo(NavRoute.HOME.routeName) { inclusive = true }
                }
            },
        ) {
            Text("로그아웃", style = MaterialTheme.typography.bodyMedium)
        }
    }
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

    if (attractionsData != null) {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
        ) {
            items(attractionsData!!.attractions) { attraction ->
                NewAttractionItem(attraction, navController)
            }
        }
    } else {
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
                    text = "거리: ${attraction.distance}km",
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
    latitude: String,
    longitude: String,
) {
    LaunchedEffect(latitude, longitude) {
        viewModel.loadPlaceData(latitude, longitude)
    }

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
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            onClick = { navController.navigate("${NavRoute.SHOWMORE.routeName}/$category") },
                        ) {
                            Text(text = "더보기 >", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (attractions.isNotEmpty()) {
                        LazyRow(modifier = Modifier.padding(horizontal = 8.dp)) {
                            items(attractions) { attraction ->
                                AttractionItem(attraction, navController)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    } else {
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
            Text(text = "거리: ${attraction.distance}km", style = MaterialTheme.typography.bodySmall)
        }
    }
}

fun startLocationUpdates(
    context: Context,
    currentLocationViewModel: CurrentLocationViewModel,
) {
    if (ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    val locationRequest =
        LocationRequest.create().apply {
            interval = 300000 // 5분
            fastestInterval = 300000
            priority = Priority.PRIORITY_HIGH_ACCURACY
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
