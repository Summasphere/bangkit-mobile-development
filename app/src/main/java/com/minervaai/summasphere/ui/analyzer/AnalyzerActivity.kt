package com.minervaai.summasphere.ui.analyzer

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


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.minervaai.summasphere.databinding.ActivityAnalyzerBinding
import com.minervaai.summasphere.ui.main.MainActivity
import com.minervaai.summasphere.ui.resultanalyzer.ResultAnalyzerActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

@Suppress("DEPRECATION")
class AnalyzerActivity : AppCompatActivity() {
    private val pickDocRequest = 1
    private lateinit var binding: ActivityAnalyzerBinding
    private val client = OkHttpClient()

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
                analyzeUrl(url)
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

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "text/plain", "text/csv"))
        startActivityForResult(intent, pickDocRequest)
    }

    @Deprecated("Deprecated")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickDocRequest && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                val fileName = getFileName(uri)
                fileName?.let {
                    Toast.makeText(this, "File: $fileName", Toast.LENGTH_SHORT).show()
                    uploadFile(uri)
                }
            }
        }
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

    private fun uploadFile(uri: Uri) {
        setLoading(true)
        val file = createFileFromUri(uri)
        val mimeType = contentResolver.getType(uri)
        val requestBody = file.asRequestBody(mimeType?.toMediaTypeOrNull())

        val request = Request.Builder()
            .url("https://18dc-182-253-50-140.ngrok-free.app/analyzer")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    setLoading(false)
                    showToast("Failed to upload file")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    setLoading(false)
                    if (response.isSuccessful) {
                        val responseData = response.body?.string()
                        responseData?.let {
                            val jsonObject = JSONObject(it)
                            val textResult = jsonObject.getString("text")
                            val wordcloudUrl = jsonObject.getString("wordcloud")
                            val topicsUrl = jsonObject.getString("topics")

                            val intent = Intent(this@AnalyzerActivity, ResultAnalyzerActivity::class.java).apply {
                                putExtra("TEXT_RESULT", textResult)
                                putExtra("WORDCLOUD_URL", wordcloudUrl)
                                putExtra("TOPICS_URL", topicsUrl)
                            }
                            startActivity(intent)

                        }
                    } else {
                        showToast("Failed to upload file")
                    }
                }
            }
        })
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

    private fun analyzeUrl(url: String) {
        setLoading(true)
        val requestBody = url.toRequestBody("text/plain".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("https://18dc-182-253-50-140.ngrok-free.app/analyzer")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    setLoading(false)
                    showToast("Failed to analyze URL")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    setLoading(false)
                    if (response.isSuccessful) {
                        val responseData = response.body?.string()
                        responseData?.let {
                            val jsonObject = JSONObject(it)
                            val textResult = jsonObject.getString("text")
                            val wordcloudUrl = jsonObject.getString("wordcloud")
                            val topicsUrl = jsonObject.getString("topics")

                            // Start ResultAnalyzerActivity and pass data
                            val intent = Intent(this@AnalyzerActivity, ResultAnalyzerActivity::class.java).apply {
                                putExtra("TEXT_RESULT", textResult)
                                putExtra("WORDCLOUD_URL", wordcloudUrl)
                                putExtra("TOPICS_URL", topicsUrl)
                            }
                            startActivity(intent)

                        }
                    } else {
                        showToast("Failed to analyze URL")
                    }
                }
            }
        })
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

