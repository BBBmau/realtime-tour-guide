package com.example.exploreai.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.example.exploreai.UserInfoActivity
import com.example.exploreai.databinding.ActivitySettingsBinding
import com.example.exploreai.R
import com.example.exploreai.utils.TokenManager

class ToggleSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var account: Auth0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        val userName = TokenManager.getUser(this)
        binding.loggedInAs.text = "Logged in as $userName"
        setContentView(binding.root)
        account = Auth0(
            "LSN9l3iMrWtRyrLgTTjbOGJIbMbkFF2i",
            "dev-y5kpzumi7ghqmuzh.us.auth0.com"
        )
        setupLissteners()
    }

    private fun setupLissteners() {
        binding.topAppBar.setNavigationOnClickListener { finish() }

        // Get reference to the divider (make sure to add this ID to your XML)
        val timerDivider = findViewById<View>(R.id.notificationTimerDivider)
        
        // Set initial visibility state
        binding.notificationTimerPickerLayout.visibility = View.GONE
        timerDivider?.visibility = View.GONE
        
        binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Toggle visibility based on switch state
            binding.notificationTimerPickerLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
            timerDivider?.visibility = if (isChecked) View.VISIBLE else View.GONE
            
            // Also update enabled state
            binding.notificationTimerPicker.isEnabled = isChecked
        }

        // Initialize and set up NumberPicker for notification timer
        with(binding.notificationTimerPicker) {
            minValue = 1
            maxValue = 60
            value = 15  // Default value (15 minutes)
            
            setOnValueChangedListener { _, _, newVal ->
                // Handle the new timer value
                Log.d("NumberPicker", "New notification timer: $newVal minutes")
                // TODO: Save this value to your preferences/settings
            }
        }
        
        // Set up destination field
        binding.destinationField.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val destination = binding.destinationField.editText?.text.toString()
                // TODO: Save destination to your preferences/settings
                Log.d("Destination", "Destination set to: $destination")
            }
        }

        binding.conversationHistoryButton.setOnClickListener {
            startActivity(Intent(this, ConversationHistoryListActivity::class.java))
        }

        binding.logoutButton.setOnClickListener {
            logout()
        }

    }

    private fun logout() {
        WebAuthProvider.logout(account)
            .withScheme("demo")
            .start(this, object: Callback<Void?, AuthenticationException> {
                override fun onSuccess(result: Void?) {
                    // The user has been logged out!
                    val intent = Intent(applicationContext, UserInfoActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }

                override fun onFailure(error: AuthenticationException) {
                    // Something went wrong!
                    Log.d("[logout]", "$error")
                }
            })
    }
}