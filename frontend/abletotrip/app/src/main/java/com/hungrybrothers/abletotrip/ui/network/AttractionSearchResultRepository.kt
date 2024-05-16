package com.hungrybrothers.abletotrip.ui.network

import android.util.Log
import com.hungrybrothers.abletotrip.ui.datatype.AttractionSearchResult
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.encodeURLParameter
import io.ktor.http.isSuccess

class AttractionSearchResultRepository {
    suspend fun fetchAttractionSearchResultData(
        latitude: String,
        longitude: String,
        keyword: String?,
        page: Int,
    ): AttractionSearchResult? {
        if (keyword == null) {
            Log.e("SearchResultData", "Item ID is null")
            return null
        }
        Log.d("AttractionSearchResultRepository", "longitude = $longitude latitude = $latitude")
        val url = "attraction/search/?keyword=${keyword.encodeURLParameter()}&page=$page"

        return try {
            Log.d("SearchResultData", "Sending request...")
            val response =
                KtorClient.client.get(url) {
                    header("latitude", latitude)
                    header("longitude", longitude)
                }
            Log.d("SearchResultData", "Response success: ${response.status}")
            Log.d("SearchResultData", "SearchResultData: ${response.bodyAsText()}")
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
