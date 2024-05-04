package com.hungrybrothers.abletotrip.ui.network

import android.util.Log
import com.hungrybrothers.abletotrip.ui.datatype.Catalog2Attractions
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.isSuccess

class Catalog2Repository {
    suspend fun fetchCatalog2Data(
        latitude: String,
        longitude: String,
        category: String,
        page: Int,
    ): Catalog2Attractions? {
        return try {
            Log.d("Catalog2Data", "요청 보내는 중...category: $category, page: $page,")
            val response =
                KtorClient.client.get("attraction/by_category/?category2=$category&page=$page") {
                    header("latitude", latitude)
                    header("longitude", longitude)
                }
            Log.d("Catalog2Data", "응답 성공: ${response.status}")
            if (response.status.isSuccess()) {
                val body = response.body<Catalog2Attractions>()
                Log.d("Catalog2Data", "받은 데이터: $body")
                body
            } else {
                Log.e("Catalog2Data", "통신 실패: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Log.e("Catalog2Data", "에러 발생", e)
            null
        }
    }
}
