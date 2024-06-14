package com.minervaai.summasphere.data.response

data class LoginResponse(
    val token: String,
    val refreshToken: String?,
    val tokenExpires: Long?,
    val user: User?
)

data class User(
    val id: Int,
    val email: String,
    val status: UserStatus
)

data class UserStatus(
    val id: Int,
    val name: String
)