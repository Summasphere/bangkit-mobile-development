package com.minervaai.summasphere.ui.resultsummarizer

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.minervaai.summasphere.databinding.ActivitySummarizerResultBinding
import com.minervaai.summasphere.ui.analyzer.AnalyzerActivity
import com.minervaai.summasphere.ui.summarizer.SummarizerActivity
import org.json.JSONObject
import org.slf4j.MDC.put
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ResultSummarizerActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySummarizerResultBinding

    companion object {
        const val REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySummarizerResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val summaryFilePath = intent.getStringExtra("SUMMARIZER_FILE_PATH")
        if (summaryFilePath != null) {
            val analysisFile = File(summaryFilePath)
            if (analysisFile.exists()) {
                val analysisData = JSONObject(analysisFile.readText())
                val textSummarySummarizer = analysisData.optString("summary", "")

                Log.d("ResultSummarizerActivity", "Text Summarizer Result: $textSummarySummarizer")

                binding.summarizerResultText.isFocusable = false
                binding.summarizerResultText.isClickable = false

                if (textSummarySummarizer.isNotEmpty()) {
                    binding.summarizerResultText.setText(textSummarySummarizer)
                } else {
                    Log.d("ResultSummarizerActivity", "Summarizer Result is empty")
                }

                binding.btnDownloadPdf.setOnClickListener {
                    if (binding.summarizerResultText.text.isNotEmpty()) {
                        checkPermissionAndSavePdf(binding.summarizerResultText.text.toString())
                    } else {
                        Toast.makeText(this, "No text to save", Toast.LENGTH_SHORT).show()
                    }
                }


                binding.btnCopyText.setOnClickListener {
                    copyClipboard(binding.summarizerResultText.text.toString())
                }

                binding.btnBack.setOnClickListener {
                    val intent = Intent(this, AnalyzerActivity::class.java)
                    analysisFile.delete()
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                }

                binding.btnBack.setOnClickListener {
                    val intent = Intent(this, SummarizerActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } else {
                showToast("Analysis file not found")
                Log.d("ResultSummarizerActivity", "Analysis file not found at path: $summaryFilePath")
            }
        } else {
            showToast("No analysis file path provided")
            Log.d("ResultSummarizerActivity", "No analysis file path provided")
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                createPdf(binding.summarizerResultText.text.toString())
                Log.d("ResultSummarizerActivity", "Permission granted to write to storage")
            } else {
                showToast("Permission denied to write to storage")
                Log.d("ResultSummarizerActivity", "Permission denied to write to storage")
            }
        }
    }

    private fun checkPermissionAndSavePdf(text: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE)
        } else {
            createPdf(text)
        }
    }

    private fun createPdf(text: String) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()

        // Set up title and regular text paint properties
        titlePaint.textSize = 16f
        titlePaint.color = Color.BLACK
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        paint.color = Color.BLACK

        // Page dimensions and margins
        val pageWidth = 595
        val pageHeight = 842
        val margin = 40f
        val textWidth = pageWidth - 2 * margin

        var currentPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var currentPage = pdfDocument.startPage(currentPageInfo)
        var canvas = currentPage.canvas
        var yPosition = margin

        // Function to add a new page
        fun addNewPage() {
            pdfDocument.finishPage(currentPage)
            currentPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pdfDocument.pages.size + 1).create()
            currentPage = pdfDocument.startPage(currentPageInfo)
            canvas = currentPage.canvas
            yPosition = margin
        }

        // Split text into sections and individual lines
        val sections = text.split("\n\n")
        for (section in sections) {
            val lines = section.split("\n")
            for (line in lines) {
                var textToDraw = line.trim()
                val useTitlePaint = textToDraw.startsWith("##")
                val currentPaint = if (useTitlePaint) titlePaint else paint

                if (useTitlePaint) {
                    textToDraw = textToDraw.substring(2).trim() // Remove '##'
                }

                // Handle text wrapping within the page width
                while (textToDraw.isNotEmpty()) {
                    val textLength = currentPaint.breakText(textToDraw, true, textWidth, null)
                    val lineToDraw = textToDraw.substring(0, textLength)
                    if (yPosition + currentPaint.textSize > pageHeight - margin) { // Check if new page is needed
                        addNewPage()
                    }
                    canvas.drawText(lineToDraw, margin, yPosition, currentPaint)
                    yPosition += if (useTitlePaint) 24f else 18f // Adjust line spacing based on text style
                    textToDraw = textToDraw.substring(textLength).trim() // Remaining text to draw
                }

                // Additional spacing after each section
                if (line == lines.last()) {
                    yPosition += 10f
                }
            }
        }

        pdfDocument.finishPage(currentPage) // Finish the last page

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "Summary.pdf") // File name
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS) // Directory
        }

        val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        try {
            contentResolver.openOutputStream(uri!!).use { outputStream ->
                pdfDocument.writeTo(outputStream)
                openPdf(uri)
                showToast("PDF has been saved")
                Log.d("ResultSummarizerActivity", "PDF has been saved, the location is: ${uri.path}")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            showToast("Error saving PDF")
            Log.e("ResultSummarizerActivity", "Error saving PDF, Status: $e")
        }

        pdfDocument.close()
    }

    private fun openPdf(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        val chooser = Intent.createChooser(intent, "Open with:")
        try {
            startActivity(chooser)
        } catch (e: ActivityNotFoundException) {
            showToast("No application found to open PDF")
        }
    }


    @SuppressLint("ServiceCast")
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