package com.hungrybrothers.abletotrip.ui.datatype

import kotlinx.serialization.Serializable

@Serializable
data class Attraction(
    val id: Int,
    val attraction_name: String,
    val si: String,
    val gu: String,
    val distance: Int,
    val image_url: String,
)

@Serializable
data class Attractions(
    val attractions: Map<String, List<Attraction>>,
)
