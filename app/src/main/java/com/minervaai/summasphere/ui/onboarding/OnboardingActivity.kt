package com.minervaai.summasphere.ui.onboarding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.minervaai.summasphere.databinding.ActivityOnboardingBinding

class IntroductoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}