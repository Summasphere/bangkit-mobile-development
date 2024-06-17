@file:Suppress("DEPRECATION")

package com.minervaai.summasphere.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.minervaai.summasphere.R
import com.minervaai.summasphere.ui.summarizer.SummarizerActivity
import kotlinx.coroutines.tasks.await

class GoogleLoginHelper(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun getGoogleSignInClient(): GoogleSignInClient {
        return googleSignInClient
    }

    fun googleSignIn(activity: Activity, launcher: (Intent) -> Unit) {
        val signInIntent = googleSignInClient.signInIntent
        launcher(signInIntent)
    }

    suspend fun manageResults(task: Task<GoogleSignInAccount>, context: Context) {
        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            if (account != null) {
                val sharedPreferences = getSharedPreferences()
                val cred = GoogleAuthProvider.getCredential(account.idToken, null)
                with(sharedPreferences.edit()) {
                    putBoolean("IsLoggedIn", true)
                    apply()
                }
                val signInTask = auth.signInWithCredential(cred).await()
                if (signInTask.user != null) {
                    Intent(context, SummarizerActivity::class.java).also {
                        context.startActivity(it)
                    }
                    Toast.makeText(context, "Successfully login", Toast.LENGTH_SHORT).show()
                } else {
                    throw Exception("Sign in task failed")
                }
            } else {
                throw ApiException(Status.RESULT_INTERNAL_ERROR)
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Login failed, please try again", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Login failed, please try again", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getSharedPreferences(): SharedPreferences {
        return context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    }

    suspend fun logOut(): Boolean {
        return LogoutUtils.googleLogout(googleSignInClient)
    }
}
