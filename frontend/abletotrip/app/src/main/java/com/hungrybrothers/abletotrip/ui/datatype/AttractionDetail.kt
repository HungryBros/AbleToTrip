package com.hungrybrothers.abletotrip.ui.datatype

import kotlinx.serialization.Serializable

@Serializable
data class AttractionDetail(
    val id: Int,
    val image_url: String?,
    val attraction_name: String,
    val attraction_sub_name: String?,
    val category1: String,
    val category2: String,
    val si: String,
    val gu: String,
    val dong: String,
    val street_number: String,
    val road_name: String?,
    val latitude: Double,
    val longitude: Double,
    val postal_code: String,
    val road_name_address: String?,
    val lot_number_address: String,
    val contact_number: String?,
    val homepage_url: String,
    val closed_days: String,
    val operation_hours: String,
    val is_free_parking: Boolean,
    val is_paid_parking: Boolean,
    val is_entrance_fee: Boolean,
    val is_disabled_restroom: Boolean,
    val is_disabled_parking: Boolean,
    val is_large_parking: Boolean,
    val is_audio_guide: Boolean,
//    val attractionImage: String,
)
