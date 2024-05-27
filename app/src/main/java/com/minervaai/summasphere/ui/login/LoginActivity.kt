package com.minervaai.summasphere.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.minervaai.summasphere.R
import com.minervaai.summasphere.databinding.ActivityLoginBinding
import com.minervaai.summasphere.ui.signup.SignupActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        setupAction()
    }

    private fun setupAction() {

        binding.btnLogin.setOnClickListener {
            val email = binding.inputEmailLogin.text.toString()
            val password = binding.inputPasswordLogin.text.toString()

            if (!isInputValid(email, password)) {
                showToast(R.string.input_error.toString())
                return@setOnClickListener
            }

            // observe not yet applied
        }

        binding.tvSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
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