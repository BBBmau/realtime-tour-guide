package com.example.exploreai.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.example.exploreai.UserInfoActivity
import com.example.exploreai.databinding.ActivitySettingsBinding

class ToggleSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var account: Auth0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        account = Auth0(
            "LSN9l3iMrWtRyrLgTTjbOGJIbMbkFF2i",
            "dev-y5kpzumi7ghqmuzh.us.auth0.com"
        )
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