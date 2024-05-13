package com.hungrybrothers.abletotrip.ui.datatype

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val latitude: Double,
    val longitude: Double,
    val address: String,
)

@Serializable
data class UserResponse(
    val users: List<User>,
)
