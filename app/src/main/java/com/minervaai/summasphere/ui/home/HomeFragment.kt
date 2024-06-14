package com.minervaai.summasphere.ui.home

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.minervaai.summasphere.databinding.FragmentHomeBinding
import com.minervaai.summasphere.ui.analyze.AnalyzeActivity
import com.minervaai.summasphere.ui.summarize.SummaryActivity

class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var binding: FragmentHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.customBtnSummarizer.setOnClickListener {
            val intent = Intent(requireContext(), SummaryActivity::class.java)
            startActivity(intent)
        }

        binding.customBtnAnalyzer.setOnClickListener {
            val intent = Intent(requireContext(), AnalyzeActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
}