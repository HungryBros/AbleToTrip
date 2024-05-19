package com.hungrybrothers.abletotrip.ui.network

import com.hungrybrothers.abletotrip.ui.datatype.AttractionDetail
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess

class AttractionDetailRepository {
    suspend fun fetchAttractionDetailData(itemId: Int?): AttractionDetail? {
        if (itemId == null) {
            return null
        }

        val url = "attraction/detail/$itemId"

        return try {
            val response = KtorClient.client.get(url)
            if (response.status.isSuccess()) {
                response.body<AttractionDetail>()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
