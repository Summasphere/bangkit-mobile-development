package com.minervaai.summasphere.ui.signup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.minervaai.summasphere.R
import com.minervaai.summasphere.databinding.ActivitySignupBinding
import com.minervaai.summasphere.helper.ResultState
import com.minervaai.summasphere.ui.login.LoginActivity


class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private val signupViewModel: SignupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.inputConfirmPasswordSignup.linkPasswordEditText(binding.inputPasswordSignup)

        binding.btnSignup.setOnClickListener {
            val email = binding.inputEmailSignup.text.toString()
            val password = binding.inputPasswordSignup.text.toString()
            val confirmPassword = binding.inputConfirmPasswordSignup.text.toString()

            if (!isInputValid(email, password, confirmPassword)) {
                showToast(R.string.fill_all_fields.toString())
                return@setOnClickListener
            }

            signupViewModel.signup(email, password, confirmPassword). observe(this) { result ->
                when (result) {
                    is ResultState.Loading -> {
                        showLoading(true)
                    }

                    is ResultState.Success -> {
                        showLoading(false)
                        showToast("Sign up successful")
                        Log.d("SignupActivity", "Sign up successful: ${result.data}")
                        navigateToLogin()
                    }

                    is ResultState.Error -> {
                        showLoading(false)
                        when (result.error) {
                            "Email already exists" -> {
                                showToast("Sign up failed: Email already exists")
                                Log.e("SignupActivity", "Sign up failed: Email already exists")
                            }
                            "Server error, please try again later" -> {
                                showToast("Sign up failed: Server error, please try again later")
                                Log.e("SignupActivity", "Server error: Please try again later")
                            }
                            else -> {
                                showToast("Sign up successful")
                                Log.e("SignupActivity", "Sign up failed: ${result.error}")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
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

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}