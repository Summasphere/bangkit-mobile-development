package com.minervaai.summasphere.ui.signup

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.minervaai.summasphere.R
import com.minervaai.summasphere.databinding.ActivitySignupBinding
import com.minervaai.summasphere.ui.login.LoginActivity

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

        setupAction()
    }

    private fun setupAction() {

        binding.btnSignup.setOnClickListener {
            val email = binding.inputEmailSignup.text.toString()
            val password = binding.inputPasswordSignup.text.toString()
            val confirmPassword = binding.inputConfirmpasswordSignup.text.toString()

            if (!isInputValid(email, password, confirmPassword)) {
                showToast(R.string.fill_all_fields.toString())
                return@setOnClickListener
            }

            // observe not yet applied
        }
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