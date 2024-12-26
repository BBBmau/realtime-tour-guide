package com.example.exploreai

import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.exploreai.databinding.ActivityAssistantBinding

class AssistantActivityActivity : AppCompatActivity() {

    private lateinit var microphoneIcon: ImageView
    private lateinit var statusText: TextView
    private lateinit var pulseAnimation: Animation
    private var isSpeaking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssistantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, ToggleSettingsActivity::class.java))
        }


        microphoneIcon = findViewById(R.id.microphoneIcon)
        statusText = findViewById(R.id.statusText)
        pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse)

        // Optional: Add click listener to toggle speaking state
        microphoneIcon.setOnClickListener {
            toggleSpeakingState()
        }
    }

    private fun toggleSpeakingState() {
        isSpeaking = !isSpeaking
        updateUI()
    }

    private fun updateUI() {
        if (isSpeaking) {
            microphoneIcon.startAnimation(pulseAnimation)
            statusText.text = "Speaking..."
            microphoneIcon.setColorFilter(getColor(R.color.primary))
            statusText.setTextColor(getColor(R.color.primary))
        } else {
            microphoneIcon.clearAnimation()
            statusText.text = "Idle"
            microphoneIcon.setColorFilter(getColor(androidx.appcompat.R.color.abc_background_cache_hint_selector_material_dark))
            statusText.setTextColor(getColor(R.color.secondary))
        }
    }

    // Call this method when you want to show that the assistant is speaking
    fun startSpeaking() {
        isSpeaking = true
        updateUI()
    }

    // Call this method when the assistant stops speaking
    fun stopSpeaking() {
        isSpeaking = false
        updateUI()
    }


    private lateinit var binding: ActivityAssistantBinding

}
