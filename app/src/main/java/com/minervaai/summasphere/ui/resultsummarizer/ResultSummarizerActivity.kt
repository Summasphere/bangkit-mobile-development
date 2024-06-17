package com.minervaai.summasphere.ui.resultsummarizer

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.minervaai.summasphere.databinding.ActivitySummarizerResultBinding
import com.minervaai.summasphere.ui.summarizer.SummarizerActivity
import java.io.OutputStream

class ResultSummarizerActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySummarizerResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.summarizerResultText.isFocusable = false
        binding.summarizerResultText.isClickable = false

        val summarizerResult = intent.getStringExtra("SUMMARY_RESULT")
        if (!summarizerResult.isNullOrEmpty()) {
            binding.summarizerResultText.setText(summarizerResult)
        }

        binding.btnCopyText.setOnClickListener{
            copyClipboard(binding.summarizerResultText.text.toString())
        }

        binding.btnDownloadPdf.setOnClickListener {
            createPdf(summarizerResult ?: "")
        }

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, SummarizerActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    @SuppressLint("ServiceCast")
    private fun copyClipboard(text: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun createPdf(content: String) {
        val resolver = contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "summarizer_result.pdf")
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
        }

        val uri: Uri? = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        if (uri != null) {
            try {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    writePdf(content, outputStream)
                }
                    showToast("PDF saved to Documents folder")
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Failed to save PDF")
            }
        } else {
            showToast("Failed to create PDF, please try again")
        }
    }

    private fun writePdf(content: String, outputStream: OutputStream) {
        val pdfWriter = PdfWriter(outputStream)
        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument)
        document.add(Paragraph(content))
        document.close()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}