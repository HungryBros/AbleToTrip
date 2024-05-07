package com.hungrybrothers.abletotrip.ui.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.hungrybrothers.abletotrip.ui.components.HeaderBar
import com.hungrybrothers.abletotrip.ui.components.SearchBar
import com.hungrybrothers.abletotrip.ui.datatype.SearchResult
import com.hungrybrothers.abletotrip.ui.navigation.NavRoute
import com.hungrybrothers.abletotrip.ui.network.AttractionSearchResultRepository
import com.hungrybrothers.abletotrip.ui.viewmodel.AttractionSearchResultViewModel

@Composable
fun SearchScreen(
    navController: NavController,
    keyword: String,
) {
    val attractionSearchResultViewModel: AttractionSearchResultViewModel =
        remember {
            val repository = AttractionSearchResultRepository()
            AttractionSearchResultViewModel(repository)
        }

    LaunchedEffect(key1 = keyword) {
        attractionSearchResultViewModel.fetchAttractionSearchResultData(keyword)
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
        DisplaySearchResultScreen(attractionSearchResultViewModel, navController)
    }
}

@Composable
fun DisplaySearchResultScreen(
    viewModel: AttractionSearchResultViewModel,
    navController: NavController,
) {
    val searchResultData by viewModel.attractionSearchResultData.collectAsState()

    // Null 체크 후 attractions 리스트를 LazyColumn에 전달
    if (searchResultData != null) {
        Text(
            text = "검색 결과: ${searchResultData?.counts}개",
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
        ) {
            items(searchResultData!!.attractions!!) { attraction ->
                Log.d("Catalog", "로드 완료$attraction")
                SearchResultItem(attraction, navController)
            }
        }
    } else {
        Log.e("Catalog", "로드 실패")
        // 데이터가 null이면 로딩 표시 또는 비어 있는 상태 표시
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("검색 결과가 없습니다.")
        }
    }
}

@Composable
fun SearchResultItem(
    attraction: SearchResult,
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
                // 텍스트를 나란히 표시하기 위해 Row 사용
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // 관광지 이름
                    Text(
                        text = "${attraction.attraction_name}",
                        modifier = Modifier.padding(horizontal = 8.dp).weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    // 카테고리
                    Text(
                        text = "${attraction.category2}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp), // 이름과 간격 유지
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "위치", modifier = Modifier.padding(horizontal = 8.dp))
                    Text(
                        text = "${attraction.si}, ${attraction.gu} ${attraction.dong}",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "운영시간", modifier = Modifier.padding(horizontal = 8.dp))
                    Text(
                        text = "${attraction.operation_hours}",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "휴무일", modifier = Modifier.padding(horizontal = 8.dp))
                    Text(
                        text = "${attraction.closed_days}",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "입장료", modifier = Modifier.padding(horizontal = 8.dp))
                    Text(
                        text = if (attraction.is_entrance_fee == true) "유료" else "무료", // 입장료 상태에 따라 텍스트 변경
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSearchScreen() {
    // rememberNavController를 사용하여 Preview에서 NavController를 제공합니다.
    SearchScreen(navController = rememberNavController(), keyword = "키워드")
}
