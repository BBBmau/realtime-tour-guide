package com.mau.exploreai

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mau.exploreai.assistant.AssistantActivityActivity
import com.mau.exploreai.utils.PreferencesManager
import com.mau.exploreai.utils.TokenManager


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
            // Check if user is already logged in
            if (TokenManager.isLoggedIn(this)) {
                // User is logged in, go directly to AssistantActivity
                startActivity(Intent(this, AssistantActivityActivity::class.java))
            } else {
                // User is not logged in, go to login screen
                startActivity(Intent(this, UserInfoActivity::class.java))
            }
        }
        
        // Close this activity after routing to the appropriate screen
        finish()
    }
}