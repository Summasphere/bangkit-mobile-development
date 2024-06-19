package com.minervaai.summasphere.ui.onboarding

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.minervaai.summasphere.R

class OnboardingAdapter(
    fragmentActivity: FragmentActivity,
    private val context: Context
) :
    FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OnboardingFragment.newInstance(
                context.resources.getString(R.string.tv_title_onboarding_1),
                context.resources.getString(R.string.tv_description_onboarding_1),
                R.raw.lottie_productive
            )
            1 -> OnboardingFragment.newInstance(
                context.resources.getString(R.string.tv_title_onboarding_2),
                context.resources.getString(R.string.tv_description_onboarding_2),
                R.raw.lottie_document
            )
            else -> OnboardingFragment.newInstance(
                context.resources.getString(R.string.tv_title_onboarding_3),
                context.resources.getString(R.string.tv_description_onboarding_3),
                R.raw.lottie_analyze
            )
        }
    }

    override fun getItemCount(): Int {
        return 3
    }
}