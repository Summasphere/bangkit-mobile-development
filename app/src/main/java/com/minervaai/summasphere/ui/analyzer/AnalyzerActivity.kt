package com.minervaai.summasphere.ui.analyzer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.minervaai.summasphere.databinding.ActivityAnalyzerBinding
import com.minervaai.summasphere.ui.main.MainActivity
import com.minervaai.summasphere.ui.resultanalyzer.ResultAnalyzerActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit

class AnalyzerActivity : AppCompatActivity() {
    private val pickDocRequest = 1
    private lateinit var binding: ActivityAnalyzerBinding
    private val client = OkHttpClient.Builder()
        .connectTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(180, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .build()

    private val URI = "https://065e-103-246-107-5.ngrok-free.app/api/analyzer"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyzerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.customBtnUpload.setOnClickListener {
            openFilePicker()
        }

        binding.btnAnalyze.setOnClickListener {
            val url = binding.inputUrl.text.toString()
            if (url.isNotEmpty()) {
                analyzerUrl(url)
            } else {
                showToast("Please enter a URL or upload a file")
            }
        }

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    @Deprecated("Deprecated")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickDocRequest && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                val fileName = getFileName(uri)
                fileName?.let {
                    Toast.makeText(this, "File: $fileName", Toast.LENGTH_SHORT).show()
                    analyzerFile(uri)
                }
            }
        }
    }

    private fun analyzerFile(uri: Uri) {
        setLoading(true)
        val file = createFileFromUri(uri)
        val mimeType = contentResolver.getType(uri)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("media", "android")
            .addFormDataPart("mode", "pdf")
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
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    setLoading(false)
                    if (response.isSuccessful) {
                        val responseData = response.body?.string()
                        Log.d("AnalyzerActivityFile", "Server response: $responseData") // Log the response data
                        responseData?.let {
                            val jsonObject = JSONObject(it)
                            if (jsonObject.has("analysis")) {
                                val analysis = jsonObject.getJSONObject("analysis")
                                val wordcloudUrl = analysis.getString("wordcloud")
                                val topicsUrl = analysis.getString("topic_distribution")
                                val textResultAnalyzer = analysis.getString("topic_desc")

                                Log.d("AnalyzerActivityFile", "Wordcloud URL: $wordcloudUrl")
                                Log.d("AnalyzerActivityFile", "Topics URL: $topicsUrl")
                                Log.d("AnalyzerActivityFile", "Text Result: $textResultAnalyzer")

                                // Save analysis data to a temporary file
                                val analysisData = JSONObject()
                                analysisData.put("wordcloud", wordcloudUrl)
                                analysisData.put("topic_distribution", topicsUrl)
                                analysisData.put("topic_desc", textResultAnalyzer)

                                val tempFile = File.createTempFile("analysis", ".json", cacheDir)
                                tempFile.writeText(analysisData.toString())

                                val intent = Intent(this@AnalyzerActivity, ResultAnalyzerActivity::class.java).apply {
                                    putExtra("ANALYSIS_FILE_PATH", tempFile.absolutePath)
                                }
                                startActivity(intent)
                            } else {
                                showToast("Failed to upload file: Invalid response structure")
                            }
                        }
                    } else {
                        showToast("Failed to upload file: ${response.message}")
                    }
                }
            }
        })
    }

    private fun analyzerUrl(url: String) {
        setLoading(true)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("media", "android")
            .addFormDataPart("mode", "link")
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
                    showToast("Failed to analyze URL: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    setLoading(false)
                    if (response.isSuccessful) {
                        val responseData = response.body?.string()
                        Log.d("AnalyzerActivityUrl", "Server response: $responseData") // Log the response data
                        responseData?.let {
                            val jsonObject = JSONObject(it)
                            if (jsonObject.has("analysis")) {
                                val analysis = jsonObject.getJSONObject("analysis")
                                val wordcloudUrl = analysis.getString("wordcloud")
                                val topicsUrl = analysis.getString("topic_distribution")
                                val textResultAnalyzer = analysis.getString("topic_desc")

                                Log.d("AnalyzerActivityUrl", "Analysis: $analysis")
                                Log.d("AnalyzerActivityUrl", "Wordcloud URL: $wordcloudUrl")
                                Log.d("AnalyzerActivityUrl", "Topics URL: $topicsUrl")
                                Log.d("AnalyzerActivityUrl", "Text Result: $textResultAnalyzer")

                                // Save analysis data to a temporary file
                                val analysisData = JSONObject()
                                analysisData.put("wordcloud", wordcloudUrl)
                                analysisData.put("topic_distribution", topicsUrl)
                                analysisData.put("topic_desc", textResultAnalyzer)

                                val tempFile = File.createTempFile("analysis", ".json", cacheDir)
                                tempFile.writeText(analysisData.toString())

                                val intent = Intent(this@AnalyzerActivity, ResultAnalyzerActivity::class.java).apply {
                                    putExtra("ANALYSIS_FILE_PATH", tempFile.absolutePath)
                                }
                                startActivity(intent)
                            } else {
                                showToast("Failed to analyze URL: Invalid response structure")
                            }
                        }
                    } else {
                        showToast("Failed to analyze URL: ${response.message}")
                    }
                }
            }
        })
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

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "text/plain", "text/csv"))
        startActivityForResult(intent, pickDocRequest)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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


// ORIGINAL CODE JANGAN DIUTAK ATIK OK.
//class AnalyzerActivity : AppCompatActivity() {
//    private val pickDocRequest = 1
//    private lateinit var binding: ActivityAnalyzerBinding
//    private val client = OkHttpClient()
//    private val URI = "https://b97a-118-96-97-147.ngrok-free.app/analyzer"
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityAnalyzerBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        binding.customBtnUpload.setOnClickListener {
//            openFilePicker()
//        }
//
//        binding.btnAnalyze.setOnClickListener {
//            val url = binding.inputUrl.text.toString()
//            if (url.isNotEmpty()) {
//                analyzeUrl(url)
//            } else {
//                showToast("Please enter a URL or upload a file")
//            }
//        }
//
//        binding.btnBack.setOnClickListener {
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//    }
//
//    private fun openFilePicker() {
//        val intent = Intent(Intent.ACTION_GET_CONTENT)
//        intent.type = "application/pdf"
//        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "text/plain", "text/csv"))
//        startActivityForResult(intent, pickDocRequest)
//    }
//
//    @Deprecated("Deprecated")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == pickDocRequest && resultCode == Activity.RESULT_OK) {
//            data?.data?.also { uri ->
//                val fileName = getFileName(uri)
//                fileName?.let {
//                    Toast.makeText(this, "File: $fileName", Toast.LENGTH_SHORT).show()
//                    uploadFile(uri)
//                }
//            }
//        }
//    }
//
//    private fun getFileName(uri: Uri): String? {
//        val mimeType = contentResolver.getType(uri)
//        if (mimeType != null) {
//            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
//            if (extension in listOf("pdf", "txt", "csv")) {
//                val cursor = contentResolver.query(uri, null, null, null, null)
//                cursor?.use {
//                    if (it.moveToFirst()) {
//                        val nameIndex = it.getColumnIndex("_display_name")
//                        if (nameIndex != -1) {
//                            return it.getString(nameIndex)
//                        }
//                    }
//                }
//            }
//        }
//        return null
//    }
//
//    private fun uploadFile(uri: Uri) {
//        setLoading(true)
//        val file = createFileFromUri(uri)
//        val mimeType = contentResolver.getType(uri)
//        val requestBody = MultipartBody.Builder()
//            .setType(MultipartBody.FORM)
//            .addFormDataPart("media", "android")
//            .addFormDataPart("mode", "pdf")
//            .addFormDataPart("file", file.name, file.asRequestBody(mimeType?.toMediaTypeOrNull()))
//            .build()
//
//        val request = Request.Builder()
//            .url(URI)
//            .post(requestBody)
//            .build()
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                runOnUiThread {
//                    setLoading(false)
//                    showToast("Failed to upload file: ${e.message}")
//                }
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                runOnUiThread {
//                    setLoading(false)
//                    if (response.isSuccessful) {
//                        val responseData = response.body?.string()
//                        Log.d("AnalyzerActivityFile", "Server response: $responseData") // Log the response data
//                        responseData?.let {
//                            val jsonObject = JSONObject(it)
//                            if (jsonObject.has("analysis")) {
//                                val analysis = jsonObject.getJSONObject("analysis")
//                                val wordcloudUrl = analysis.getString("wordcloud")
//                                val topicsUrl = analysis.getString("topic_distribution")
//
//                                Log.d("AnalyzerActivityFile", "Analysis: $analysis")
//                                Log.d("AnalyzerActivityFile", "Wordcloud URL: $wordcloudUrl")
//                                Log.d("AnalyzerActivityFile", "Topics URL: $topicsUrl")
//
//                                // Save analysis data to a temporary file
//                                val analysisData = JSONObject()
//                                analysisData.put("wordcloud", wordcloudUrl)
//                                analysisData.put("topic_distribution", topicsUrl)
//
//                                val tempFile = File.createTempFile("analysis", ".json", cacheDir)
//                                tempFile.writeText(analysisData.toString())
//
//                                val intent = Intent(this@AnalyzerActivity, ResultAnalyzerActivity::class.java).apply {
//                                    putExtra("ANALYSIS_FILE_PATH", tempFile.absolutePath)
//                                }
//                                startActivity(intent)
//                            } else {
//                                showToast("Failed to upload file: Invalid response structure")
//                            }
//                        }
//                    } else {
//                        showToast("Failed to upload file: ${response.message}")
//                    }
//                }
//            }
//        })
//    }
//
//    private fun createFileFromUri(uri: Uri): File {
//        val inputStream: InputStream? = contentResolver.openInputStream(uri)
//        val tempFile = File.createTempFile("upload", null, cacheDir)
//        tempFile.deleteOnExit()
//        val out = FileOutputStream(tempFile)
//
//        inputStream?.use { input ->
//            out.use { output ->
//                input.copyTo(output)
//            }
//        }
//        return tempFile
//    }
//
//    private fun analyzeUrl(url: String) {
//        setLoading(true)
//        val requestBody = MultipartBody.Builder()
//            .setType(MultipartBody.FORM)
//            .addFormDataPart("media", "android")
//            .addFormDataPart("mode", "link")
//            .addFormDataPart("url", url)
//            .build()
//
//        val request = Request.Builder()
//            .url(URI)
//            .post(requestBody)
//            .build()
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                runOnUiThread {
//                    setLoading(false)
//                    showToast("Failed to analyze URL: ${e.message}")
//                }
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                runOnUiThread {
//                    setLoading(false)
//                    if (response.isSuccessful) {
//                        val responseData = response.body?.string()
//                        Log.d("AnalyzerActivityUrl", "Server response: $responseData") // Log the response data
//                        responseData?.let {
//                            val jsonObject = JSONObject(it)
//                            if (jsonObject.has("analysis")) {
//                                val analysis = jsonObject.getJSONObject("analysis")
//                                val wordcloudUrl = analysis.getString("wordcloud")
//                                val topicsUrl = analysis.getString("topic_distribution")
//
//                                Log.d("AnalyzerActivityUrl", "Analysis: $analysis")
//                                Log.d("AnalyzerActivityUrl", "Wordcloud URL: $wordcloudUrl")
//                                Log.d("AnalyzerActivityUrl", "Topics URL: $topicsUrl")
//
//                                // Save analysis data to a temporary file
//                                val analysisData = JSONObject()
//                                analysisData.put("wordcloud", wordcloudUrl)
//                                analysisData.put("topic_distribution", topicsUrl)
//
//                                val tempFile = File.createTempFile("analysis", ".json", cacheDir)
//                                tempFile.writeText(analysisData.toString())
//
//                                val intent = Intent(this@AnalyzerActivity, ResultAnalyzerActivity::class.java).apply {
//                                    putExtra("ANALYSIS_FILE_PATH", tempFile.absolutePath)
//                                }
//                                startActivity(intent)
//                            } else {
//                                showToast("Failed to analyze URL: Invalid response structure")
//                            }
//                        }
//                    } else {
//                        showToast("Failed to analyze URL: ${response.message}")
//                    }
//                }
//            }
//        })
//    }
//
//    private fun showToast(message: String) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//    }
//
//    private fun setLoading(value: Boolean) {
//        runOnUiThread {
//            binding.apply {
//                if (value) {
//                    progressIndicator.visibility = View.VISIBLE
//                } else {
//                    progressIndicator.visibility = View.INVISIBLE
//                }
//            }
//        }
//    }
//}

//import android.app.Activity
//import android.content.Intent
//import android.net.Uri
//import android.os.Bundle
//import android.view.View
//import android.webkit.MimeTypeMap
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.minervaai.summasphere.R
//import com.minervaai.summasphere.databinding.ActivityAnalyzerBinding

//@Suppress("DEPRECATION")
//class AnalyzerActivity : AppCompatActivity() {
//    private val pickDocRequest = 1
//    private lateinit var binding: ActivityAnalyzerBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_analyzer)
//
//        binding.customBtnUpload.setOnClickListener {
//            openFilePicker()
//        }
//
//        binding.btnAnalyze.setOnClickListener {
//            val url = binding.inputUrl.text.toString()
//            if (url.isNotEmpty()) {
//                showToast("URL : $url")
//            } else {
//                showToast("Please enter a URL or upload a file")
//            }
//
//        }
//    }
//
//    private fun openFilePicker() {
//        val intent = Intent(Intent.ACTION_GET_CONTENT)
//        intent.type = "application/pdf"
//        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "text/plain", "text/csv"))
//        startActivityForResult(intent, pickDocRequest)
//    }
//
//    @Deprecated("Deprecated")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == pickDocRequest && resultCode == Activity.RESULT_OK) {
//            data?.data?.also { uri ->
//                val fileName = getFileName(uri)
//                fileName?.let {
//                    Toast.makeText(this, "File: $fileName", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
//
//    private fun getFileName(uri: Uri): String? {
//        val mimeType = contentResolver.getType(uri)
//        if (mimeType != null) {
//            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
//            if (extension in listOf("pdf", "txt", "csv")) {
//                val cursor = contentResolver.query(uri, null, null, null, null)
//                cursor?.use {
//                    if (it.moveToFirst()) {
//                        val nameIndex = it.getColumnIndex("_display_name")
//                        if (nameIndex != -1) {
//                            return it.getString(nameIndex)
//                        }
//                    }
//                }
//            }
//        }
//        return null
//    }
//
//    private fun showToast(message: String) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//    }
//    private fun setLoading(value : Boolean) {
//        runOnUiThread {
//            binding.apply {
//                if (value) {
//                    progressIndicator.visibility = View.VISIBLE
//                } else {
//                    progressIndicator.visibility = View.INVISIBLE
//                }
//            }
//        }
//    }
//}