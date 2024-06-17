package com.minervaai.summasphere.ui.resultanalyzer

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.minervaai.summasphere.databinding.ActivityAnalyzerResultBinding
import com.squareup.picasso.Picasso

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
    }
}
