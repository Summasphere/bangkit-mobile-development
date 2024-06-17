package com.minervaai.summasphere.ui.resultanalyzer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.property.TextAlignment
import com.minervaai.summasphere.databinding.ActivityAnalyzerResultBinding
import com.squareup.picasso.Picasso
import java.io.OutputStream

class ResultAnalyzerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAnalyzerResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyzerResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val analyzerResult = intent.getStringExtra("TEXT_RESULT") ?: ""
        val wordcloudUrl = intent.getStringExtra("WORDCLOUD_URL")
        val topicsUrl = intent.getStringExtra("TOPICS_URL")

        binding.analyzerResultText.isFocusable = false
        binding.analyzerResultText.isClickable = false

        if (analyzerResult.isNotEmpty()) {
            binding.analyzerResultText.setText(analyzerResult)
        }
        if (!wordcloudUrl.isNullOrEmpty()) {
            Picasso.get().load(wordcloudUrl).into(binding.ivResultWordcloud)
            binding.ivResultWordcloud.visibility = View.VISIBLE
        } else {
            binding.ivResultWordcloud.visibility = View.GONE
        }

        if (!topicsUrl.isNullOrEmpty()) {
            Picasso.get().load(topicsUrl).into(binding.ivResultTopics)
            binding.ivResultTopics.visibility = View.VISIBLE
        } else {
            binding.ivResultTopics.visibility = View.GONE
        }

        binding.btnDownloadPdf.setOnClickListener {
            createPdf(analyzerResult)
        }

        binding.btnCopyText.setOnClickListener{
            copyClipboard(binding.analyzerResultText.text.toString())
        }
    }

    private fun createPdf(analyzerResult: String) {
        val resolver = contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "summarizer_result.pdf")
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/MyApp")
        }

        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        if (uri != null) {
            try {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    val wordcloudPath = intent.getStringExtra("WORDCLOUD_URL")
                    val topicsPath = intent.getStringExtra("TOPICS_URL")
                    writePdf(analyzerResult, wordcloudPath, topicsPath, outputStream)
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

    private fun writePdf(content: String, wordcloudPath: String?, topicsPath: String?, outputStream: OutputStream) {
        val pdfWriter = PdfWriter(outputStream)
        val pdfDocument = com.itextpdf.kernel.pdf.PdfDocument(pdfWriter)
        val document = Document(pdfDocument, PageSize.A4)

        val font = PdfFontFactory.createFont()

        document.add(
            Paragraph(content).setFont(font).setFontSize(12f).setTextAlignment(
            TextAlignment.LEFT).setMarginBottom(10f))

        if (!wordcloudPath.isNullOrEmpty()) {
            val wordcloudImage = Image(ImageDataFactory.create(wordcloudPath))
            wordcloudImage.scaleToFit(400f, 400f)
            document.add(wordcloudImage)
        }

        if (!topicsPath.isNullOrEmpty()) {
            val topicsImage = Image(ImageDataFactory.create(topicsPath))
            topicsImage.scaleToFit(400f, 400f)
            document.add(topicsImage)
        }

        document.close()
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
