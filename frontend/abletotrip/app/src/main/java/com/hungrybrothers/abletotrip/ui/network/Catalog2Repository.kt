package com.hungrybrothers.abletotrip.ui.network

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
        if (category.isBlank()) {
            return null
        }

        return try {
            val response =
                KtorClient.client.get("attraction/by_category/?category2=$category&page=$page") {
                    header("latitude", latitude)
                    header("longitude", longitude)
                }
            if (response.status.isSuccess()) {
                val body = response.body<Catalog2Attractions>()
                body
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
