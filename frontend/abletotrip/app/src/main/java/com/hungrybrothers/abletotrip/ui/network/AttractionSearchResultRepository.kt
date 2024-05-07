package com.hungrybrothers.abletotrip.ui.network

import android.util.Log
import com.hungrybrothers.abletotrip.ui.datatype.AttractionSearchResult
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess

class AttractionSearchResultRepository {
    suspend fun fetchAttractionSearchResultData(
        keyword: String?,
        page: Int,
    ): AttractionSearchResult? {
        if (keyword == null) {
            Log.e("SearchResultData", "Item ID is null")
            return null
        }

        val url = "attraction/search/?keyword=$keyword&page=$page"

        return try {
            Log.d("SearchResultData", "Sending request...")
            val response = KtorClient.client.get(url)
            Log.d("SearchResultData", "Response success: ${response.status}")
            if (response.status.isSuccess()) {
                response.body<AttractionSearchResult>()
            } else {
                Log.e("SearchResultData", "Communication failure: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Log.e("SearchResultData", "Error occurred", e)
            null
        }
    }
}
