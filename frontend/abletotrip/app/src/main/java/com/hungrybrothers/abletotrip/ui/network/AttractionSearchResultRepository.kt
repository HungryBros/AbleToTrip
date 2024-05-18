package com.hungrybrothers.abletotrip.ui.network

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
            return null
        }
        val url = "attraction/search/?keyword=${keyword.encodeURLParameter()}&page=$page"

        return try {
            val response =
                KtorClient.client.get(url) {
                    header("latitude", latitude)
                    header("longitude", longitude)
                }
            if (response.status.isSuccess()) {
                response.body<AttractionSearchResult>()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
