package com.minervaai.summasphere.helper

import com.minervaai.summasphere.ui.profile.ProfileFragmentViewModel
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ProfileViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileFragmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileFragmentViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
    }
}