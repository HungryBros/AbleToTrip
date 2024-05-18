package com.hungrybrothers.abletotrip.ui.network

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
            val response =
                KtorClient.client.get("attraction/") {
                    header("latitude", latitude)
                    header("longitude", longitude)
                }
            if (response.status.isSuccess()) {
                response.body<Attractions>()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
