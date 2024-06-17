@file:Suppress("DEPRECATION")

package com.minervaai.summasphere.helper


import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.minervaai.summasphere.data.retrofit.ApiConfig
import kotlinx.coroutines.tasks.await

object LogoutUtils {

    suspend fun googleLogout(googleSignInClient: GoogleSignInClient): Boolean {
        return try {
            googleSignInClient.signOut().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun emailPasswordLogout(token: String): Boolean {
        return try {
            val response = ApiConfig
                .getApiService()
                .logout("Bearer $token")
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
