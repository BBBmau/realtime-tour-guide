package com.example.exploreai.webrtc.connection

import android.util.Log
import org.webrtc.*

class PeerConnectionManager(private val peerConnectionFactory: PeerConnectionFactory) {
    lateinit var peerConnection: PeerConnection
    var audioManagerCallback: ((Boolean) -> Unit)? = null
    
    fun createPeerConnection(): PeerConnection {
        val iceServers = mutableListOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )
        
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, createObserver())!!
        
        if (peerConnection == null){
            Log.e("[PEER CONNECTION ERROR]", "Could not create Peer Connection")
        }
        
        return peerConnection
    }
    
    fun setupAudioTransceiver() {
        peerConnection.addTransceiver(
            MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO,
            RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.SEND_RECV)
        )
    }
    
    private fun createObserver(): PeerConnection.Observer {
        return object : PeerConnection.Observer {
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
                        Log.d("WebRTC", "ICE Connected!")
                        Log.d("[peerConnection]","connection state: ${peerConnection.connectionState()}")
                        
                        // Enable speakerphone when connection is established
                        audioManagerCallback?.invoke(true)
                    }
                    // ... other states ...
                }
            }
            
            // Other observer methods...
            // ... existing code ...
        }
    }
} 