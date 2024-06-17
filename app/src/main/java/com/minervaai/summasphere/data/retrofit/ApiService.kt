package com.minervaai.summasphere.data.retrofit

import com.minervaai.summasphere.data.request.LoginRequest
import com.minervaai.summasphere.data.request.SignupRequest
import com.minervaai.summasphere.data.response.LoginResponse
import com.minervaai.summasphere.data.response.SignupResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("auth/email/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("auth/email/register")
    suspend fun signup(
        @Body request: SignupRequest
    ): Response<SignupResponse>

    @POST("auth/logout")
    fun logout(
        @Header("Authorization") token: String
    ): Response<Unit>

}