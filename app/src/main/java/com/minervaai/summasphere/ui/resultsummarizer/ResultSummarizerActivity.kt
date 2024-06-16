package com.minervaai.summasphere.ui.resultsummarizer

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.minervaai.summasphere.databinding.ActivitySummarizerResultBinding
import com.minervaai.summasphere.ui.summarizer.SummarizerActivity

class ResultSummarizerActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySummarizerResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Make EditText readonly
        binding.summarizerResultText.isFocusable = false
        binding.summarizerResultText.isClickable = false

        // Get summary result from Intent
        val summaryResult = intent.getStringExtra("SUMMARY_RESULT")
        if (!summaryResult.isNullOrEmpty()) {
            binding.summarizerResultText.setText(summaryResult)
        }

        binding.btnCopyText.setOnClickListener{
            copyClipboard(binding.summarizerResultText.text.toString())
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
}