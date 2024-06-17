package com.minervaai.summasphere.data.request

import com.google.gson.annotations.SerializedName

data class SignupRequest(

    val email: String,

    val password: String,

    val passwordConfirm: String
)
