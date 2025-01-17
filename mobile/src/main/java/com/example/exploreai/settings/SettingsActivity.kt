package com.example.exploreai.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.exploreai.databinding.ActivitySettingsBinding

class ToggleSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLissteners()
    }

    private fun setupLissteners() {
        binding.topAppBar.setNavigationOnClickListener { finish() }

        binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Handle notification toggle
        }

        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Handle dark mode toggle
        }

        binding.soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Handle sound toggle
        }

        binding.conversationHistoryButton.setOnClickListener {
            startActivity(Intent(this, ConversationHistoryListActivity::class.java))
        }

    }
}