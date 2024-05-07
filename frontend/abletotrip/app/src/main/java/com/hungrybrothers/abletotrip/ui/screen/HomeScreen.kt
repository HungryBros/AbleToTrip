package com.hungrybrothers.abletotrip.ui.screen

import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.hungrybrothers.abletotrip.ui.datatype.Attraction
import com.hungrybrothers.abletotrip.ui.network.AttractionsRepository
import com.hungrybrothers.abletotrip.ui.viewmodel.HomeViewModel
import java.util.Locale

@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel =
        remember {
            val repository = AttractionsRepository()
            HomeViewModel(repository)
        }
    val context = LocalContext.current
    val categories = listOf("nearby", "exhibition", "performance", "leisure park", "culture famous")
    val selectedCategories = remember { mutableStateListOf<String>() }

    val locationPermissionRequest =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted: Boolean ->
                if (isGranted) {
                    startLocationUpdates(context, viewModel)
                } else {
                    viewModel.loadPlaceData("37.5665", "126.9780")
                }
            },
        )

    LaunchedEffect(Unit) {
        when (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            PackageManager.PERMISSION_GRANTED -> startLocationUpdates(context, viewModel)
            else -> locationPermissionRequest.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CategorySelection(categories, selectedCategories)
        DisplayAttractionsScreen(viewModel, navController, selectedCategories)
    }
}

@Composable
fun DisplayAttractionsScreen(
    viewModel: HomeViewModel,
    navController: NavController,
    selectedCategories: List<String>,
) {
    val attractionsData by viewModel.placeData.observeAsState()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(vertical = 8.dp)) {
        if (selectedCategories.isEmpty()) {
            attractionsData?.attractions?.forEach { (category, attractions) ->
                item {
                    // 카테고리 제목과 더보기 버튼을 같은 행에 배치
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text =
                                category.replace('-', ' ').replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                                },
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.weight(1f), // 카테고리 제목이 더 많은 공간을 차지
                        )
                        // 더보기 버튼
                        TextButton(
                            onClick = { /* TODO: 네비게이션 로직 추가 */ },
                        ) {
                            Text("더보기 >", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // 관광지가 있을 경우 LazyRow로 표시, 없을 경우 아름다운 메시지 표시
                    if (attractions.isNotEmpty()) {
                        LazyRow(modifier = Modifier.padding(horizontal = 8.dp)) {
                            items(attractions) { attraction ->
                                AttractionItem(attraction, onClick = {}) // Todo: 여기 로직 맞는지 모르겠음
                            }
                        }
                    } else {
                        // 목록이 없을 경우의 메시지를 아름답게 표시
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "해당 목록이 없습니다.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier =
                                    Modifier
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
        } else {
            val filteredData =
                attractionsData?.attractions
                    ?.filterKeys { it in selectedCategories }
                    ?.flatMap { entry -> entry.value.map { attraction -> entry.key to attraction } }
                    ?: listOf()

            if (filteredData.isNotEmpty()) {
                items(filteredData) { (category, attractions) ->
                    if (isSelectedCategory(category, selectedCategories)) {
                        SelectedAttractionItem(attractions) {
                            // Todo: 클릭 이벤트 처리
                            //  여기서 해당 카드의 ID 값을 사용하여 네비게이션 처리
                        }
                    } else {
                        AttractionItem(attractions) {
                            // Todo: 클릭 이벤트 처리
                            //  여기서 해당 카드의 ID 값을 사용하여 네비게이션 처리
                        }
                    }
                }
            } else {
                item {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("목록이 없습니다.", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun SelectedAttractionItem(
    attraction: Attraction,
    onClick: () -> Unit, // 클릭 이벤트를 처리할 콜백 함수 추가
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .clickable { onClick() },
        // 카드를 클릭할 때 onClick 함수 호출
        elevation =
            CardDefaults.elevatedCardElevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp,
                focusedElevation = 3.dp,
                hoveredElevation = 3.dp,
            ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = rememberAsyncImagePainter(model = attraction.image_url),
                contentDescription = "Attraction Image",
                modifier =
                    Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(attraction.attraction_name, style = MaterialTheme.typography.titleMedium)
                Text("${attraction.si}, ${attraction.gu}", style = MaterialTheme.typography.bodyMedium)
                Text("거리: ${attraction.distance}m", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun isSelectedCategory(
    category: String,
    selectedCategories: List<String>,
): Boolean {
    return category in selectedCategories
}

@Composable
fun AttractionItem(
    attraction: Attraction,
    onClick: () -> Unit, // 클릭 이벤트를 처리할 콜백 함수 추가
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .clickable { onClick() },
        // 카드를 클릭할 때 onClick 함수 호출
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
                modifier = Modifier.size(100.dp).align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.height(16.dp))
            Text(text = attraction.attraction_name, style = MaterialTheme.typography.titleMedium)
            Text(text = "${attraction.si}, ${attraction.gu}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "거리: ${attraction.distance}m", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun CategorySelection(
    categories: List<String>,
    selectedCategories: MutableList<String>,
) {
    LazyRow(modifier = Modifier.padding(8.dp)) {
        items(categories) { category ->
            val isSelected = category in selectedCategories
            CustomFilterChip(label = category, isSelected = isSelected, onSelectionChanged = { isSelected ->
                if (isSelected) {
                    selectedCategories.add(category)
                } else {
                    selectedCategories.remove(category)
                }
            })
        }
    }
}

@Composable
fun CustomFilterChip(
    label: String,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
) {
    TextButton(
        onClick = { onSelectionChanged(!isSelected) },
        colors =
            ButtonDefaults.textButtonColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            ),
        modifier =
            Modifier
                .padding(4.dp)
                .background(
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(50),
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

fun startLocationUpdates(
    context: Context,
    viewModel: HomeViewModel,
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
            interval = 300000 // 5 minutes
            fastestInterval = 300000 // 5 minutes
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

    val locationCallback =
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.lastOrNull()?.let { location ->
                    viewModel.loadPlaceData(location.latitude.toString(), location.longitude.toString())
                }
            }
        }

    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    HomeScreen(navController = rememberNavController())
}
