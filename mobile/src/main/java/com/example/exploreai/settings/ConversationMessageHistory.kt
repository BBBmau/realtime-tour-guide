package com.example.exploreai.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.exploreai.databinding.ActivityConversationMessagesBinding

class ConversationMessageHistory : AppCompatActivity() {
    private lateinit var binding: ActivityConversationMessagesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConversationMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {

        binding.backButton.setOnClickListener { finish() }

    }
}