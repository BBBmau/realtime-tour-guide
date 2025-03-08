package com.example.exploreai

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.example.exploreai.assistant.AssistantActivityActivity
import com.example.exploreai.databinding.ActivityUserInfoBinding
import com.example.exploreai.utils.TokenManager

class UserInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserInfoBinding
    private lateinit var account: Auth0
    private fun loginWithBrowser() {
        // Setup the WebAuthProvider, using the custom scheme and scope.

        WebAuthProvider.login(account)
            .withScheme("demo")
            .withScope("openid profile email")
            // Launch the authentication passing the callback where the results will be received
            .start(this, object : Callback<Credentials, AuthenticationException> {
                // Called when there is an authentication failure
                override fun onFailure(error: AuthenticationException) {
                    // Something went wrong!
                    Log.d("[auth0]","failure! - ${error}")

                }

                // Called when authentication completed successfully
                override fun onSuccess(result: Credentials) {
                    // Get the access token from the credentials object.
                    // This can be used to call APIs
                    startActivity(Intent(applicationContext, AssistantActivityActivity::class.java))
                    TokenManager.saveToken(applicationContext, result)
                    Toast.makeText(applicationContext,"success!", Toast.LENGTH_SHORT).show()
                    finish() // Optional: prevents going back
                }
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        account = Auth0(
            "LSN9l3iMrWtRyrLgTTjbOGJIbMbkFF2i",
            "dev-y5kpzumi7ghqmuzh.us.auth0.com"
        )
        setContentView(binding.root)

        binding.continueButton.setOnClickListener {
            // Optional: Validate inputs before proceeding
            loginWithBrowser()
        }
    }

}