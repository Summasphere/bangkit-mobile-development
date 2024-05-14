package com.minervaai.summasphere.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.minervaai.summasphere.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {
    private lateinit var title: String
    private lateinit var description: String
    private var imageResource = 0
    private lateinit var ivOnboardingLogo: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var ivOnboarding: LottieAnimationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            title =
                requireArguments().getString(ARG_PARAM1)!!
            description =
                requireArguments().getString(ARG_PARAM2)!!
            imageResource =
                requireArguments().getInt(ARG_PARAM3)
        }
    }

    private var _binding: FragmentOnboardingBinding? = null

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        val view = binding.root
        ivOnboardingLogo = binding.ivOnboardingLogo
        tvTitle = binding.tvOnboardingTitle
        tvDescription = binding.tvOnboardingDescription
        ivOnboarding = binding.ivOnboarding
        tvTitle.text = title
        tvDescription.text = description
        ivOnboarding.setAnimation(imageResource)
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        private const val ARG_PARAM3 = "param3"
        fun newInstance(
            title: String,
            description: String,
            imageResource: Int
        ): OnboardingFragment {
            val fragment =
                OnboardingFragment()
            val args = Bundle()
            args.putString(
                ARG_PARAM1,
                title
            )
            args.putString(
                ARG_PARAM2,
                description
            )
            args.putInt(
                ARG_PARAM3,
                imageResource
            )
            fragment.arguments = args
            return fragment
        }
    }
}