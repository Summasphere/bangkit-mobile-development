package com.minervaai.summasphere.ui.login

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minervaai.summasphere.data.request.LoginRequest
import com.minervaai.summasphere.data.response.LoginResponse
import com.minervaai.summasphere.data.retrofit.ApiConfig
import com.minervaai.summasphere.helper.ResultState
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class LoginViewModel(private val sharedPreferences: SharedPreferences) : ViewModel() {

    fun login(email: String, password: String): LiveData<ResultState<LoginResponse>> {
        val loginState = MutableLiveData<ResultState<LoginResponse>>()
        loginState.value = ResultState.Loading

        viewModelScope.launch {
            try {
                val response = ApiConfig
                    .getApiService()
                    .login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    response.body()?.let {
                        saveSession(email, password, it.token)
                        loginState.value = ResultState.Success(it)
                    } ?: run {
                        loginState.value = ResultState.Error("Login failed")
                    }
                } else {
                    loginState.value = ResultState.Error("Login failed: ${response.errorBody()?.string()}")
                }
            } catch (e: HttpException) {
                loginState.value = ResultState.Error("Network error: ${e.message()}")
            } catch (e: IOException) {
                loginState.value = ResultState.Error("IO error: ${e.message}")
            }
        }
        return loginState
    }

    private fun saveSession(email: String, password: String, token: String?) {
        token?.let {
            sharedPreferences.edit().apply {
                putString("email", email)
                putString("password", password)
                putString("token", token)
                apply()
            }
        }
    }

    fun userIsLoggedIn(): Boolean {
        val token = sharedPreferences.getString("token", null)
        return !token.isNullOrEmpty()
    }
}
