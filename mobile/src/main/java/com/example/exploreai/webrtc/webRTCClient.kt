package com.mau.exploreai.webrtc

import android.content.Context
import com.mau.exploreai.assistant.AssistantActivityActivity
import com.example.exploreai.webrtc.audio.AudioManager
import com.example.exploreai.webrtc.connection.PeerConnectionManager
import com.example.exploreai.webrtc.datachannel.DataChannelManager
import com.example.exploreai.webrtc.sdp.SessionDescriptionManager
import org.webrtc.DataChannel
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SessionDescription

// Main client that coordinates between components
class WebRTCClient(ctx: Context, activity: AssistantActivityActivity) {
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
        dataChannelManager = DataChannelManager(activity)
        sessionDescriptionManager = SessionDescriptionManager()
        
        // Connect components
        peerConnectionManager.audioManagerCallback = { enable ->
            audioManager.enableSpeakerphone(enable)
        }
    }
    
    // Public API
    fun createPeerConnection() {
        val pc = peerConnectionManager.createPeerConnection()
        val dc = dataChannelManager.createDataChannel(pc)
        peerConnectionManager.setupAudioTransceiver()
    }
    
    suspend fun createOffer(): String {
        return sessionDescriptionManager.createOffer(peerConnection)
    }
    
    fun setRemoteDescription(description: SessionDescription) =
        sessionDescriptionManager.setRemoteDescription(peerConnection, description)
    
    fun cleanupAudio() {
        audioManager.cleanup()
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