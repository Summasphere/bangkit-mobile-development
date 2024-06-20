package com.minervaai.summasphere.ui.main

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.minervaai.summasphere.R
import com.minervaai.summasphere.databinding.ActivityMainBinding

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            val bottomNavigationView = binding.bottomNavigationView
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController

            NavigationUI.setupWithNavController(bottomNavigationView, navController)

            bottomNavigationView.setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.home_fragment -> {
                        navController.navigate(R.id.home_fragment)
                        true
                    }
                    R.id.history_fragment -> {
                        navController.navigate(R.id.history_fragment)
                        true
                    }
                    R.id.profile_fragment -> {
                        navController.navigate(R.id.profile_fragment)
                        true
                    }
                    else -> false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("MainActivity", "Error: ${e.message}")
        }
    }
}

