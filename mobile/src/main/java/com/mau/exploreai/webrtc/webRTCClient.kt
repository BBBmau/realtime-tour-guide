package com.mau.exploreai.webrtc

import android.content.Context
import com.mau.exploreai.assistant.AssistantActivityActivity
import com.mau.exploreai.webrtc.webrtc.audio.AudioManager
import com.mau.exploreai.webrtc.webrtc.connection.PeerConnectionManager
import com.mau.exploreai.webrtc.webrtc.datachannel.DataChannelManager
import com.mau.exploreai.webrtc.webrtc.sdp.SessionDescriptionManager
import org.webrtc.DataChannel
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SessionDescription

// Main client that coordinates between components
class WebRTCClient(ctx: Context, private val activity: AssistantActivityActivity) {

    // Core components
    private val peerConnectionManager: PeerConnectionManager
    private val dataChannelManager: DataChannelManager
    private val sessionDescriptionManager: SessionDescriptionManager
    private val audioManager: AudioManager

    // Accessible properties
    val peerConnection: PeerConnection get() = peerConnectionManager.peerConnection
    val dataChannel: DataChannel get() = dataChannelManager.dataChannel
    
    init {
        // Initialize PeerConnectionFactory
        val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(ctx)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)
        
        val pcFactory = PeerConnectionFactory.builder()
            .setOptions(PeerConnectionFactory.Options())
            .createPeerConnectionFactory()
        audioManager = AudioManager(ctx)
        peerConnectionManager = PeerConnectionManager(pcFactory)
        dataChannelManager = DataChannelManager()
        sessionDescriptionManager = SessionDescriptionManager()
        
        // Connect components
        peerConnectionManager.audioManagerCallback = { enable ->
            audioManager.enableSpeakerphone(enable)
        }
    }
    
    // Public API
    fun createPeerConnection() {
        val pc = peerConnectionManager.createPeerConnection()
        dataChannelManager.createDataChannel(pc, activity)
        peerConnectionManager.setupAudioTransceiver()
    }
    
    suspend fun createOffer(): String {
        return sessionDescriptionManager.createOffer(peerConnection)
    }
    
    fun setRemoteDescription(description: SessionDescription) =
        sessionDescriptionManager.setRemoteDescription(peerConnection, description)
}