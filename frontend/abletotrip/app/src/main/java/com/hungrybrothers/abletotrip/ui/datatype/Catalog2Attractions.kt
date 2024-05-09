package com.hungrybrothers.abletotrip.ui.datatype

import kotlinx.serialization.Serializable

@Serializable
data class Catalog2Attraction(
    val id: Int,
    val attraction_name: String,
    val operation_hours: String,
    val closed_days: String,
    val is_entrance_fee: Boolean,
    val si: String,
    val gu: String,
    val dong: String,
    val image_url: String,
    val distance: Float,
)

@Serializable
data class Catalog2Attractions(
    val attractions: List<Catalog2Attraction>,
)
