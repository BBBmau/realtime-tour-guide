package com.example.exploreai

import android.app.Application
import android.content.Intent
import com.auth0.android.result.Credentials
import androidx.appcompat.app.AppCompatActivity
import com.example.exploreai.databinding.ActivityTutorialBinding
import android.os.Bundle
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.google.android.material.tabs.TabLayoutMediator

class AssistantApplication : Application() {
    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { ConversationRoomDatabase.getDatabase(this) }
    val repository by lazy { Repository(database.conversationDAO(), database.messageDAO() ) }

}

// TutorialActivity.kt
class TutorialActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTutorialBinding
    private lateinit var pagerAdapter: TutorialPagerAdapter
    private lateinit var account: Auth0

    private fun loginWithBrowser() {
        // Setup the WebAuthProvider, using the custom scheme and scope.

        WebAuthProvider.login(account)
            .withScheme("demo")
            .withScope("openid profile email")
            // Launch the authentication passing the callback where the results will be received
            .start(this, object : Callback<Credentials, AuthenticationException> {
                // Called when there is an authentication failure
                override fun onFailure(exception: AuthenticationException) {
                    // Something went wrong!
                }

                // Called when authentication completed successfully
                override fun onSuccess(credentials: Credentials) {
                    // Get the access token from the credentials object.
                    // This can be used to call APIs
                    val accessToken = credentials.accessToken
                }
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        account = Auth0(
            "LSN9l3iMrWtRyrLgTTjbOGJIbMbkFF2i",
            "dev-y5kpzumi7ghqmuzh.us.auth0.com"
        )
        loginWithBrowser()
        setupViewPager()
        setupButtons()
    }

    private fun setupViewPager() {
        pagerAdapter = TutorialPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        // Add dots indicator
        TabLayoutMediator(binding.dotsIndicator, binding.viewPager) { tab, _ ->
            tab.setIcon(R.drawable.dot_indicator)
        }.attach()
    }

    private fun setupButtons() {
        binding.nextButton.setOnClickListener {
            if (binding.viewPager.currentItem < pagerAdapter.itemCount - 1) {
                binding.viewPager.currentItem += 1
            } else {
                startActivity(Intent(this, UserInfoActivity::class.java))
                finish()
            }
        }

        binding.skipButton.setOnClickListener {
            startActivity(Intent(this, UserInfoActivity::class.java))
            finish()
        }
    }
}