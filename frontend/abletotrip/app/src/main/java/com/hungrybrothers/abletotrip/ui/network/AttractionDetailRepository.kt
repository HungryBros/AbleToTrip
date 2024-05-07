package com.hungrybrothers.abletotrip.ui.network

import android.util.Log
import com.hungrybrothers.abletotrip.ui.datatype.AttractionDetail
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess

class AttractionDetailRepository {
    suspend fun fetchAttractionDetailData(itemId: Int?): AttractionDetail? {
        if (itemId == null) {
            Log.e("DetailData", "Item ID is null")
            return null
        }

        val url = "attraction/detail/$itemId"

        return try {
            Log.d("DetailData", "Sending request...")
            val response = KtorClient.client.get(url)
            Log.d("DetailData", "Response success: ${response.status}")
            if (response.status.isSuccess()) {
                response.body<AttractionDetail>()
            } else {
                Log.e("DetailData", "Communication failure: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Log.e("DetailData", "Error occurred", e)
            null
        }
    }
}
