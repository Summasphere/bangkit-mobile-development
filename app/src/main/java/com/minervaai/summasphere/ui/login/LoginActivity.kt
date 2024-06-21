package com.minervaai.summasphere.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.minervaai.summasphere.databinding.ActivityLoginBinding
import com.minervaai.summasphere.helper.ResultState
import com.minervaai.summasphere.helper.ViewModelFactory
import com.minervaai.summasphere.ui.main.MainActivity
import com.minervaai.summasphere.ui.signup.SignupActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val sharedPreferences by lazy { getSharedPreferences("AppPrefs", Context.MODE_PRIVATE) }
    private val loginViewModel by viewModels<LoginViewModel> {
        ViewModelFactory(sharedPreferences)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (loginViewModel.userIsLoggedIn()) {
            navigateToHome()
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.inputEmailLogin.text.toString()
            val password = binding.inputPasswordLogin.text.toString()

            if (!isInputValid(email, password)) {
                showToast("Invalid email or password")
                return@setOnClickListener
            }

            loginViewModel.login(email, password).observe(this) { result ->
                when (result) {
                    is ResultState.Loading -> {
                        showLoading(true)
                    }

                    is ResultState.Success -> {
                        showLoading(false)
                        showToast("Succesfully login")
                        Log.d("LoginActivity", "Login successful: ${result.data}")
                        navigateToHome()
                    }

                    is ResultState.Error -> {
                        showLoading(false)
                        showToast("Failed to login, please try again")
                        Log.e("LoginActivity", "Login failed: ${result.error}")
                    }
                }
            }
        }

        binding.tvSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

    }

    private fun navigateToHome() {
        startActivity(Intent(this, MainActivity::class.java))
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

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
