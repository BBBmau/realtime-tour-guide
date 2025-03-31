package com.mau.exploreai.webrtc.webrtc.connection

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
                    else -> {}
                }
            }

            override fun onIceConnectionReceivingChange(p0: Boolean) {
                TODO("Not yet implemented")
            }

            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
                TODO("Not yet implemented")
            }

            override fun onIceCandidate(p0: IceCandidate?) {
                TODO("Not yet implemented")
            }

            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
                TODO("Not yet implemented")
            }

            override fun onAddStream(p0: MediaStream?) {
                TODO("Not yet implemented")
            }

            override fun onRemoveStream(p0: MediaStream?) {
                TODO("Not yet implemented")
            }

            override fun onDataChannel(p0: DataChannel?) {
                TODO("Not yet implemented")
            }

            override fun onRenegotiationNeeded() {
                TODO("Not yet implemented")
            }

            // Other observer methods...
            // ... existing code ...
        }
    }
} 