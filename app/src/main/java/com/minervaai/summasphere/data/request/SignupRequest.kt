package com.minervaai.summasphere.data.request

data class SignupRequest(
    val email: String,
    val password: String,
    val passwordConfirm: String
)
