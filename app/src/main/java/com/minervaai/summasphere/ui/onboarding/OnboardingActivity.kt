package com.minervaai.summasphere.ui.onboarding

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.minervaai.summasphere.R
import com.minervaai.summasphere.databinding.ActivityOnboardingBinding
import com.minervaai.summasphere.helper.GoogleLoginHelper
import com.minervaai.summasphere.ui.login.LoginActivity
import com.minervaai.summasphere.ui.main.MainActivity
import com.minervaai.summasphere.ui.signup.SignupActivity
import com.minervaai.summasphere.ui.summarizer.SummarizerActivity
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class OnboardingActivity : AppCompatActivity() {
    private lateinit var mViewPager: ViewPager2
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var googleLoginHelper: GoogleLoginHelper

//    private lateinit var auth : FirebaseAuth
//    private lateinit var googleSignInClient : GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        googleLoginHelper = GoogleLoginHelper(this)


//        auth = FirebaseAuth.getInstance()
//
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(getString(R.string.default_web_client_id))
//            .requestEmail()
//            .build()

        // Periksa apakah pengguna sudah login menggunakan SharedPreferences
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("IsLoggedIn", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

//        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        binding.btnGoogle.setOnClickListener {
            googleLoginHelper.googleSignIn(this) { intent ->
                launcher.launch(intent)
            }
        }

        binding.btnLogin.setOnClickListener {
            val intent = Intent (this, LoginActivity::class.java)
            startActivity(intent)
        }

        mViewPager = findViewById(R.id.onboarding_viewpager)
        mViewPager.adapter = OnboardingAdapter(this, this)
        TabLayoutMediator(binding.pageIndicator, mViewPager) { _, _ -> }.attach()
        mViewPager.offscreenPageLimit = 1
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        } else {
            Toast.makeText(this, "Login failed, please try again", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        lifecycleScope.launch {
            googleLoginHelper.manageResults(task, this@OnboardingActivity)
        }
    }

//    private fun googleSignIn() {
//        val signInIntent = googleSignInClient.signInIntent
//        launcher.launch(signInIntent)
//    }
//
//    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
//            manageResults(task)
//        } else {
//            Log.e("GoogleSignIn", "Google sign in failed 1")
//            Toast.makeText(this, "Login failed, please try again", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun manageResults(task: Task<GoogleSignInAccount>) {
//        try {
//            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
//            if (account != null) {
//                val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
//                val cred = GoogleAuthProvider.getCredential(account.idToken, null)
//                with(sharedPreferences.edit()) {
//                    putBoolean("IsLoggedIn", true)
//                    apply()
//                }
//                auth.signInWithCredential(cred).addOnCompleteListener { signInTask ->
//                    if (signInTask.isSuccessful) {
//                        Log.d("GoogleSignIn", "signInWithCredential:success")
//                        Intent(this, SummarizerActivity::class.java).also {
//                            startActivity(it)
//                        }
//                        Toast.makeText(this, "Successfully login", Toast.LENGTH_SHORT).show()
//                    } else {
//                        Log.e("GoogleSignIn", "signInWithCredential:failure", signInTask.exception)
//                        Toast.makeText(this, "Login failed, please try again", Toast.LENGTH_SHORT).show()
//                    }
//                }
//                Log.d("GoogleSignIn", "firebaseAuthWithGoogle:" + account.id)
//            } else {
//                Log.e("GoogleSignIn", "Google sign in failed 2", task.exception)
//                Toast.makeText(this, "Login failed, please try again", Toast.LENGTH_SHORT).show()
//            }
//        } catch (e: ApiException) {
//            Log.e("GoogleSignIn", "Google sign in failed 3", e)
//            Toast.makeText(this, "Login failed, please try again", Toast.LENGTH_SHORT).show()
//        }
//    }
}