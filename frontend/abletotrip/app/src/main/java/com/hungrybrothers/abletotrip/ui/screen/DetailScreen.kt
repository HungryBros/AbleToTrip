package com.hungrybrothers.abletotrip.ui.screen

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.hungrybrothers.abletotrip.R
import com.hungrybrothers.abletotrip.ui.components.HeaderBar
import com.hungrybrothers.abletotrip.ui.datatype.AttractionDetail
import com.hungrybrothers.abletotrip.ui.network.AttractionDetailRepository
import com.hungrybrothers.abletotrip.ui.theme.CustomPrimary
import com.hungrybrothers.abletotrip.ui.viewmodel.AttractionDetailViewModel

@Composable
fun DetailScreen(
    navController: NavController,
    itemId: Int?,
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HeaderBar(navController = navController, showBackButton = true)
        }

        if (attractionDetail == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator() // 데이터 로딩 중 표시
            }
        } else {
            Column(
                modifier =
                    Modifier
                        .padding(top = 108.dp, bottom = 72.dp)
                        .padding(horizontal = 16.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
            ) {
                attractionDetail?.let { detail ->
                    Image(
                        painter = rememberAsyncImagePainter(model = detail.image_url),
                        contentDescription = "Attraction Image",
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier, verticalAlignment = Alignment.Bottom) {
                        detail.attraction_name?.let {
                            Text(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                text = it,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        detail.category2?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                            )
                        }
                    }
                    detail.road_name_address?.let { InfoRow(R.drawable.locationon, it) }
                    InfoRow(
                        R.drawable.clock,
                        if (detail.operation_hours?.isNotEmpty() == true) "OPEN . ${detail.operation_hours}" else "OPEN . 정보 없음",
                    )
                    InfoRow(
                        R.drawable.calendarmonth,
                        if (detail.closed_days?.isNotEmpty() == true) "휴무일 . ${detail.closed_days}" else "휴무일 . 정보 없음",
                    )
                    InfoLinkRow(R.drawable.call, detail.contact_number, "tel:")
                    InfoLinkRow(R.drawable.earth, detail.homepage_url, "http://")
                    InfoRow(R.drawable.ticket2, if (detail.is_entrance_fee) "입장료 . 무료" else "입장료 . 유료")
                    Spacer(Modifier.height(24.dp))
                    FacilitiesGrid(detail)
                } // 제거된 CircularProgressIndicator 위치
            }
        }

        attractionDetail?.lot_number_address?.let { lotNumberAddress ->
            RouteButton(
                navController = navController,
                latitude = attractionDetail!!.latitude,
                longitude = attractionDetail!!.longitude,
                lotNumberAddress = lotNumberAddress,
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
            )
        }
    }
}

@Composable
fun RouteButton(
    navController: NavController,
    latitude: Double,
    longitude: Double,
    lotNumberAddress: String,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = {
            navController.navigate("DEPARTURE/${latitude.toFloat()}/${longitude.toFloat()}/$lotNumberAddress")
        },
        modifier =
            modifier
                .fillMaxWidth()
                .height(48.dp),
        colors = ButtonDefaults.buttonColors(CustomPrimary),
    ) {
        Text(
            "길찾기",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun InfoRow(
    iconId: Int,
    text: String,
) {
    Row(
        modifier = Modifier.padding(top = 16.dp),
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = "icon",
            modifier =
                Modifier
                    .padding(horizontal = 8.dp)
                    .size(16.dp)
                    .align(Alignment.CenterVertically),
            tint = Color.Unspecified,
        )
        Text(text = text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun InfoLinkRow(
    iconId: Int,
    text: String?,
    schemePrefix: String = "",
) {
    val context = LocalContext.current
    val annotatedLink =
        buildAnnotatedString {
            if (!text.isNullOrEmpty()) {
                var link = "$schemePrefix$text"

                // Ensure link starts with http:// or https://
                if (!link.startsWith("http://") && !link.startsWith("https://") && schemePrefix == "http://") {
                    link = "http://$text"
                }

                append(text)
                addStringAnnotation("URL", link, 0, text.length)
            } else {
                append("정보 없음")
            }
        }

    Row(
        modifier = Modifier.padding(top = 16.dp),
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = "icon",
            modifier =
                Modifier
                    .padding(horizontal = 8.dp)
                    .size(16.dp)
                    .align(Alignment.CenterVertically),
            tint = Color.Unspecified,
        )
        ClickableText(
            text = annotatedLink,
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary),
            onClick = { offset ->
                annotatedLink.getStringAnnotations("URL", offset, offset).firstOrNull()?.let {
                    openUrl(context, it.item)
                }
            },
        )
    }
}

fun openUrl(
    context: android.content.Context,
    url: String,
) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    try {
        context.startActivity(intent)
    } catch (e: android.content.ActivityNotFoundException) {
        Log.e("DetailScreen", "No activity found to handle this intent: $url")
    }
}

data class FacilityInfo(val isAvailable: Boolean, val iconId: Int, val availableDescription: String)

@Composable
fun FacilitiesGrid(attractionDetail: AttractionDetail) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text("시설 정보", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        val facilities =
            listOf(
                FacilityInfo(attractionDetail.is_disabled_parking, R.drawable.p, "장애인 주차장"),
                FacilityInfo(attractionDetail.is_disabled_restroom, R.drawable.men, "장애인 화장실"),
                FacilityInfo(attractionDetail.is_free_parking, R.drawable.p, "무료 주차"),
                FacilityInfo(attractionDetail.is_audio_guide, R.drawable.audio, "오디오 가이드"),
                FacilityInfo(attractionDetail.is_large_parking, R.drawable.parking, "대형차 주차"),
            )
        val showFacilities = facilities.any { it.isAvailable }

        if (showFacilities) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(facilities) { facility ->
                    if (facility.isAvailable) {
                        FacilityItem(
                            iconId = facility.iconId,
                            availableDescription = facility.availableDescription,
                        )
                    }
                }
            }
        } else {
            Text(
                text = "장애인 편의시설이 없습니다.",
                modifier = Modifier.padding(8.dp),
            )
        }
    }
    Spacer(modifier = Modifier.size(80.dp))
}

@Composable
fun FacilityItem(
    iconId: Int,
    availableDescription: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(8.dp),
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = availableDescription,
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = availableDescription,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black,
        )
    }
}

@Composable
fun DrawXMark() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawXMark(this)
    }
}

private fun drawXMark(scope: DrawScope) {
    val size = scope.size.minDimension
    val strokeWidth = size / 8
    val path =
        Path().apply {
            moveTo(0f, 0f)
            lineTo(size, size)
            moveTo(size, 0f)
            lineTo(0f, size)
        }

    scope.drawPath(
        path = path,
        color = Color.Gray,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth),
    )
}
