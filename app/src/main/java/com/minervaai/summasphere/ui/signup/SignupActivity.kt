package com.minervaai.summasphere.ui.signup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.minervaai.summasphere.R
import com.minervaai.summasphere.databinding.ActivitySignupBinding
import com.minervaai.summasphere.data.request.SignupRequest
import com.minervaai.summasphere.data.response.SignupResponse
import com.minervaai.summasphere.data.retrofit.ApiConfig
import com.minervaai.summasphere.ui.login.LoginActivity
import com.minervaai.summasphere.ui.summarize.SummaryActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.inputConfirmPasswordSignup.linkPasswordEditText(binding.inputPasswordSignup)

        setupAction()
    }

    private fun setupAction() {
        binding.btnSignup.setOnClickListener {
            val email = binding.inputEmailSignup.text.toString()
            val password = binding.inputPasswordSignup.text.toString()
            val confirmPassword = binding.inputConfirmPasswordSignup.text.toString()

            if (!isInputValid(email, password, confirmPassword)) {
                showToast(R.string.fill_all_fields.toString())
                return@setOnClickListener
            }

            signup(email, password, confirmPassword)
        }
    }

    private fun signup(email: String, password: String, confirmPassword: String) {
        val signupRequest = SignupRequest(email, password, confirmPassword)
        ApiConfig.instance.signup(signupRequest).enqueue(object : Callback<SignupResponse> {
            override fun onResponse(call: Call<SignupResponse>, response: Response<SignupResponse>) {
                if (response.code() == 500) {
                    val signupResponse = response.body()
                    Log.d("SignupActivity", "signin successful: $signupResponse")
                    navigateToHome()
                }
                if (response.code() == 400) {
                    val signupResponse = response.body()
                    showAlert("Signup failed: Email already exists")
                    Log.e("SignupActivity", "Signup failed: $signupResponse")
                } else {
                    showAlert("Signup failed: ${response.message()}")
                    Log.e("SignupActivity", "Signup failed: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<SignupResponse>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun showAlert(message: String) {
        AlertDialog.Builder(this).apply {
            setTitle("Signup Error")
            setMessage(message)
            setPositiveButton("OK", null)
            show()
        }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, SummaryActivity::class.java))
        finish()
    }

    private fun isInputValid(email: String, password: String, confirmPassword: String): Boolean {
        val isEmailValid = email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val isPasswordValid = password.isNotEmpty() && password.length in 8..10
        val isConfirmPasswordValid = confirmPassword == password

        return isEmailValid && isPasswordValid && isConfirmPasswordValid
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}