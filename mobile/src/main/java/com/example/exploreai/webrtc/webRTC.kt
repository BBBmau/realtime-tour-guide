package com.example.exploreai.webrtc

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import org.json.JSONException
import org.json.JSONObject
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
import java.nio.charset.Charset
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class webRTCclient {
    lateinit var pc: PeerConnection
    lateinit var dc: DataChannel
    private var pcf: PeerConnectionFactory

    // Primary constructor with no arguments
    constructor(ctx: Context) {
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
                        dc = pc.createDataChannel("oai-events", DataChannel.Init())!!
                        dc.registerObserver(dataChannelObserver)
                        Log.d("[dataChannel]", "state: ${dc.state()}")
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

        pc.addTransceiver(
            MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO,
            RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.SEND_ONLY)
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

                        sdp = sdp.replace("a=recvonly", "a=sendonly")
                            .replace("a=sendrecv", "a=sendonly")
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
}