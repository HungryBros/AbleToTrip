package com.hungrybrothers.abletotrip.ui.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.hungrybrothers.abletotrip.BuildConfig
import com.hungrybrothers.abletotrip.ui.components.HeaderBar
import com.hungrybrothers.abletotrip.ui.components.SearchBar
import com.hungrybrothers.abletotrip.ui.datatype.Catalog2Attraction
import com.hungrybrothers.abletotrip.ui.navigation.NavRoute
import com.hungrybrothers.abletotrip.ui.network.ShowMoreInfoRepository
import com.hungrybrothers.abletotrip.ui.theme.CustomPrimary
import com.hungrybrothers.abletotrip.ui.viewmodel.CurrentLocationViewModel
import com.hungrybrothers.abletotrip.ui.viewmodel.ShowMoreViewModel

val categoryTranslations2 =
    mapOf(
        "nearby" to "내 주변 여행지",
        "exhibition-performance" to "전시/공연",
        "leisure-park" to "레저/공원",
        "culture-famous" to "문화관광/명소",
    )

@Composable
fun ShowMoreScreen(
    navController: NavController,
    category: String,
    currentLocationViewModel: CurrentLocationViewModel,
) {
    val showMoreViewModel: ShowMoreViewModel =
        remember {
            val repository = ShowMoreInfoRepository()
            ShowMoreViewModel(repository)
        }

    val latitude by currentLocationViewModel.latitude.observeAsState(null)
    val longitude by currentLocationViewModel.longitude.observeAsState(null)
    Log.d("ShowMoreScreen", "latitude = $latitude, longitude = $longitude")

    // 검색창 관련 변수 선언
    var searchText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(latitude, longitude) {
        if (latitude != null && longitude != null) {
            showMoreViewModel.loadInitialData(latitude!!, longitude!!, category)
        }
    }

    Column(
        modifier =
            Modifier
                .padding(16.dp)
                .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HeaderBar(navController, true, true)
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
                keyboardController?.hide()
            },
        )
        Text(
            text = "${categoryTranslations2[category]} 관광지",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
        )
        if (latitude != null && longitude != null) {
            DisplayMoreAttractionsScreen(
                viewModel = showMoreViewModel,
                navController = navController,
                latitude = latitude!!,
                longitude = longitude!!,
                category = category,
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = CustomPrimary)
            }
        }
    }
}

@Composable
fun DisplayMoreAttractionsScreen(
    viewModel: ShowMoreViewModel,
    navController: NavController,
    latitude: String,
    longitude: String,
    category: String,
) {
    val attractionsData by viewModel.showmoreData.collectAsState(null)
    val listState = rememberLazyListState()

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
        state = listState,
    ) {
        if (attractionsData != null) {
            items(attractionsData!!.attractions) { attraction ->
                MoreAttractionItem(attraction, navController)
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                val lastVisibleItem = visibleItems.lastOrNull()
                val totalItems = listState.layoutInfo.totalItemsCount
                if (lastVisibleItem != null && lastVisibleItem.index >= totalItems - 1) {
                    viewModel.loadMoreData(latitude, longitude, category)
                }
            }
    }
}

@Composable
fun MoreAttractionItem(
    attraction: Catalog2Attraction,
    navController: NavController,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RectangleShape,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .clickable(onClick = { navController.navigate("detail/${attraction.id}") }),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = BuildConfig.S3_BASE_URL + attraction.image_url),
                contentDescription = attraction.attraction_name,
                modifier =
                    Modifier
                        .size(120.dp)
                        .fillMaxSize(1f)
                        .aspectRatio(5f / 4f),
                contentScale = ContentScale.Crop,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f).padding(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                ) {
                    // 관광지 이름
                    Text(
                        text = attraction.attraction_name,
                        modifier = Modifier.padding(horizontal = 8.dp).weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    // 카테고리
                    Text(
                        text = "${attraction.category2}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp), // 이름과 간격 유지
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "위치",
                        modifier = Modifier.padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                    Text(
                        text = "${attraction.si}, ${attraction.gu} ${attraction.dong}",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "운영시간",
                        modifier = Modifier.padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                    Text(
                        text = attraction.operation_hours,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "휴무일",
                        modifier = Modifier.padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                    Text(
                        text = attraction.closed_days,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "입장료",
                        modifier = Modifier.padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                    Text(
                        text = if (attraction.is_entrance_fee) "유료" else "무료", // 입장료 상태에 따라 텍스트 변경
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
