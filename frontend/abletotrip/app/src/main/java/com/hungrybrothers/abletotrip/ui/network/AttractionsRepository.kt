package com.hungrybrothers.abletotrip.ui.network

import android.util.Log
import com.hungrybrothers.abletotrip.ui.datatype.Attractions
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.isSuccess

class AttractionsRepository {
    suspend fun fetchPlaceData(
        latitude: String,
        longitude: String,
    ): Attractions? {
        return try {
            Log.d("PlaceData", "요청 보내는 중...")
            val response =
                KtorClient.client.get("attraction/") {
                    header("latitude", latitude)
                    header("longitude", longitude)
                }
            Log.d("PlaceData", "응답 성공: ${response.status}")
            if (response.status.isSuccess()) {
                response.body<Attractions>()
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

// suspend fun fetchPlaceData(
//    latitude: String,
//    longitude: String,
// ): Attractions? {
//    return withContext(Dispatchers.IO) {
//        try {
//            Log.d("PlaceData", "요청 보내는 중...")
//            val response =
//                KtorClient.client.get("attraction/") {
//                    header("latitude", latitude)
//                    header("longitude", longitude)
//                }
//            Log.d("PlaceData", "응답 성공: ${response.status}")
//            if (response.status.isSuccess()) {
//                // 성공적인 응답을 받았을 때, JSON을 AttractionsResponse 객체로 변환
//                val responseData: Attractions = response.body()
//                Log.d("PlaceData", "디코딩 성공: $responseData")
//                responseData
//            } else {
//                Log.e("PlaceData", "통신 실패: ${response.status}")
//                null
//            }
//        } catch (e: Exception) {
//            Log.e("PlaceData", "에러 발생", e)
//            null
//        }
//    }
// }
