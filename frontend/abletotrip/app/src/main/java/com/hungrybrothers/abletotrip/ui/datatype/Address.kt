package com.hungrybrothers.abletotrip.ui.datatype

import kotlinx.serialization.Serializable

@Serializable
data class AddressBody(
    val address: String,
    val latitude: Double,
    val longitude: Double,
)
