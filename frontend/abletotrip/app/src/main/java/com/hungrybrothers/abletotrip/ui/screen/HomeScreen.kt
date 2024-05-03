package com.hungrybrothers.abletotrip.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.hungrybrothers.abletotrip.ui.datatype.Attractions
import com.hungrybrothers.abletotrip.ui.network.KtorClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun fetchPlaceData(
    latitude: String,
    longitude: String,
): Attractions? {
    return withContext(Dispatchers.IO) {
        try {
            Log.d("PlaceData", "요청 보내는 중...")
            val response =
                KtorClient.client.get("attraction/") {
                    header("latitude", latitude)
                    header("longitude", longitude)
                }
            Log.d("PlaceData", "응답 성공: ${response.status}")
            if (response.status.isSuccess()) {
                // 성공적인 응답을 받았을 때, JSON을 AttractionsResponse 객체로 변환
                val responseData: Attractions = response.body()
                Log.d("PlaceData", "디코딩 성공: $responseData")
                responseData
            } else {
                Log.e("PlaceData", "통신 실패: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Log.e("PlaceData", "에러 발생", e)
            null
        }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    val (placeData, setPlaceData) = remember { mutableStateOf<Attractions?>(null) }
//    위도(Latitude) : 37.497952 / 경도(Longitude) : 127.027619
    LaunchedEffect(Unit) {
        val fetchedData = fetchPlaceData("37.497952", "127.027619")
        setPlaceData(fetchedData) // 이렇게 상태를 업데이트합니다.
    }
    Log.d("PlaceData확인", "placeData: $placeData")

    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            placeData?.let { data ->
                Log.d("PlaceData1", "${data.attractions["nearby"]}")
                Log.d("PlaceData2", "${data.attractions["exhibition_performance"]}")
                Log.d("PlaceData3", "${data.attractions["leisure_park"]}")
                Log.d("PlaceData4", "${data.attractions["culture_famous"]}")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    // rememberNavController를 사용하여 Preview에서 NavController를 제공합니다.
    HomeScreen(navController = rememberNavController())
}
