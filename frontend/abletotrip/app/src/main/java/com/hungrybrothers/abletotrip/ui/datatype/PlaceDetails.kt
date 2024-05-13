package com.hungrybrothers.abletotrip.ui.datatype

import kotlinx.serialization.Serializable

@kotlinx.serialization.Serializable
data class PlaceDetailsResponse(
    val result: PlaceDetailsResult,
)

@kotlinx.serialization.Serializable
data class PlaceDetailsResult(
    val geometry: PlaceGeometry,
)

@kotlinx.serialization.Serializable
data class PlaceGeometry(
    val location: PlaceLocation,
)

@Serializable
data class PlaceLocation(
    val lat: Double,
    val lng: Double,
)
