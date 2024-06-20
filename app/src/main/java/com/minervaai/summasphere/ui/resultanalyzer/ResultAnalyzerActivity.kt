package com.minervaai.summasphere.ui.resultanalyzer

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.minervaai.summasphere.databinding.ActivityAnalyzerResultBinding
import com.minervaai.summasphere.ui.analyzer.AnalyzerActivity
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ResultAnalyzerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAnalyzerResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyzerResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val analysisFilePath = intent.getStringExtra("ANALYSIS_FILE_PATH")
        if (analysisFilePath != null) {
            val analysisFile = File(analysisFilePath)
            if (analysisFile.exists()) {
                val analysisData = JSONObject(analysisFile.readText())
                val textResultAnalyzer = analysisData.optString("topic_desc", "")
                val wordcloudBase64 = analysisData.optString("wordcloud", "")
                val topicsBase64 = analysisData.optString("topic_distribution", "")

                Log.d("ResultAnalyzerActivity", "Text Analyzer Result: $textResultAnalyzer")
                Log.d("ResultAnalyzerActivity", "Wordcloud Base64: $wordcloudBase64")
                Log.d("ResultAnalyzerActivity", "Topics Base64: $topicsBase64")

                binding.analyzerResultText.isFocusable = false
                binding.analyzerResultText.isClickable = false

                if (textResultAnalyzer.isNotEmpty()) {
                    binding.analyzerResultText.setText(textResultAnalyzer)
                } else {
                    Log.d("ResultAnalyzerActivity", "Analyzer Result is empty")
                }

                if (wordcloudBase64.isNotEmpty()) {
                    val wordcloudBitmap = decodeBase64ToBitmap(wordcloudBase64)
                    binding.ivResultWordcloud.setImageBitmap(wordcloudBitmap)
                    binding.ivResultWordcloud.visibility = View.VISIBLE
                } else {
                    binding.ivResultWordcloud.visibility = View.GONE
                    Log.d("ResultAnalyzerActivity", "Wordcloud Base64 is empty")
                }

                if (topicsBase64.isNotEmpty()) {
                    val topicsBitmap = decodeBase64ToBitmap(topicsBase64)
                    binding.ivResultTopics.setImageBitmap(topicsBitmap)
                    binding.ivResultTopics.visibility = View.VISIBLE
                } else {
                    binding.ivResultTopics.visibility = View.GONE
                    Log.d("ResultAnalyzerActivity", "Topics Base64 is empty")
                }

                binding.btnDownloadPdf.setOnClickListener {
                    createPdf(textResultAnalyzer, wordcloudBase64, topicsBase64)
                }

                binding.btnCopyText.setOnClickListener {
                    copyClipboard(binding.analyzerResultText.text.toString())
                }

                binding.btnBack.setOnClickListener {
                    val intent = Intent(this, AnalyzerActivity::class.java)
                    analysisFile.delete()
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                }

            } else {
                showToast("Analysis file not found")
            }
        } else {
            showToast("No analysis file path provided")
        }
    }

    private fun decodeBase64ToBitmap(base64Str: String): Bitmap {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun createPdf(textAnalyzer: String, wordCloudBase64: String, topicsBase64: String) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 24f

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        var yOffset = 40

        fun drawTextJustified(text: String, canvas: Canvas, paint: Paint, x: Float, y: Float, pageWidth: Int): Float {
            val lines = text.split("\n")
            var newY = y
            for (line in lines) {
                val words = line.split(" ")
                var currentLine = ""
                for (word in words) {
                    if (paint.measureText("$currentLine $word") < pageWidth - 40) {
                        currentLine += " $word"
                    } else {
                        canvas.drawText(currentLine.trim(), x, newY, paint)
                        newY += 20
                        currentLine = word
                    }
                }
                canvas.drawText(currentLine.trim(), x, newY, paint)
                newY += 20
            }
            return newY
        }

        // Draw Analyzer title
        canvas.drawText("Analyzer", 20f, yOffset.toFloat(), titlePaint)
        yOffset += 40

        // Draw TextAnalyzer content
        yOffset = drawTextJustified(textAnalyzer, canvas, paint, 20f, yOffset.toFloat(), pageInfo.pageWidth).toInt()
        yOffset += 20

        // Check if need new page
        if (yOffset + 220 > pageInfo.pageHeight) {
            pdfDocument.finishPage(page)
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            yOffset = 40
        }

        // Draw separator
        canvas.drawLine(20f, yOffset.toFloat(), pageInfo.pageWidth - 20f, yOffset.toFloat(), paint)
        yOffset += 20

        // Draw WordCloud
        if (wordCloudBase64.isNotEmpty()) {
            val wordCloudBitmap = decodeBase64ToBitmap(wordCloudBase64)
            val scaledWordCloudBitmap = Bitmap.createScaledBitmap(wordCloudBitmap, wordCloudBitmap.width / 2, wordCloudBitmap.height / 2, true)
            val xPos = (pageInfo.pageWidth - scaledWordCloudBitmap.width) / 2f
            canvas.drawBitmap(scaledWordCloudBitmap, xPos, yOffset.toFloat(), paint)
            yOffset += scaledWordCloudBitmap.height + 20
        }

        // Check if need new page
        if (yOffset + 220 > pageInfo.pageHeight) {
            pdfDocument.finishPage(page)
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            yOffset = 40
        }

        // Draw separator
        canvas.drawLine(20f, yOffset.toFloat(), pageInfo.pageWidth - 20f, yOffset.toFloat(), paint)
        yOffset += 20

        // Draw Topics
        if (topicsBase64.isNotEmpty()) {
            val topicsBitmap = decodeBase64ToBitmap(topicsBase64)
            val scaledTopicsBitmap = Bitmap.createScaledBitmap(topicsBitmap, topicsBitmap.width / 2, topicsBitmap.height / 2, true)
            val xPos = (pageInfo.pageWidth - scaledTopicsBitmap.width) / 2f
            canvas.drawBitmap(scaledTopicsBitmap, xPos, yOffset.toFloat(), paint)
        }

        pdfDocument.finishPage(page)

        // Create Summasphere folder in external storage
        val summasphereFolder = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Summasphere")
        if (!summasphereFolder.exists()) {
            summasphereFolder.mkdirs()
        }

        // Save PDF to Summasphere folder
        val filePath = File(summasphereFolder, "AnalysisResult.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(filePath))
            showToast("PDF created successfully: ${filePath.absolutePath}")
        } catch (e: IOException) {
            e.printStackTrace()
            showToast("Error creating PDF: ${e.message}")
        } finally {
            pdfDocument.close()
        }

        // Open PDF with third-party PDF viewer
        val pdfUri = FileProvider.getUriForFile(this, "$packageName.provider", filePath)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(pdfUri, "application/pdf")
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val chooser = Intent.createChooser(intent, "Open PDF with")
        try {
            startActivity(chooser)
        } catch (e: ActivityNotFoundException) {
            showToast("No application found to open PDF")
        }
    }

    private fun copyClipboard(text: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}