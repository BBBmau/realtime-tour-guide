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
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
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

    private lateinit var speechRecognitionManager: SpeechRecognitionManager
    private lateinit var microphoneIcon: ImageView
    private lateinit var statusText: TextView
    lateinit var dc : DataChannel
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
        // fetches the ephemeral key
        assistant.fetch() // TODO: only works on physical device and not emulator
        //TODO: this is meant for debugging the ephemeral key, should be removed later on.
        assistant.resp.observe(this) { response ->
            EPHEMERAL_KEY = response.clientSecret.value
            Log.d("[EPHEMERAL KEY]", response.clientSecret.value)
        //TODO: we need to add the body that initializes the rtc session over voice
            assistant.createSession(pc!!)
            assistant.sessionResp.observe(this) { resp ->
                when (resp) {
                    is ApiResult.Success -> {
                        // this is the returning SDP that we get from openai, we use for answer
                        Log.d("[startSession]", "201 SUCCESS")
                        setRemoteDescriptionAsync(pc, SessionDescription(SessionDescription.Type.ANSWER, resp.data.sdp))
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

        dc = pc?.createDataChannel("oai-events", DataChannel.Init())!!
        dc.registerObserver(dataChannelObserver)

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

        sendResponseCreate(dc, text)
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

lateinit var sanitizedSDP : SessionDescription
//TODO: move this to its own file
suspend fun createOffer(peerConnection: PeerConnection) = coroutineScope {
    suspendCoroutine<String> { continuation ->
        val offerObserver = object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                try {
                    var sdp = sessionDescription.description

                    // Sanitize SDP
                    sdp = sdp.replace("a=sendrecv", "a=sendonly")
                        .replace("a=setup:active", "a=setup:actpass")

                    sanitizedSDP = SessionDescription(
                        SessionDescription.Type.OFFER,
                        sdp
                    )

                    Log.d("[createOffer]", "Setting local description with SDP: ${sanitizedSDP.description}")

                    // Wait for setLocalDescription to complete
                    setLocalDescriptionAsync(peerConnection, sanitizedSDP).invokeOnCompletion { throwable ->
                        if (throwable != null) {
                            continuation.resumeWithException(throwable)
                        } else {
                            // Only continue when local description is set
                            peerConnection.localDescription?.let { desc ->
                                continuation.resume(desc.description)
                            } ?: continuation.resumeWithException(Exception("Local description is null"))
                        }
                    }
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }

            override fun onCreateFailure(error: String) {
                continuation.resumeWithException(Exception("Failed to create offer: $error"))
            }

            override fun onSetSuccess() {
                Log.d("[createOffer]", "SUCCESS: set local description")
            }
            override fun onSetFailure(error: String) {
                Log.d("[createOffer]", error)
            }
        }

        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
        }

        try {
            peerConnection.createOffer(offerObserver, constraints)
            Log.d("[createOffer]","signaling state now: ${peerConnection.signalingState()}")
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
}

private fun setLocalDescriptionAsync(
    peerConnection: PeerConnection,
    description: SessionDescription
) = CompletableDeferred<Unit>().apply {
    peerConnection.setLocalDescription(object : SdpObserver {
        override fun onSetSuccess() {
            Log.d("[setLocalDescription]", "Set local description success")
            complete(Unit)
        }

        override fun onSetFailure(error: String) {
            Log.e("[setLocalDescription]", "Set local description failed: $error")
            completeExceptionally(Exception("Failed to set local description: $error"))
        }

        override fun onCreateSuccess(p0: SessionDescription?) {}
        override fun onCreateFailure(p0: String?) {}
    }, description)
}

private fun setRemoteDescriptionAsync(
    peerConnection: PeerConnection,
    description: SessionDescription
) = CompletableDeferred<Unit>().apply {
        peerConnection.setRemoteDescription( object : SdpObserver {
        override fun onCreateSuccess(sessionDescription: SessionDescription) {
        }

        override fun onSetSuccess() {
            Log.d("[setRemoteDescription]", "Set remote description success")
            Log.d("[setRemoteDescription]", "remoteDescription: ${peerConnection.remoteDescription.description}")
            complete(Unit)
        }
        override fun onCreateFailure(error: String) {
            Log.e("[createRemoteDescription]", "Unable to set remote description: $error")
        }
        override fun onSetFailure(error: String) {
            Log.e("[createRemoteDescription]", error)
        }
    }, description)
}

private fun onDataChannelMessage(buffer: DataChannel.Buffer) {
    val data = buffer.data
        // Handle text/JSON data
        val jsonString = String(
            ByteArray(data.remaining()).apply { data.get(this) },
            Charset.forName("UTF-8")
        )
        try {
            val jsonObject = JSONObject(jsonString)
            handleJsonMessage(jsonObject)
        } catch (e: JSONException) {
            Log.e("DataChannel", "Failed to parse JSON: ${e.message}")
        }
    }

// Example handler for JSON messages
private fun handleJsonMessage(json: JSONObject) {
    try {
        when (json.optString("type")) {
            "message" -> {
                val content = json.optString("content")
                Log.d("DataChannel", "Received message: $content")
            }
            "command" -> {
                val command = json.optString("command")
                Log.d("DataChannel", "Received command: $command")
            }
            else -> {
                Log.d("DataChannel", "Unknown message type: ${json.toString()}")
            }
        }
    } catch (e: Exception) {
        Log.e("DataChannel", "Error handling JSON message: ${e.message}")
    }
}

// Usage in DataChannel.Observer
val dataChannelObserver = object : DataChannel.Observer {
    override fun onMessage(buffer: DataChannel.Buffer) {
        onDataChannelMessage(buffer)
    }

    override fun onBufferedAmountChange(amount: Long) {
        // Handle buffered amount change
    }

    override fun onStateChange() {
        // Handle state change
        Log.d("[dataChannelObserver]", "state change")
    }
}