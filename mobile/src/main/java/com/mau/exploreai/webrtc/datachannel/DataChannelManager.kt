package com.mau.exploreai.webrtc.webrtc.datachannel

import android.util.Log
import com.mau.exploreai.assistant.AssistantActivityActivity
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.DataChannel
import org.webrtc.PeerConnection
import java.nio.charset.Charset

class DataChannelManager{
    lateinit var dataChannel: DataChannel
    lateinit var messageHandler: MessageHandler
    
    fun createDataChannel(peerConnection: PeerConnection, activity: AssistantActivityActivity): DataChannel {
        dataChannel = peerConnection.createDataChannel("oai-events", DataChannel.Init())!!
        dataChannel.registerObserver(createDataChannelObserver())
        messageHandler = MessageHandler(activity, dataChannel)
        return dataChannel
    }
    
    private fun createDataChannelObserver(): DataChannel.Observer {
        return object : DataChannel.Observer {
            override fun onMessage(buffer: DataChannel.Buffer) {
                handleDataChannelMessage(buffer)
            }
            
            override fun onBufferedAmountChange(amount: Long) {}
            
            override fun onStateChange() { Log.d("[dataChannelObserver]", "state change: ${dataChannel.state()}") }
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
            messageHandler.handleJsonMessage(jsonObject)
        } catch (e: JSONException) { Log.e("DataChannel", "Failed to parse JSON: ${e.message}") }
    }
} 