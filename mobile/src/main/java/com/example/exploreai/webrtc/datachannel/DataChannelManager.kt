package com.example.exploreai.webrtc.datachannel

import android.util.Log
import com.google.gson.Gson
import com.mau.exploreai.assistant.AssistantActivityActivity
import com.example.exploreai.webrtc.models.Event
import com.example.exploreai.webrtc.models.InputAudioTranscription
import com.example.exploreai.webrtc.models.Session
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.DataChannel
import org.webrtc.PeerConnection
import java.nio.ByteBuffer
import java.nio.charset.Charset

class DataChannelManager(private val assistantActivity: AssistantActivityActivity) {
    lateinit var dataChannel: DataChannel
    
    fun createDataChannel(peerConnection: PeerConnection): DataChannel {
        dataChannel = peerConnection.createDataChannel("oai-events", DataChannel.Init())!!
        dataChannel.registerObserver(createDataChannelObserver())
        return dataChannel
    }
    
    private fun createDataChannelObserver(): DataChannel.Observer {
        return object : DataChannel.Observer {
            override fun onMessage(buffer: DataChannel.Buffer) {
                handleDataChannelMessage(buffer)
            }
            
            override fun onBufferedAmountChange(amount: Long) {
                // Handle buffered amount change
            }
            
            override fun onStateChange() {
                Log.d("[dataChannelObserver]", "state change: ${dataChannel.state()}")
            }
        }
    }
    
    private fun handleDataChannelMessage(buffer: DataChannel.Buffer) {
        val data = buffer.data
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
    
    private fun handleJsonMessage(json: JSONObject) {
        // ... existing message handling code ...
    }
    
    // Method to send messages through data channel
    fun sendMessage(message: Any) {
        val jsonString = Gson().toJson(message)
        val byteBuffer = ByteBuffer.wrap(jsonString.toByteArray(Charsets.UTF_8))
        dataChannel.send(DataChannel.Buffer(byteBuffer, false))
    }
} 