package com.mau.exploreai.webrtc.webrtc.datachannel

import android.util.Log
import com.google.gson.Gson
import com.mau.exploreai.assistant.AssistantActivityActivity
import com.mau.exploreai.webrtc.webrtc.models.Event
import com.mau.exploreai.webrtc.webrtc.models.InputAudioTranscription
import com.mau.exploreai.webrtc.webrtc.models.Session
import org.json.JSONObject
import org.webrtc.DataChannel
import java.nio.ByteBuffer

class MessageHandler(
    private val assistantActivity: AssistantActivityActivity,
    private val dataChannel: DataChannel
) {
    /**
     * Handle JSON messages received through the data channel
     */
    fun handleJsonMessage(json: JSONObject) {
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
                    assistantActivity.addNewMessage(assistantText, false)
                }
                "conversation.item.input_audio_transcription.completed" -> {
                    val assistantText = json.optString("transcript")
                    Log.d("[handleJsonMessage]", "Received conversation.item.input_audio_transcription.completed: $assistantText")
                    assistantActivity.addNewMessage(assistantText, true)
                }
                "session.created" -> {
                    sendTranscriptionUpdate()
                }
                "response.done" -> {
                    handleResponseDone(json)
                }
                else -> {
                    Log.d("[handleJsonMessage]", "Unknown message type: $json")
                }
            }
        } catch (e: Exception) {
            Log.e("[handleJsonMessage]", "Error handling JSON message: ${e.message}")
        }
    }
    
    /**
     * Handle response.done messages
     */
    private fun handleResponseDone(json: JSONObject) {
        try {
            val responseObject = json.getJSONObject("response")
            if (responseObject.getString("status") == "failed") {
                val statusDetails = responseObject.getJSONObject("status_details")
                val errorObj = statusDetails.getJSONObject("error")
                val errorMsg = errorObj.getString("message")
                Log.d("[handleJsonMessage]", "Received response.done error: $errorMsg")
                assistantActivity.addNewMessage(errorMsg, false)
            } else {
                // Handle successful response
                val outputArray = responseObject.optJSONArray("output")
                if (outputArray != null && outputArray.length() > 0) {
                    val item = outputArray.getJSONObject(0)
                    val assistantText = item.optString("text", "")
                    if (assistantText.isNotEmpty()) {
                        assistantActivity.addNewMessage(assistantText, false)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("[handleResponseDone]", "Error parsing response.done: ${e.message}")
        }
    }
    
    /**
     * Send a transcription update request
     */
    private fun sendTranscriptionUpdate() {
        val event = Event(
            event_id = "transcription_update",
            type = "session.update",
            session = Session(
                input_audio_transcription = InputAudioTranscription(model = "whisper-1")
            )
        )
        sendMessage(event)
    }
    
    /**
     * Send a message through the data channel
     */
    fun sendMessage(message: Any) {
        val jsonString = Gson().toJson(message)
        val byteBuffer = ByteBuffer.wrap(jsonString.toByteArray(Charsets.UTF_8))
        dataChannel.send(DataChannel.Buffer(byteBuffer, false))
    }
} 