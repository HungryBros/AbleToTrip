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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.hungrybrothers.abletotrip.ui.components.HeaderBar
import com.hungrybrothers.abletotrip.ui.components.SearchBar
import com.hungrybrothers.abletotrip.ui.datatype.SearchResult
import com.hungrybrothers.abletotrip.ui.navigation.NavRoute
import com.hungrybrothers.abletotrip.ui.network.AttractionSearchResultRepository
import com.hungrybrothers.abletotrip.ui.viewmodel.AttractionSearchResultViewModel
import com.hungrybrothers.abletotrip.ui.viewmodel.CurrentLocationViewModel

@Composable
fun SearchScreen(
    navController: NavController,
    keyword: String,
    currentLocationViewModel: CurrentLocationViewModel,
) {
    val attractionSearchResultViewModel: AttractionSearchResultViewModel =
        remember {
            val repository = AttractionSearchResultRepository()
            AttractionSearchResultViewModel(repository)
        }
    val latitude by currentLocationViewModel.latitude.observeAsState(null)
    val longitude by currentLocationViewModel.longitude.observeAsState(null)
    Log.d("SearchScreen", "latitude = $latitude, longitude = $longitude")

    LaunchedEffect(key1 = keyword, key2 = latitude, key3 = longitude) {
        if (latitude != null && longitude != null) {
            attractionSearchResultViewModel.fetchInitialData(latitude!!, longitude!!, keyword)
        }
    }

    // 관광지 검색 키워드
    var searchText by remember { mutableStateOf(keyword) }
    // 키보드
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HeaderBar(navController = navController, showBackButton = false)
        // 검색창
        SearchBar(
            text = searchText,
            onValueChange = { newSearchText ->
                searchText = newSearchText
            },
            placeholder = "관광지를 검색해보세요",
            onClear = {
                searchText = ""
                keyboardController?.hide()
            },
            onSearch = {
                if (searchText.isNotEmpty()) {
                    navController.navigate("${NavRoute.SEARCH.routeName}/$searchText")
                }
            },
        )
        DisplaySearchResultScreen(attractionSearchResultViewModel, navController, keyword, latitude, longitude)
    }
}

@Composable
fun DisplaySearchResultScreen(
    viewModel: AttractionSearchResultViewModel,
    navController: NavController,
    keyword: String,
    latitude: String?,
    longitude: String?,
) {
    val searchResultData by viewModel.attractionSearchResultData.collectAsState()
    val listState = rememberLazyListState()

    // Null 체크 후 attractions 리스트를 LazyColumn에 전달
    if (searchResultData != null) {
        Column {
            Text(
                text = "검색 결과: ${searchResultData?.counts}개",
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp),
                state = listState,
            ) {
                items(searchResultData!!.attractions!!) { attraction ->
                    Log.d("Catalog", "로드 완료$attraction")
                    SearchResultItem(attraction, navController)
                }

                item {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("로딩 중...")
                    }
                }
            }
        }
    } else {
        Log.e("Catalog", "로드 실패")
        // 데이터가 null이면 로딩 표시 또는 비어 있는 상태 표시
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("검색 결과가 없습니다.")
        }
    }

    if (latitude != null && longitude != null) {
        LaunchedEffect(listState) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo }
                .collect { visibleItems ->
                    val lastVisibleItem = visibleItems.lastOrNull()
                    val totalItems = listState.layoutInfo.totalItemsCount
                    if (lastVisibleItem != null && lastVisibleItem.index >= totalItems - 1) {
                        viewModel.fetchMoreData(latitude, longitude, keyword)
                    }
                }
        }
    }
}

@Composable
fun SearchResultItem(
    attraction: SearchResult,
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
                painter = rememberAsyncImagePainter(model = attraction.image_url),
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
                modifier = Modifier.weight(1f),
            ) {
                // 텍스트를 나란히 표시하기 위해 Row 사용
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // 관광지 이름
                    Text(
                        text = "${ attraction.attraction_name }",
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
                // 거리
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
                        text = "${attraction.operation_hours}",
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
                        text = "${attraction.closed_days}",
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
                        text = if (attraction.is_entrance_fee == true) "유료" else "무료", // 입장료 상태에 따라 텍스트 변경
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
