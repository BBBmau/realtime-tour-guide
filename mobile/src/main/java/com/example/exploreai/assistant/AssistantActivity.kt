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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.exploreai.R
import com.example.exploreai.ToggleSettingsActivity
import com.example.exploreai.databinding.ActivityAssistantBinding
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory


lateinit var EPHEMERAL_KEY: String

class AssistantActivityActivity : AppCompatActivity() {

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


        // TODO: AssistantActivity is getting very bloated.
        //  we need to break this up into smaller pieces
        // Initialize PeerConnectionFactory globals.
        val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(this)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)

        // Create a new PeerConnectionFactory instance.
        val options = PeerConnectionFactory.Options()
        val peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .createPeerConnectionFactory()
        val pc = createPeerConnection(peerConnectionFactory)
        // Create an AudioSource instance.
        val audioSource: AudioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        val localAudioTrack: AudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource)

        if (pc != null) {
            pc.addTrack(localAudioTrack)
        }
        // fetches the ephemeral key
        val assistant = AssistantViewModel()
        assistant.fetch() // TODO: only works on physical device and not emulator
        //TODO: this is meant for debugging the ephemeral key, should be removed later on.
        assistant.resp.observe(this) { response ->
            EPHEMERAL_KEY = response.clientSecret.value
            Log.d("[EPHEMERAL KEY]", response.clientSecret.value)
        //TODO: we need to add the body that initializes the rtc session over voice
//        assistant.startSession(SessionBody("sdpText"))
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
            microphoneIcon.startAnimation(pulseAnimation)
            statusText.text = "Speaking..."
            microphoneIcon.setColorFilter(getColor(R.color.primary))
            statusText.setTextColor(getColor(R.color.primary))
            //TODO: have ui update in real-time while user is speaking
            speechRecognitionManager.startListening { result -> addNewMessage(result, true) }
        } else {
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

fun createPeerConnection(peerConnectionFactory: PeerConnectionFactory) : PeerConnection? {
    // Configuration for the peer connection.
    val iceServers = mutableListOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
    )

    val rtcConfig = PeerConnection.RTCConfiguration(iceServers)

// Create the peer connection instance.
    val peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
        override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {
            // Handle signaling state changes
        }

        override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
            // Handle ICE connection state changes
        }

        override fun onIceConnectionReceivingChange(receiving: Boolean) {
            // Handle ICE connection receiving state changes
        }

        override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {
            // Handle ICE gathering state changes
        }

        override fun onIceCandidate(iceCandidate: IceCandidate) {
            // Handle new ICE candidates
        }

        override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {
            // Handle removed ICE candidates
        }

        override fun onAddStream(mediaStream: MediaStream) {
            // Handle added media stream
        }

        override fun onRemoveStream(mediaStream: MediaStream) {
            // Handle removed media stream
        }

        override fun onDataChannel(dataChannel: DataChannel) {
            // Handle data channel events
        }

        override fun onRenegotiationNeeded() {
            // Handle renegotiation needed events
        }
    })

    if (peerConnection == null){
        Log.e("[PEER CONNECTION ERROR]", "Could not create Peer Connection")
        return null
    }

    return peerConnection
}