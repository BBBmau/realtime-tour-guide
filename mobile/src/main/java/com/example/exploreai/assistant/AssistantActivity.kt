package com.example.exploreai.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.exploreai.R
import com.example.exploreai.ToggleSettingsActivity
import com.example.exploreai.databinding.ActivityAssistantBinding
import com.example.exploreai.webrtc.webRTCclient
import kotlinx.coroutines.launch
import org.webrtc.SessionDescription


lateinit var EPHEMERAL_KEY: String
val assistant = AssistantViewModel()

class AssistantActivityActivity : AppCompatActivity() {

    private lateinit var client : webRTCclient
    private  val audioPlayback = AudioPlayback()
    private lateinit var speechRecognitionManager: SpeechRecognitionManager
    private lateinit var microphoneIcon: ImageView
    private lateinit var statusText: TextView
    private lateinit var pulseAnimation: Animation
    private var inSession = false
    private lateinit var messageAdapter: MessageAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            checkPermissionAndSetup()
        } else {
            // Handle permission denied
            Toast.makeText(this, "Permission needed for speech recognition", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        client = webRTCclient(this, this)

        // TODO: ephemeral key fetch should only be requested when expired.
        assistant.fetch() // TODO: only works on physical device and not emulator
        assistant.resp.observe(this) { response ->
            EPHEMERAL_KEY = response.clientSecret.value
        }

        checkPermissionAndSetup()

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
            toggleSession()
        }

        val messageList = findViewById<RecyclerView>(R.id.messageList)
        messageAdapter = MessageAdapter()
        messageList.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true  // Messages start from bottom
            }
            adapter = messageAdapter
        }

    }

    private fun toggleSession() {
        inSession = !inSession
        if (inSession) {
            startRealtimeSession()
            microphoneIcon.startAnimation(pulseAnimation)
            statusText.text = "In conversation..."
            microphoneIcon.setColorFilter(getColor(R.color.primary))
            statusText.setTextColor(getColor(R.color.primary))
            //TODO: have ui update in real-time while user is speaking
        } else {
            client.pc.close()
            microphoneIcon.clearAnimation()
            statusText.text = "Idle"
            microphoneIcon.setColorFilter(getColor(androidx.appcompat.R.color.abc_background_cache_hint_selector_material_dark))
            statusText.setTextColor(getColor(R.color.secondary))
        }
    }

    private lateinit var binding: ActivityAssistantBinding

    private fun startRealtimeSession() {
        lifecycleScope.launch {
            try {
                // Step 1: Create Peer Connection
                client.createPeerConnection()

                // Step 3: Observe the session response and handle it
                when (val sessionResult = assistant.createSession(client)) {
                    is ApiResult.Success -> {
                        Log.d("[startSession]", "201 SUCCESS")
                        client.setRemoteDescriptionAsync(
                            SessionDescription(SessionDescription.Type.ANSWER, sessionResult.data.sdp)
                        )
                    }
                    is ApiResult.Error -> {
                        Toast.makeText(applicationContext, sessionResult.message, Toast.LENGTH_SHORT).show()
                        Log.e("[API ERROR]", sessionResult.message)
                    }
                    else -> {
                        Toast.makeText(applicationContext, "Unknown sessionResp error", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                // Handle any exceptions that occur during the process
                Log.e("[startSession]", "Error: ${e.message}")
            }
        }
    }

    // Function to add new messages
    fun addNewMessage(text: String, isFromUser: Boolean) {
        runOnUiThread {
            messageAdapter.addMessage(Message(text, isFromUser))
            // Scroll to bottom
            findViewById<RecyclerView>(R.id.messageList).scrollToPosition(messageAdapter.itemCount - 1)
        }
        if (isFromUser){
            sendResponseCreate(client.dc, text)
        }
    }

    private fun checkPermissionAndSetup() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("Assistant", "Assistant received permissions to listen")
                speechRecognitionManager = SpeechRecognitionManager(this)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    when{
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
        ) == PackageManager.PERMISSION_GRANTED -> {
            Log.d("Assistant", "Assistant received permissions to speak")
        }
        else -> {
            requestPermissionLauncher.launch(Manifest.permission.MODIFY_AUDIO_SETTINGS)
        }
    }
        when{
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("Assistant", "Assistant received permissions to speak")
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
            }
        }

    }
}