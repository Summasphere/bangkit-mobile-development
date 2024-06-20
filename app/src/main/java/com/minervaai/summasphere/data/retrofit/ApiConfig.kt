package com.minervaai.summasphere.data.retrofit

import com.minervaai.summasphere.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//object ApiConfig {
//    private const val BASE_URL = "https://inilho.its.ac.id/summasphere/api/v1/"
//
//    val instance: ApiService by lazy {
//        val retrofit = Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        retrofit.create(ApiService::class.java)
//    }
//}

class ApiConfig {
    companion object {
        fun getApiService():ApiService {
            val client = OkHttpClient.Builder()
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)

                .build()
            return  retrofit.create(ApiService::class.java)
        }
    }
}