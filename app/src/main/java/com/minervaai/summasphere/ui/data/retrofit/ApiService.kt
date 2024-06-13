package com.minervaai.summasphere.ui.data.retrofit

import com.minervaai.summasphere.ui.data.request.LoginRequest
import com.minervaai.summasphere.ui.data.request.SignupRequest
import com.minervaai.summasphere.ui.data.response.LoginResponse
import com.minervaai.summasphere.ui.data.response.SignupResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/email/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("auth/email/register")
    fun signup(@Body request: SignupRequest): Call<SignupResponse>
}