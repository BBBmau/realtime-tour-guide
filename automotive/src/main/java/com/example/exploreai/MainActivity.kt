package com.example.exploreai

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private lateinit var launchButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        launchButton = findViewById(R.id.launchButton)

        // Check if Android Auto is installed
        updateStatus()

        // Launch button action
        launchButton.setOnClickListener {
            launchAndroidAuto()
        }
    }

    private fun updateStatus() {
        val isAndroidAutoInstalled = packageManager.getLaunchIntentForPackage("com.google.android.projection.gearhead") != null
        statusText.text = if (isAndroidAutoInstalled) {
            "Android Auto is installed"
        } else {
            "Android Auto is not installed"
        }
    }

    private fun launchAndroidAuto() {
        val intent = packageManager.getLaunchIntentForPackage("com.google.android.projection.gearhead")
        if (intent != null) {
            startActivity(intent)
        } else {
            statusText.text = "Android Auto not installed"
        }
    }
}