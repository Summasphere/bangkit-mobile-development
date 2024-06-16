package com.minervaai.summasphere.ui.summarizer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.minervaai.summasphere.R
import com.minervaai.summasphere.databinding.ActivitySummaryBinding
import com.minervaai.summasphere.ui.onboarding.OnboardingActivity
import com.minervaai.summasphere.ui.resultsummarizer.ResultSummarizerActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class SummarizerActivity : AppCompatActivity() {
    // val text = "Rencana yang Anda sampaikan untuk merombak encoder dan decoder terdengar cukup baik. Mengombinasikan Transformer dan Conformer pada encoder dan decoder berpotensi meningkatkan performa model, terutama jika Anda dapat memanfaatkan kelebihan dari kedua arsitektur tersebut. Namun, perlu diingat bahwa performa model juga bergantung pada banyak faktor lain seperti kualitas data, teknik preprocessing, hyperparameter, dan sebagainya. Untuk encoder, struktur yang Anda usulkan dengan 4 Conv2D diikuti Transformer dan Conformer terdengar masuk akal. Kombinasi CNN dan arsitektur self-attention seperti Transformer/Conformer sering digunakan pada tugas speech recognition untuk mengekstrak fitur dari input spektrogram secara efektif. Untuk decoder, mengombinasikan Transformer dan Conformer juga merupakan pendekatan yang baik. Decoder Transformer dapat menangkap dependensi jangka panjang dalam urutan output, sementara Conformer dapat menambahkan kemampuan untuk menangkap pola lokal dan meningkatkan modeling konvolutif. Namun, perlu diperhatikan bahwa menggabungkan Transformer dan Conformer pada decoder mungkin memerlukan sedikit modifikasi pada arsitektur karena Conformer awalnya didesain untuk encoder. Anda perlu memastikan bahwa mekanisme perhatian pada decoder Conformer masih dapat bekerja dengan baik dan tidak melanggar aturan kausal (hanya dapat mengakses token sebelumnya). Mengenai jenis perhatian yang akan digunakan, pada dasarnya semua jenis perhatian yang Anda sebutkan (multi-head attention, grouped query attention, vanilla attention, self-attention) dapat digunakan dan memiliki kelebihan masing-masing. Multi-head attention dan self-attention merupakan pilihan yang umum digunakan pada Transformer dan terbukti efektif dalam banyak tugas. Grouped query attention dan vanilla attention juga dapat dipertimbangkan, tergantung pada sumber daya komputasi dan kompleksitas yang Anda harapkan. Secara keseluruhan, rencana Anda terdengar cukup baik dari perspektif arsitektur. Namun, perlu diingat bahwa performa model juga sangat bergantung pada implementasi yang tepat, pemilihan hyperparameter yang optimal, dan kualitas data yang digunakan. Anda mungkin perlu melakukan beberapa percobaan dan evaluasi untuk menemukan kombinasi yang memberikan hasil terbaik. Mengenai pengurangan loss dan peningkatan akurasi, sulit untuk memprediksi secara pasti tanpa melakukan percobaan langsung. Namun, jika Anda dapat memanfaatkan kelebihan dari Transformer dan Conformer dengan baik, ada kemungkinan bahwa model yang diusulkan dapat memberikan performa yang lebih baik dibandingkan model sebelumnya. Namun, perlu diingat bahwa peningkatan performa tidak selalu signifikan dan mungkin membutuhkan penyesuaian dan optimasi yang cermat."
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivitySummaryBinding

    val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    val BASE_URL = "https://inilho.its.ac.id/summasphere/api/v1/summary/" // ngrok URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.buttonLogout.setOnClickListener {
            googleLogout()
            emailPasswordLogout()
        }

        binding.buttonSummarize.setOnClickListener {
            val userInputText = binding.editTextInputText.text.toString()
            val userInputURL = binding.editTextInputURL.text.toString()
            val inputModel = "gemini"

            if (userInputText.isNotEmpty() || userInputURL.isNotEmpty()) {
                summarize(userInputText, userInputURL, inputModel)
            } else {
                Toast.makeText(this, "Please input text or URL", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun summarize(textInput: String, urlInput: String, model: String) {
        val reqBody = JSONObject().apply {
            put("textInput", if (textInput.isNotEmpty()) textInput else JSONObject.NULL)
            put("urlInput", if (urlInput.isNotEmpty()) urlInput else JSONObject.NULL)
            put("model", model)
        }

        val request = Request.Builder()
            .url(BASE_URL)
            .header("Content-Type", "application/json")
            .post(RequestBody.create(MediaType.parse("application/json"), reqBody.toString()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.e("SummaryActivity", "Error: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@SummarizerActivity, "Request failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()?.string()
                val json = body?.let { JSONObject(it) }
                val code = json?.getInt("code")
                val status = json?.getString("status")
                val message = json?.getString("message")
                val summary = json?.getJSONObject("data")?.getString("summary")

                runOnUiThread {
                    if (code == 200 && status == "success") {
                        Log.d("SummaryActivity", "Summary: $summary")
                        Toast.makeText(this@SummarizerActivity, "Text summarized successfully", Toast.LENGTH_SHORT).show()

                        // Start ResultSummarizerActivity and pass the summary
                        val intent = Intent(this@SummarizerActivity, ResultSummarizerActivity::class.java).apply {
                            putExtra("SUMMARY_RESULT", summary)
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@SummarizerActivity, message ?: "Failed to summarize text", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun googleLogout() {
        googleSignInClient.signOut().addOnCompleteListener(this) {
            clearSession()
            navigateToOnboarding()
            Toast.makeText(this, "Logged out from Google", Toast.LENGTH_SHORT).show()
        }
    }

    private fun emailPasswordLogout() {
        val token = sharedPreferences.getString("token", null)
        if (token != null) {
            val request = Request.Builder()
                .url("https://inilho.its.ac.id/summasphere/api/v1/auth/logout")
                .header("Authorization", "Bearer $token")
                .post(RequestBody.create(null, byteArrayOf()))
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        clearSession()
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Logged out successfully", Toast.LENGTH_SHORT).show()
                            navigateToOnboarding()
                        }
                    }
                }
            })
        }
    }

    private fun clearSession() {
        with(sharedPreferences.edit()) {
            remove("email")
            remove("password")
            remove("token")
            putBoolean("IsLoggedIn", false)
            apply()
        }
    }

    private fun navigateToOnboarding() {
        val intent = Intent(this, OnboardingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
}