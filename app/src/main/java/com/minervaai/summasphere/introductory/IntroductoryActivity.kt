package com.minervaai.summasphere.introductory

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.minervaai.summasphere.databinding.ActivityLandingBinding

class IntroductoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLandingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}