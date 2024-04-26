package com.hungrybrothers.abletotrip.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.libraries.places.api.model.Place

@Composable
fun PlacesList(places: List<Place>) {
    LazyColumn {
        items(places) { place ->
            place.name?.let {
                Text(
                    it,
                    modifier =
                        Modifier.clickable {
                            // 여기서 장소 선택 로직 처리
                        },
                )
            }
        }
    }
}
