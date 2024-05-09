package com.hungrybrothers.abletotrip.ui.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.hungrybrothers.abletotrip.ui.components.HeaderBar
import com.hungrybrothers.abletotrip.ui.datatype.Catalog2Attraction
import com.hungrybrothers.abletotrip.ui.network.ShowMoreInfoRepository
import com.hungrybrothers.abletotrip.ui.viewmodel.CurrentLocationViewModel
import com.hungrybrothers.abletotrip.ui.viewmodel.ShowMoreViewModel

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

    LaunchedEffect(latitude, longitude) {
        if (latitude != null && longitude != null) {
            showMoreViewModel.loadInitialData(latitude!!, longitude!!, category)
        }
    }

    Column {
        HeaderBar(navController, true)

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
                CircularProgressIndicator()
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

        item {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
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
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .clickable(onClick = { navController.navigate("detail/${attraction.id}") }),
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
