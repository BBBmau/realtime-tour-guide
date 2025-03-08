package com.example.exploreai

import android.app.Application
import android.content.Intent
import com.auth0.android.result.Credentials
import androidx.appcompat.app.AppCompatActivity
import com.example.exploreai.databinding.ActivityTutorialBinding
import android.os.Bundle
import android.widget.Toast
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.example.exploreai.utils.PreferencesManager
import com.google.android.material.tabs.TabLayoutMediator

class AssistantApplication : Application() {
    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { ConversationRoomDatabase.getDatabase(this) }
    val repository by lazy { Repository(database.conversationDAO(), database.messageDAO() ) }
}

class StartingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferencesManager = PreferencesManager(this)

        if (preferencesManager.isFirstTimeLaunch()) {
            // First time launch, show tutorial
            startActivity(Intent(this, TutorialActivity::class.java))

            // Mark that tutorial has been shown
            preferencesManager.setFirstTimeLaunchComplete()
        } else {
            // Not first time, go to normal flow
            startActivity(Intent(this, UserInfoActivity::class.java))
        }
    }
}