package com.mau.exploreai.webrtc.webrtc.models

import com.google.gson.annotations.SerializedName

/**
 * Event sent through the data channel
 */
data class Event(
    @SerializedName("event_id") val event_id: String,
    val type: String,
    val session: Session? = null
)

/**
 * Session details for WebRTC
 */
data class Session(
    @SerializedName("input_audio_transcription") val input_audio_transcription: InputAudioTranscription? = null,
)

/**
 * Audio transcription configuration
 */
data class InputAudioTranscription(
    val model: String = "whisper-1"
)
