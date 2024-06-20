package com.minervaai.summasphere.ui.summarizer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.minervaai.summasphere.R
import com.minervaai.summasphere.data.retrofit.ApiService
import com.minervaai.summasphere.databinding.ActivitySummarizerBinding
import com.minervaai.summasphere.ui.main.MainActivity
import com.minervaai.summasphere.ui.resultanalyzer.ResultAnalyzerActivity
import com.minervaai.summasphere.ui.resultsummarizer.ResultSummarizerActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit

class SummarizerActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySummarizerBinding
    private val pickDocRequest = 1
    private val client = OkHttpClient.Builder()
        .connectTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(180, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .build()

    private val URI = "https://9477-103-246-107-5.ngrok-free.app/api/summarize"
    private var fileUri: Uri? = null
    private var lastInputType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySummarizerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.customBtnUpload.setOnClickListener {
            openFilePicker()
        }

        binding.btnSummarize.setOnClickListener {
            val url = binding.inputUrl.text.toString()
            val text = binding.inputText.text.toString()
            if (fileUri != null) {
                lastInputType = "File"
                summarizerFile(fileUri!!)
            } else if (url.isNotEmpty()) {
                lastInputType = "URL"
                summarizerUrl(url)
            } else if (text.isNotEmpty()) {
                lastInputType = "Text"
                summarizerText(text)
            } else {
                showToast("Please provide input")
                return@setOnClickListener
            }
            showToast("Processing $lastInputType with model ${getSelectedModel()}")
        }
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnClearText.setOnClickListener {
            clearInputs()
            Toast.makeText(this, "All inputs cleared", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickDocRequest && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                val fileName = getFileName(uri)
                fileName?.let {
                    fileUri = uri
                    Toast.makeText(this, "File: $fileName selected", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun summarizerFile(uri: Uri) {
        setLoading(true)
        val file = createFileFromUri(uri)
        val mimeType = contentResolver.getType(uri)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("mode", "pdf")
            .addFormDataPart("model", getSelectedModel())
            .addFormDataPart("file", file.name, file.asRequestBody(mimeType?.toMediaTypeOrNull()))
            .build()

        val request = Request.Builder()
            .url(URI)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    setLoading(false)
                    showToast("Failed to upload file: ${e.message}")
                    Log.e("SummarizerActivityFile", "Failed to upload file: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    setLoading(false)
                    if (response.isSuccessful) {
                        val responseData = response.body?.string()
                        Log.d("SummarizerActivityFile", "Server response: $responseData")
                        responseData?.let {
                            val jsonObject = JSONObject(it)
                            if (jsonObject.has("summary")) {
                                val summary = jsonObject.getString("summary")

                                Log.d("SummarizerActivityFile", "Summary: $summary")

                                // Save analysis data to a temporary file
                                val summarizerFiles = JSONObject()
                                summarizerFiles.put("summary", summary)

                                val tempFile = File.createTempFile("summarizer", ".json", cacheDir)
                                tempFile.writeText(summarizerFiles.toString())

                                val intent = Intent(this@SummarizerActivity, ResultSummarizerActivity::class.java).apply {
                                    putExtra("SUMMARIZER_FILE_PATH", tempFile.absolutePath)
                                }
                                startActivity(intent)
                            } else {
                                showToast("Failed to summarizerFiles: Invalid response structure")
                            }
                        }
                    } else {
                        showToast("Failed to summarizerFiles: ${response.message}")
                        Log.e("SummarizerActivityFile", "Failed to summarizerFiles: ${response.message}")
                    }
                }
            }
        })
    }

    private fun summarizerUrl(url: String) {
        setLoading(true)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("mode", "link")
            .addFormDataPart("model", getSelectedModel())
            .addFormDataPart("url", url)
            .build()

        val request = Request.Builder()
            .url(URI)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    setLoading(false)
                    showToast("Failed to summarizerURL: ${e.message}")
                    Log.e("SummarizerActivityUrl", "Failed to summarizerURL: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    setLoading(false)
                    if (response.isSuccessful) {
                        val responseData = response.body?.string()
                        Log.d("SummarizerActivityUrl", "Server response: $responseData")
                        responseData?.let {
                            val jsonObject = JSONObject(it)
                            if (jsonObject.has("summary")) {
                                val summary = jsonObject.getString("summary")

                                Log.d("SummarizerActivityUrl", "Summary: $summary")

                                // Save analysis data to a temporary file
                                val summarizerURL = JSONObject()
                                summarizerURL.put("summary", summary)

                                val tempFile = File.createTempFile("summary", ".json", cacheDir)
                                tempFile.writeText(summarizerURL.toString())

                                val intent = Intent(this@SummarizerActivity, ResultSummarizerActivity::class.java).apply {
                                    putExtra("SUMMARIZER_FILE_PATH", tempFile.absolutePath)
                                }
                                startActivity(intent)
                            } else {
                                showToast("Failed to summarizerURL: Invalid response structure")
                            }
                        }
                    } else {
                        showToast("Failed to summarizerURL: ${response.message}")
                        Log.e("SummarizerActivityUrl", "Failed to summarizerURL: ${response.message}")
                    }
                }
            }
        })
    }

    private fun summarizerText(text: String) {
        setLoading(true)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("mode", "text")
            .addFormDataPart("model", getSelectedModel())
            .addFormDataPart("text", text)
            .build()

        val request = Request.Builder()
            .url(URI)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    setLoading(false)
                    showToast("Failed to summarizerText: ${e.message}")
                    Log.e("SummarizerActivityText", "Failed to summarizerText: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    setLoading(false)
                    if (response.isSuccessful) {
                        val responseData = response.body?.string()
                        Log.d("SummarizerActivityText", "Server response: $responseData")
                        responseData?.let {
                            val jsonObject = JSONObject(it)
                            if (jsonObject.has("summary")) {
                                val summary = jsonObject.getString("summary")

                                Log.d("SummarizerActivityText", "Summary: $summary")

                                // Save analysis data to a temporary file
                                val summarizerText = JSONObject()
                                summarizerText.put("summary", summary)

                                val tempFile = File.createTempFile("summary", ".json", cacheDir)
                                tempFile.writeText(summarizerText.toString())

                                val intent = Intent(this@SummarizerActivity, ResultSummarizerActivity::class.java).apply {
                                    putExtra("SUMMARIZER_FILE_PATH", tempFile.absolutePath)
                                }
                                startActivity(intent)
                            } else {
                                showToast("Failed to summarizerText: Invalid response structure")
                            }
                        }
                    } else {
                        showToast("Failed to summary text: ${response.message}")
                        Log.e("SummarizerActivityText", "Failed to summarizerText: ${response.message}")
                    }
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun clearInputs() {
        binding.inputText.setText("")
        binding.inputUrl.setText("")
        binding.customBtnUploadTitle.text = getString(R.string.custom_btn_upload_title)
        binding.customBtnUploadSubtitle.text = getString(R.string.custom_btn_upload_subtitle)
        binding.rgModel.clearCheck()
        fileUri = null
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "text/plain", "text/csv"))
        startActivityForResult(intent, pickDocRequest)
    }

    private fun getFileName(uri: Uri): String? {
        val mimeType = contentResolver.getType(uri)
        if (mimeType != null) {
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            if (extension in listOf("pdf", "txt", "csv")) {
                val cursor = contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex("_display_name")
                        if (nameIndex != -1) {
                            return it.getString(nameIndex)
                        }
                    }
                }
            }
        }
        return null
    }

    private fun createFileFromUri(uri: Uri): File {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload", null, cacheDir)
        tempFile.deleteOnExit()
        val out = FileOutputStream(tempFile)

        inputStream?.use { input ->
            out.use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    private fun getSelectedModel(): String {
        return when (binding.rgModel.checkedRadioButtonId) {
            R.id.rb_gemini -> "gemini"
            R.id.rb_bart -> "bart"
            else -> "gemini" // Default to gemini if none is selected
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setLoading(value: Boolean) {
        runOnUiThread {
            binding.apply {
                if (value) {
                    progressIndicator.visibility = View.VISIBLE
                } else {
                    progressIndicator.visibility = View.INVISIBLE
                }
            }
        }
    }
}
