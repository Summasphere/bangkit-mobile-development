package com.minervaai.summasphere.ui.profile

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.minervaai.summasphere.databinding.CustomDialogLogoutBinding
import com.minervaai.summasphere.databinding.FragmentProfileBinding
import com.minervaai.summasphere.helper.GoogleLoginHelper
import com.minervaai.summasphere.helper.ProfileViewModelFactory
import com.minervaai.summasphere.helper.ResultState
import com.minervaai.summasphere.ui.onboarding.OnboardingActivity

class ProfileFragment : Fragment() {
    private lateinit var googleSignInHelper: GoogleLoginHelper
    private lateinit var binding: FragmentProfileBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val viewModel: ProfileFragmentViewModel by viewModels {
        ProfileViewModelFactory(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentProfileBinding.inflate(layoutInflater)

        googleSignInHelper = GoogleLoginHelper(requireContext())
        sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        binding.layoutLogOut.setOnClickListener {
            showLogoutDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ensure your binding is not null and correctly references the layout
        binding = FragmentProfileBinding.bind(view)

        // Setup UI components or listeners here
        setupListeners()

        // Now observe LiveData or any asynchronous operations
        observeLogout()
    }

    private fun setupListeners() {
        binding.layoutLogOut.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        val binding = CustomDialogLogoutBinding.inflate(LayoutInflater.from(requireContext()))
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(binding.root)
        val alertDialog = dialogBuilder.create()

        binding.btnLogout.setOnClickListener {
            performLogout()
            alertDialog.dismiss()
        }

        binding.btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun performLogout() {
        viewModel.logout(googleSignInHelper)
        viewModel.logoutWithEmailPassword()
    }

    private fun observeLogout() {
        viewModel.logout(googleSignInHelper).observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> {
                    showLoading(true)
                }
                is ResultState.Success -> {
                    showLoading(false)
                    clearSession()
                    navigateToOnboarding()
                    showToast("Successfully log out")
                }
                is ResultState.Error -> {
                    showLoading(false)
                    showToast("Failed to log out")
                }
            }
        }

        viewModel.logoutWithEmailPassword().observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> {
                    showLoading(true)
                }
                is ResultState.Success -> {
                    showLoading(false)
                    clearSession()
                    navigateToOnboarding()
                    showToast("Successfully log out")
                }
                is ResultState.Error -> {
                    showLoading(false)
                }
            }
        }
    }


    private fun navigateToOnboarding() {
        val intent = Intent(requireActivity(), OnboardingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun clearSession() {
        with(sharedPreferences.edit()) {
            remove("email")
            remove("password")
            remove("token")
            putBoolean("IsLoggedIn", false)
            apply()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
