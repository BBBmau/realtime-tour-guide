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
    val active: Boolean = true
)

/**
 * Audio transcription configuration
 */
data class InputAudioTranscription(
    val model: String = "whisper-1"
)

/**
 * Response from the server
 */
data class Response(
    val status: String,
    @SerializedName("status_details") val statusDetails: StatusDetails? = null,
    val text: String? = null,
    val transcript: String? = null
)

/**
 * Status details including error information
 */
data class StatusDetails(
    val error: Error? = null
)

/**
 * Error information
 */
data class Error(
    val code: String,
    val message: String
)

/**
 * Message handler for communication between components
 */
data class WebRTCMessage(
    val type: MessageType,
    val content: Any? = null
)

/**
 * Message types for internal communication
 */
enum class MessageType {
    ICE_CONNECTED,
    CONNECTION_FAILED, 
    AUDIO_TRANSCRIPTION,
    USER_TRANSCRIPT,
    ASSISTANT_TRANSCRIPT,
    ERROR
} 