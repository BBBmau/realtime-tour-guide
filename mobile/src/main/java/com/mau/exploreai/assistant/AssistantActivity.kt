package com.mau.exploreai.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mau.exploreai.R
import com.mau.exploreai.databinding.ActivityAssistantBinding
import com.mau.exploreai.AssistantApplication
import com.mau.exploreai.Conversation
import com.mau.exploreai.ConversationMessage
import com.mau.exploreai.settings.ToggleSettingsActivity
import com.mau.exploreai.webrtc.WebRTCClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.webrtc.SessionDescription
import com.mau.exploreai.utils.LocationTimeUtils
import com.mau.exploreai.utils.NotificationManager
import com.mau.exploreai.utils.PreferencesManager


lateinit var EPHEMERAL_KEY: String
 lateinit var assistant: AssistantViewModel

class AssistantActivityActivity : AppCompatActivity() {

    private var conversationID: Int = -1
    private lateinit var webRTCClient : WebRTCClient
    private  val audioPlayback = AudioPlayback()
    private lateinit var microphoneIcon: ImageView
    private lateinit var statusText: TextView
    private lateinit var pulseAnimation: Animation
    private var inSession = false
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var dest: String
    private lateinit var loc: String
    private lateinit var notificationManager: NotificationManager

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
        assistant = ViewModelProvider(this,
            AssistantViewModelFactory((application as AssistantApplication).repository)
        )[AssistantViewModel::class.java]

        webRTCClient = WebRTCClient(this, this)
        //TODO: we shouldn't need to call this twice
        LocationTimeUtils.getCurrentDateTimeLocation(this) { _, _, location ->
            loc = location

            dest = PreferencesManager(this).getLocation()!!
            // TODO: ephemeral key fetch should only be requested when expired.
            assistant.fetch(loc, dest) // TODO: only works on physical device and not emulator
            assistant.resp.observe(this) { response ->
                EPHEMERAL_KEY = response.clientSecret.value
            }
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
            messageAdapter.clearConversation() // clear the previous conversation
            Log.d("[toggleSession]", "Session in Progress")
            startRealtimeSession()
            microphoneIcon.startAnimation(pulseAnimation)
            statusText.text = "In conversation..."
            microphoneIcon.setColorFilter(getColor(R.color.primary))
            statusText.setTextColor(getColor(R.color.primary))
            //TODO: have ui update in real-time while user is speaking
        } else {
            addConversationToDatabase()
            webRTCClient.peerConnection.close()
            microphoneIcon.clearAnimation()
            statusText.text = "Idle"
            microphoneIcon.setColorFilter(getColor(androidx.appcompat.R.color.abc_background_cache_hint_selector_material_dark))
            statusText.setTextColor(getColor(R.color.secondary))
        }
    }

    private fun addConversationToDatabase(){
        if (messageAdapter.itemCount != 0){
            Log.d("[addConversationToDatabase]", "Adding conversation to database")
        LocationTimeUtils.getCurrentDateTimeLocation(this) { date, time, location ->
            Log.d("[toggleSession]", "Location: $location")
            val newConversation = Conversation(
                date = date,
                time = time,
                start = location,
                destination = PreferencesManager(this).getDestination()
            )
            PreferencesManager(this).setLocation(location)
            lifecycleScope.launch {
                // Insert conversation in a background thread
                withContext(Dispatchers.IO) {
                    // Insert the conversation and get the ID
                    conversationID = assistant.insertConversation(newConversation).toInt()
                    addMessagesToConversation()
                    Log.d("[toggleSession]", "newConversation inserted with ID: $conversationID")
                }
            }
        }
    }
    }

    private fun addMessagesToConversation(){
        if (messageAdapter.itemCount != 0 && conversationID > 0){  // Check that conversationID is valid
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    messageAdapter.getAllMessages().forEach{ msg -> 
                        assistant.insertMessage(
                            ConversationMessage(
                            conversationId = conversationID.toLong(),
                            time = "${msg.timestamp}", 
                            isFromUser = msg.isFromUser,
                            text = msg.text
                        )
                        )
                    }
                }
            }
        } else {
            Log.w("[addMessagesToConversation]", "No messages to add or invalid conversationID: $conversationID")
        }
    }

    private lateinit var binding: ActivityAssistantBinding

    private fun startRealtimeSession() {
        lifecycleScope.launch {
            try {
                // Step 1: Create Peer Connection
                webRTCClient.createPeerConnection()

                // Step 3: Observe the session response and handle it
                when (val sessionResult = assistant.createSession(webRTCClient)) {
                    is ApiResult.Success -> {
                        Log.d("[startSession]", "201 SUCCESS")
                        webRTCClient.setRemoteDescription(
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
            sendResponseCreate(webRTCClient.dataChannel, text)
        }
    }

    private fun checkPermissionAndSetup() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("Assistant", "Assistant received permissions to listen")
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("Assistant", "Assistant received permissions for location")
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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