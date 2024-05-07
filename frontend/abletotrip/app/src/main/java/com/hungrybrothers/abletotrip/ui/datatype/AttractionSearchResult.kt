package com.hungrybrothers.abletotrip.ui.datatype

import kotlinx.serialization.Serializable

@Serializable
data class AttractionSearchResult(
    val counts: Int?,
    val attractions: List<SearchResult>?,
)

@Serializable
data class SearchResult(
    val id: Int?,
    val attraction_name: String?,
    val category2: String?,
    val longitude: Double?,
    val latitude: Double?,
    val operation_hours: String?,
    val closed_days: String?,
    val is_entrance_fee: Boolean?,
    val si: String?,
    val gu: String?,
    val dong: String?,
    val image_url: String?,
    val distance: Int?,
)
