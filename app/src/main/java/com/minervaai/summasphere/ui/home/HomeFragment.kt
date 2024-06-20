package com.minervaai.summasphere.ui.home

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.minervaai.summasphere.databinding.FragmentHomeBinding
import com.minervaai.summasphere.ui.analyzer.AnalyzerActivity
import com.minervaai.summasphere.ui.summarizer.SummarizerActivity

class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Setting click listeners after binding is initialized
        binding.customBtnSummarizer.setOnClickListener {
            val intent = Intent(requireContext(), SummarizerActivity::class.java)
            startActivity(intent)
        }

        binding.customBtnAnalyzer.setOnClickListener {
            val intent = Intent(requireContext(), AnalyzerActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}
