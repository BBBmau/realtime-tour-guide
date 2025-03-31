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
            
            override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {Log.d("WebRTC", "Signaling state change: $signalingState")}

            
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

            override fun onIceConnectionReceivingChange(p0: Boolean) {}

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
                peerConnection.addIceCandidate(iceCandidate)
            }

            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
                TODO("Not yet implemented")
            }

            // These are deprecated but still required
            override fun onAddStream(mediaStream: MediaStream) {}
            override fun onRemoveStream(mediaStream: MediaStream) {}

            override fun onDataChannel(p0: DataChannel?) {
                TODO("Not yet implemented")
            }

            override fun onRenegotiationNeeded() {
                Log.d("WebRTC", "Renegotiation needed")
                // Usually you'd create a new offer here if needed
            }
        }
    }
} 