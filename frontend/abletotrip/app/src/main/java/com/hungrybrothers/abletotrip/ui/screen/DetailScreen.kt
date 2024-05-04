package com.hungrybrothers.abletotrip.ui.screen
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.hungrybrothers.abletotrip.ui.datatype.AttractionDetail
import com.hungrybrothers.abletotrip.ui.network.AttractionDetailRepository
import com.hungrybrothers.abletotrip.ui.viewmodel.AttractionDetailViewModel

@Composable
fun DetailScreen(
    navController: NavController,
    itemId: Int?,
//    attractionDetailViewModel: AttractionDetailViewModel = viewModel(),
) {
    val attractionDetailViewModel: AttractionDetailViewModel =
        remember {
            val repository = AttractionDetailRepository()
            AttractionDetailViewModel(repository)
        }

    LaunchedEffect(key1 = itemId) {
        if (itemId != null) {
            attractionDetailViewModel.fetchAttractionDetailData(itemId)
        } else {
            Log.e("DetailScreen", "Invalid item ID: $itemId")
        }
    }

    val attractionDetail by attractionDetailViewModel.attractionDetailData.collectAsState()
//    Column {
//        Text(text = "$attractionDetail")
//    }
//
    attractionDetail?.let { detail ->
        Column(
            modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = null), // 이미지 없음
                contentDescription = "Attraction Image",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.height(16.dp))
            Text(text = detail.attraction_name, style = MaterialTheme.typography.headlineMedium)
            if (detail.attraction_sub_name != null) {
                Text(text = detail.attraction_sub_name, style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = "${detail.si} ${detail.gu}, ${detail.dong}", style = MaterialTheme.typography.bodySmall)
            Text(
                text = "주소: ${detail.road_name_address ?: detail.lot_number_address}",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(text = "연락처: ${detail.contact_number ?: "정보 없음"}", style = MaterialTheme.typography.bodySmall)
            Text(text = "운영시간: ${detail.operation_hours}", style = MaterialTheme.typography.bodySmall)
            Text(text = "홈페이지: ${detail.homepage_url}", style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(24.dp))
            FacilitiesGrid(detail)
        }
    } ?: Text("Loading details...", style = MaterialTheme.typography.bodyLarge)
}

@Composable
fun FacilitiesGrid(attractionDetail: AttractionDetail) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text("시설 정보", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            FacilityIcon(attractionDetail.is_free_parking, "무료 주차")
            FacilityIcon(attractionDetail.is_paid_parking, "유료 주차")
            FacilityIcon(attractionDetail.is_entrance_fee, "입장료")
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            FacilityIcon(attractionDetail.is_disabled_restroom, "장애인 화장실")
            FacilityIcon(attractionDetail.is_disabled_parking, "장애인 주차")
            FacilityIcon(attractionDetail.is_large_parking, "대형 주차장")
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            FacilityIcon(attractionDetail.is_audio_guide, "오디오 가이드")
        }
    }
}

@Composable
fun FacilityIcon(
    isAvailable: Boolean,
    label: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = if (isAvailable) Icons.Filled.Check else Icons.Filled.Close,
            contentDescription = if (isAvailable) "$label 가능" else "$label 불가능",
            tint = if (isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(24.dp),
        )
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}
