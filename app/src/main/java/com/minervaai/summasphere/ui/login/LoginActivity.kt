package com.minervaai.summasphere.ui.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.minervaai.summasphere.databinding.ActivityLoginBinding
import com.minervaai.summasphere.data.request.LoginRequest
import com.minervaai.summasphere.data.response.LoginResponse
import com.minervaai.summasphere.data.retrofit.ApiConfig
import com.minervaai.summasphere.ui.signup.SignupActivity
import com.minervaai.summasphere.ui.summarize.SummaryActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        if (userIsLoggedIn()) {
            navigateToHome()
        }

        setupAction()

        binding.tvSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupAction() {
        binding.btnLogin.setOnClickListener {
            val email = binding.inputEmailLogin.text.toString()
            val password = binding.inputPasswordLogin.text.toString()

            if (!isInputValid(email, password)) {
                showToast("Invalid email or password")
                return@setOnClickListener
            }

            login(email, password)
        }
    }

    private fun login(email: String, password: String) {
        val loginRequest = LoginRequest(email, password)
        ApiConfig.instance.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    showToast("Login successful: ${loginResponse?.token}")
                    Log.d("LoginActivity", "Login successful: $loginResponse")
                    saveSession(email, password, loginResponse?.token)
                    navigateToHome()
                } else {
                    showToast("Login failed: ${response.errorBody()?.string()}")
                    Log.e("LoginActivity", "Login failed: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun saveSession(email: String, password: String, token: String?) {
        token?.let {
            val editor = sharedPreferences.edit()
            editor.putString("email", email)
            editor.putString("password", password)
            editor.putString("token", token)
            editor.apply()
        }
    }

    private fun userIsLoggedIn(): Boolean {
        val token = sharedPreferences.getString("token", null)
        return !token.isNullOrEmpty()
    }

    private fun navigateToHome() {
        startActivity(Intent(this, SummaryActivity::class.java))
        finish()
    }

    private fun isInputValid(email: String, password: String): Boolean {
        val isEmailValid = email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val isPasswordValid = password.isNotEmpty() && password.length in 8..10
        return isEmailValid && isPasswordValid
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
