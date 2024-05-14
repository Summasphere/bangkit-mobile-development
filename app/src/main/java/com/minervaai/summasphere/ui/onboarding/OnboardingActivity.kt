package com.minervaai.summasphere.ui.onboarding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.minervaai.summasphere.R
import com.minervaai.summasphere.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {
    private lateinit var mViewPager: ViewPager2
    private lateinit var binding: ActivityOnboardingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignup.setOnClickListener {
            finish() // belum di intent
        }
        binding.btnGoogle.setOnClickListener {
            finish() // belum di intent
        }
        binding.btnLogin.setOnClickListener {
            finish() // belum di intent
        }

        mViewPager = findViewById(R.id.onboarding_viewpager)
        mViewPager.adapter = OnboardingAdapter(this, this)
        TabLayoutMediator(binding.pageIndicator, mViewPager) { _, _ -> }.attach()
        mViewPager.offscreenPageLimit = 1

    }
}