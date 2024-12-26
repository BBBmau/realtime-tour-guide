package com.example.exploreai

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.exploreai.databinding.ActivityUserInfoBinding

class UserInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.continueButton.setOnClickListener {
            // Optional: Validate inputs before proceeding
            if (validateInputs()) {
                startActivity(Intent(this, AssistantActivityActivity::class.java))
                finish() // Optional: prevents going back
            }
        }
    }

    private fun validateInputs(): Boolean {
        val name = binding.nameInput.editText?.text.toString()
        val email = binding.emailInput.editText?.text.toString()

        if (name.isEmpty()) {
            binding.nameInput.error = "Name is required"
            return false
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInput.error = "Valid email is required"
            return false
        }

        return true
    }
}