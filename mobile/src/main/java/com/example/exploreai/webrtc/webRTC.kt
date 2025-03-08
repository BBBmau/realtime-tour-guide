package com.example.exploreai.webrtc

import android.content.Context
import android.media.AudioManager
import android.util.Log
import com.example.exploreai.assistant.AssistantActivityActivity
import com.google.gson.Gson
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import org.json.JSONException
import org.json.JSONObject
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
import java.nio.ByteBuffer
import java.nio.charset.Charset
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class webRTCclient {
    lateinit var pc: PeerConnection
    lateinit var dc: DataChannel
    private var pcf: PeerConnectionFactory
    private lateinit var audioManager: AudioManager

    lateinit var assistantActivity: AssistantActivityActivity

    // Primary constructor with no arguments
    constructor(ctx: Context, activity: AssistantActivityActivity) {
        assistantActivity = activity
        audioManager = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Initialize PeerConnectionFactory globals.
        val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(ctx)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)

        // Create a new PeerConnectionFactory instance.
        val options = PeerConnectionFactory.Options()
        pcf = PeerConnectionFactory.builder()
            .setOptions(options)
            .createPeerConnectionFactory()
    }


    fun createPeerConnection() {
        // Configuration for the peer connection.
        val iceServers = mutableListOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
// Create the peer connection instance.
        pc = pcf.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onTrack(transceiver: RtpTransceiver?) {
                val receiver = transceiver?.receiver
                val track = receiver?.track()
                Log.d("[onAudioTrack]", "Received audio track: ${track?.id()}")
            }


            override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {
                Log.d("WebRTC", "Signaling state change: $signalingState")
            }

            override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
                Log.d("WebRTC", "ICE connection state change: $iceConnectionState")
                when (iceConnectionState) {
                    PeerConnection.IceConnectionState.CONNECTED -> {
                        // Connection established
                        Log.d("WebRTC", "ICE Connected!")
                        Log.d("[peerConnection]","connection state: ${pc.connectionState()}")
                        Log.d("[dataChannel]", "state: ${dc.state()}")
                        
                        // Enable speakerphone when connection is established
                        enableSpeakerphone(true)
                    }
                    PeerConnection.IceConnectionState.FAILED -> {
                        // Connection failed
                        Log.e("WebRTC", "ICE Connection failed")
                    }
                    PeerConnection.IceConnectionState.COMPLETED ->
                        Log.d("WebRTC", "ICE Connection Complete!")
                    else -> {Log.e("WebRTC", "ICE connection unknown state: ${iceConnectionState.name}")}
                }
            }

            override fun onIceConnectionReceivingChange(receiving: Boolean) {
                Log.d("WebRTC", "ICE connection receiving change: $receiving")
            }

            override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {
                Log.d("WebRTC", "ICE gathering state: $iceGatheringState")
                when (iceGatheringState) {
                    PeerConnection.IceGatheringState.COMPLETE -> {
                        // ICE gathering is complete
                        Log.d("WebRTC", "ICE gathering complete")
                    }
                    else -> {
                        Log.e("WebRTC", "ICE gathering error")
                    }
                }
            }

            override fun onIceCandidate(iceCandidate: IceCandidate) {
                // Important: Add the ICE candidate to the peer connection
                Log.d("WebRTC", "New ICE candidate: ${iceCandidate.sdp}")
                pc.addIceCandidate(iceCandidate)
            }

            override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {
                // Remove the ICE candidates
                iceCandidates.forEach { candidate ->
                    pc.removeIceCandidates(arrayOf(candidate))
                }
            }

            override fun onDataChannel(dataChannel: DataChannel) {
                Log.d("WebRTC", "Data channel received: ${dataChannel.label()}")
                // Store the data channel reference if needed
                dataChannel.registerObserver(object : DataChannel.Observer {
                    override fun onBufferedAmountChange(amount: Long) {}

                    override fun onStateChange() {
                        Log.d("WebRTC", "Data channel state: ${dataChannel.state()}")
                    }

                    override fun onMessage(buffer: DataChannel.Buffer) {
                        // Handle incoming messages
                    }
                })
            }

            override fun onRenegotiationNeeded() {
                Log.d("WebRTC", "Renegotiation needed")
                // Usually you'd create a new offer here if needed
            }

            // These are deprecated but still required
            override fun onAddStream(mediaStream: MediaStream) {}
            override fun onRemoveStream(mediaStream: MediaStream) {}
        })!!

        if (pc == null){
            Log.e("[PEER CONNECTION ERROR]", "Could not create Peer Connection")
        }

        dc = pc.createDataChannel("oai-events", DataChannel.Init())!!
        dc.registerObserver(dataChannelObserver)

        pc.addTransceiver(
            MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO,
            RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.SEND_RECV)
        )
    }

    lateinit var sanitizedSDP : SessionDescription
    //TODO: move this to its own file
    suspend fun createOffer(peerConnection: PeerConnection) = coroutineScope {
        suspendCoroutine<String> { continuation ->
            val offerObserver = object : SdpObserver {
                override fun onCreateSuccess(sessionDescription: SessionDescription) {
                    try {
                        var sdp = sessionDescription.description

                        sdp = sdp.replace("a=setup:active", "a=setup:actpass")

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
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
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

    fun setRemoteDescriptionAsync(
        description: SessionDescription
    ) = CompletableDeferred<Unit>().apply {
        pc.setRemoteDescription( object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
            }

            override fun onSetSuccess() {
                Log.d("[setRemoteDescription]", "Set remote description success")
                Log.d("[setRemoteDescription]", "remoteDescription: ${pc.remoteDescription.description}")
                Log.d("[peerConnection]","current connection state: ${pc.connectionState()}")
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
                "response.created" -> {
                    Log.d("[handleJsonMessage]", "Received response.created: ${json.optString("response")}")
                }
                "response.audio.delta" -> {
                    Log.d("[handleJsonMessage]", "Received response.audio.delta: ${json.optString("response")}")
                }
                "response.audio_transcript.done" -> {
                    val assistantText = json.optString("transcript")
                    Log.d("[handleJsonMessage]", "Received response.audio_transcript.done: $assistantText")
                    assistantActivity.addNewMessage(assistantText,false)
                }
                "conversation.item.input_audio_transcription.completed" -> {
                    val assistantText = json.optString("transcript")
                    Log.d("[handleJsonMessage]", "Received conversation.item.input_audio_transcription.completed: $assistantText")
                    assistantActivity.addNewMessage(assistantText,true)
                }
                "session.created" -> {
                    val event = Event(
                        event_id = "transcription_update",
                        type = "session.update",
                        session = Session(
                            input_audio_transcription = InputAudioTranscription(model = "whisper-1")
                        )
                    )
                    val eventJson = Gson().toJson(event)

// Serialize to JSON string
                    val byteBuffer = ByteBuffer.wrap(eventJson.toByteArray(Charsets.UTF_8))
                    dc.send(DataChannel.Buffer(byteBuffer, false))
                }
                "response.done" -> {
                    val responseObject = json.getJSONObject("response")
                    if (responseObject.getString("status") == "failed"){
                        val statusDetails = responseObject.getJSONObject("status_details")
                        val errorObj = statusDetails.getJSONObject("error")
                        val errorMsg = errorObj.getString("message")
                        Log.d("[handleJsonMessage]", "Received response.done text: $errorMsg")
                        assistantActivity.addNewMessage(errorMsg,false)
                    }

// Access the first item in the array as a JSONObject
//                    val item = outputArray.getJSONObject(0)
//
//// Extract the "test" field from the JSONObject
//                    val assistantText = item.getString("text")
                }
                else -> {
                    Log.d("[handleJsonMessage]", "Unknown message type: $json")
                }
            }
        } catch (e: Exception) {
            Log.e("[handleJsonMessage]", "Error handling JSON message: ${e.message}")
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

    fun enableSpeakerphone(enable: Boolean) {
        // Save the current mode to restore later if needed
        val previousMode = audioManager.mode
        
        // Set audio mode for communication
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        
        // Enable/disable speakerphone
        audioManager.isSpeakerphoneOn = enable
        
        Log.d("WebRTC", "Speakerphone ${if(enable) "enabled" else "disabled"}")
    }

    fun cleanupAudio() {
        // Reset audio settings when done
        audioManager.isSpeakerphoneOn = false
        audioManager.mode = AudioManager.MODE_NORMAL
    }
}

data class Event(
    val event_id: String,
    val type: String,
    val session: Session
)

data class Session(
    val input_audio_transcription: InputAudioTranscription
)

data class InputAudioTranscription(val model: String)
