@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.hungrybrothers.abletotrip.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.model.Place

@Composable
fun PlacesList(
    places: List<Place>,
    onPlaceClicked: (Place) -> Unit,
) {
    Log.d("Places : PlaceList", "플레이스리스트: $places")
    LazyColumn(modifier = Modifier.padding(start = 8.dp, end = 8.dp)) {
        items(places) { place ->
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { onPlaceClicked(place) }
                        .background(MaterialTheme.colorScheme.surfaceVariant), // 항목의 배경색 설정
            ) {
                Text(
                    text = place.name ?: "Unknown name", // null 처리
                    style = MaterialTheme.typography.titleMedium, // Material You 디자인에 맞춘 타이포그래피
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier =
                        Modifier.padding(
                            top = 16.dp,
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 0.dp, // 바텀 패딩은 0으로 설정
                        ),
                )
                Text(
                    text = place.address ?: "No address provided", // null 처리
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier =
                        Modifier.padding(
                            top = 0.dp,
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 0.dp,
                        ),
                )
                Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}
