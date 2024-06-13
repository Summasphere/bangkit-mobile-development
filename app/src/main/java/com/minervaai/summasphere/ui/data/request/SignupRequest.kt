package com.minervaai.summasphere.ui.data.request

data class SignupRequest(
    val email: String,
    val password: String,
    val passwordConfirm: String
)
