package com.hungrybrothers.abletotrip.ui.datatype

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val latitude: Double,
    val longtitude: Double,
    val address: String,
)

@Serializable
data class UserResponse(
    val users: List<User>,
)
