package com.hungrybrothers.abletotrip.ui.network

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String,
)

@Serializable
data class UserResponse(
    val users: List<User>,
)
