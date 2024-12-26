package com.example.exploreai

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.exploreai.databinding.ActivityAssistantBinding

class AssistantActivityActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAssistantBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssistantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, ToggleSettingsActivity::class.java))
        }
    }
}