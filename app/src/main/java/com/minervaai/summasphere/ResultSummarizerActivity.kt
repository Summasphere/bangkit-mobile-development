package com.minervaai.summasphere

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class ResultSummarizerActivity : AppCompatActivity() {
    private lateinit var editTextInput: EditText
    private lateinit var btnCopyText: MaterialButton
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_summarizer)

        editTextInput = findViewById(R.id.editTextInput)
        btnCopyText = findViewById(R.id.btn_copy_text)
        btnBack = findViewById(R.id.btn_back)

        val summaryResult = intent.getStringExtra("SUMMARY_RESULT")
        if(summaryResult != null) {
            editTextInput.setText(summaryResult)
        }

        // Make EditText readonly
        editTextInput.isFocusable = false
        editTextInput.isClickable = false

        btnCopyText.setOnClickListener{
            copyClipboard(editTextInput.text.toString())
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, SummaryActivity::class.java)
            startActivity(intent)
            finish() // Optional: close the current activity
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