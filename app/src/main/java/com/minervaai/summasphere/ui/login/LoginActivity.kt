package com.minervaai.summasphere.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.minervaai.summasphere.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val inputEmail = binding.inputEmailLogin
        val inputEmailLayout = binding.inputEmailLoginLayout

        inputEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validateEmail(inputEmail, inputEmailLayout)
            }

        })

        val inputPassword = binding.inputPasswordLogin
        val inputPasswordLayout = binding.inputPasswordLoginLayout

        inputPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validatePassword(inputPassword, inputPasswordLayout)
            }

        })

        binding.btnLoginLoginscreen.setOnClickListener {
            if (validateEmail(inputEmail, inputEmailLayout)
                && validatePassword(inputPassword, inputPasswordLayout))
                {
                    Toast.makeText(this, "Successfully login", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateEmail (inputEmail: TextInputEditText, inputEmailLayout: TextInputLayout): Boolean {
        val emailPattern = Regex("[a-zA-Z\\d._-]+@[a-z]+\\.+[a-z]+")
        return when {
            inputEmail.text.toString().trim().isEmpty() -> {
                inputEmailLayout.error = "Required"
                false
            }
            ! inputEmail.text.toString().trim().matches(emailPattern) -> {
                inputEmailLayout.error = "Please input valid email"
                false
            }
            else -> {
                inputEmailLayout.error = null
                true
            }
        }
    }

    private fun validatePassword (inputPassword: TextInputEditText, inputPasswordLayout: TextInputLayout) : Boolean {
        return when {
            inputPassword.text.toString().trim().isEmpty() -> {
                inputPasswordLayout.error = "Required"
                false
            }
            inputPassword.text.toString().trim().length < 8 || inputPassword.text.toString().trim().length > 10 -> {
                inputPasswordLayout.error = "Password must be 8 to 10 characters"
                false
            }
            else -> {
                inputPassword.error = null
                true
            }

        }
    }
