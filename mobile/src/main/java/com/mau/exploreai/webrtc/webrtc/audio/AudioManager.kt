package com.mau.exploreai.webrtc.webrtc.audio

import android.content.Context
import android.media.AudioManager as AndroidAudioManager
import android.util.Log

class AudioManager(context: Context) {
    private val audioManager: AndroidAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AndroidAudioManager
    private var previousMode: Int = AndroidAudioManager.MODE_NORMAL
    
    init {
        // Store initial audio state to restore later
        previousMode = audioManager.mode
    }
    
    /**
     * Enable or disable speakerphone mode
     */
    fun enableSpeakerphone(enable: Boolean) {
        // Save the current mode to restore later if needed
        previousMode = audioManager.mode
        
        // Set audio mode for communication
        audioManager.mode = AndroidAudioManager.MODE_IN_COMMUNICATION
        
        // Enable/disable speakerphone
        audioManager.isSpeakerphoneOn = enable
        
        Log.d("WebRTC", "Speakerphone ${if(enable) "enabled" else "disabled"}")
    }
    
    /**
     * Clean up audio settings when WebRTC session is complete
     */
    fun cleanup() {
        // Reset audio settings when done
        audioManager.isSpeakerphoneOn = false
        audioManager.mode = previousMode
        Log.d("WebRTC", "Audio settings restored")
    }
    
    /**
     * Adjust the volume level
     */
    fun adjustVolume(increase: Boolean) {
        val direction = if (increase) 
            AndroidAudioManager.ADJUST_RAISE 
        else 
            AndroidAudioManager.ADJUST_LOWER
            
        audioManager.adjustStreamVolume(
            AndroidAudioManager.STREAM_VOICE_CALL,
            direction,
            AndroidAudioManager.FLAG_SHOW_UI
        )
    }
    
    /**
     * Get the current volume level
     */
    fun getCurrentVolume(): Int {
        return audioManager.getStreamVolume(AndroidAudioManager.STREAM_VOICE_CALL)
    }
} 