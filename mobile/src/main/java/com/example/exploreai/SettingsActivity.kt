package com.example.exploreai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.exploreai.databinding.ActivitySettingsBinding

class ToggleSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToggleListeners()
    }

    private fun setupToggleListeners() {
        binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Handle notification toggle
        }

        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Handle dark mode toggle
        }

        binding.soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Handle sound toggle
        }
    }
}