package com.example.exploreai.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.se.omapi.Session
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.exploreai.AssistantClient
import com.example.exploreai.R
import com.example.exploreai.ToggleSettingsActivity
import com.example.exploreai.databinding.ActivityAssistantBinding
import com.example.exploreai.webrtc.webRTCclient
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.nio.charset.Charset
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


lateinit var EPHEMERAL_KEY: String
val assistant = AssistantViewModel()

class AssistantActivityActivity : AppCompatActivity() {

    private lateinit var client : webRTCclient
    private lateinit var speechRecognitionManager: SpeechRecognitionManager
    private lateinit var microphoneIcon: ImageView
    private lateinit var statusText: TextView
    private lateinit var pulseAnimation: Animation
    private var isSpeaking = false
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

        client = webRTCclient(this)
        client.createPeerConnection()

        // fetches the ephemeral key
        assistant.fetch() // TODO: only works on physical device and not emulator
        //TODO: this is meant for debugging the ephemeral key, should be removed later on.
        assistant.resp.observe(this) { response ->
            EPHEMERAL_KEY = response.clientSecret.value
            Log.d("[EPHEMERAL KEY]", response.clientSecret.value)
        //TODO: we need to add the body that initializes the rtc session over voice
            assistant.createSession(client)
            assistant.sessionResp.observe(this) { resp ->
                when (resp) {
                    is ApiResult.Success -> {
                        // this is the returning SDP that we get from openai, we use for answer
                        Log.d("[startSession]", "201 SUCCESS")
                        client.setRemoteDescriptionAsync(SessionDescription(SessionDescription.Type.ANSWER, resp.data.sdp))
                    }
                    is ApiResult.Error -> {
                        // Handle error
                        Toast.makeText(this, resp.message, Toast.LENGTH_SHORT).show()
                        Log.e("[API ERROR]", resp.message)
                    }
                    else -> { Toast.makeText(this, "unknown sessionResp error", Toast.LENGTH_SHORT).show()}
                }
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
            toggleSpeakingState()
        }

        val messageList = findViewById<RecyclerView>(R.id.messageList)
        messageAdapter = MessageAdapter()

        messageList.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true  // Messages start from bottom
            }
            adapter = messageAdapter
        }

        // Example: Add some messages
        messageAdapter.addMessage(Message("Hello!", isFromUser = true))
        messageAdapter.addMessage(Message("Hi! How can I help you today?", isFromUser = false))
    }

    private fun toggleSpeakingState() {
        isSpeaking = !isSpeaking
        updateUI()
    }

    private fun updateUI() {
        if (isSpeaking) {
            client.pc.setAudioRecording(true)
            microphoneIcon.startAnimation(pulseAnimation)
            statusText.text = "Speaking..."
            microphoneIcon.setColorFilter(getColor(R.color.primary))
            statusText.setTextColor(getColor(R.color.primary))
            //TODO: have ui update in real-time while user is speaking
            speechRecognitionManager.startListening { result -> addNewMessage(result, true) }
        } else {
            client.pc.setAudioRecording(false)
            microphoneIcon.clearAnimation()
            statusText.text = "Idle"
            microphoneIcon.setColorFilter(getColor(androidx.appcompat.R.color.abc_background_cache_hint_selector_material_dark))
            statusText.setTextColor(getColor(R.color.secondary))
        }
    }

    private lateinit var binding: ActivityAssistantBinding


    // Function to add new messages
    fun addNewMessage(text: String, isFromUser: Boolean) {
        messageAdapter.addMessage(Message(text, isFromUser))
        // Scroll to bottom
        findViewById<RecyclerView>(R.id.messageList).scrollToPosition(messageAdapter.itemCount - 1)

        sendResponseCreate(client.dc, text)
    }

    private fun checkPermissionAndSetup() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("Assistant", "Assistant received permissions to listen")
                speechRecognitionManager = SpeechRecognitionManager(this)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
}