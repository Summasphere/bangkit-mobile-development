package com.minervaai.summasphere.data.response

import com.google.gson.annotations.SerializedName

data class SignupResponse(

    val statusCode: Int,

    val code: Int,

    val status: String,

    val message: String
)
