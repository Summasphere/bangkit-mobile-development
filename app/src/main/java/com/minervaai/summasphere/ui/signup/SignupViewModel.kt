package com.minervaai.summasphere.ui.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minervaai.summasphere.data.request.SignupRequest
import com.minervaai.summasphere.data.response.SignupResponse
import com.minervaai.summasphere.data.retrofit.ApiConfig
import com.minervaai.summasphere.helper.ResultState
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class SignupViewModel : ViewModel() {

    fun signup(email: String, password: String, confirmPassword: String): LiveData<ResultState<SignupResponse>> {
        val signupState = MutableLiveData<ResultState<SignupResponse>>()
        signupState.value = ResultState.Loading

        viewModelScope.launch {
            try {
                val response = ApiConfig
                    .getApiService()
                    .signup(SignupRequest(email, password, confirmPassword))
                if (response.isSuccessful) {
                    response.body()?.let {
                        signupState.value = ResultState.Success(it)
                    } ?: run {
                        signupState.value = ResultState.Error("Sign up failed")
                    }
                } else {
                    when (response.code()) {
                        400 -> {
                            signupState.value = ResultState.Error("Email already exists")
                        }
                        500 -> {
                            signupState.value = ResultState.Error("Server error, please try again later")
                        }
                        else -> {
                            signupState.value = ResultState.Error("Signup failed: ${response.errorBody()?.string()}")
                        }
                    }
                }
            } catch (e: HttpException) {
                signupState.value = ResultState.Error("Network error: ${e.message()}")
            } catch (e: IOException) {
                signupState.value = ResultState.Error("IO error: ${e.message}")
            }
        }
        return signupState
    }
}