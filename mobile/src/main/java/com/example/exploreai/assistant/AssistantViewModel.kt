package com.example.exploreai.assistant

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exploreai.Repository
import com.example.exploreai.webrtc.webRTCclient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import com.google.gson.Gson
import java.nio.ByteBuffer
import org.webrtc.DataChannel
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

class AssistantViewModel : ViewModel() {
    private val repository = Repository()
    private val _data = MutableLiveData<ExploreAiEphemeralResp>()
    val resp: LiveData<ExploreAiEphemeralResp> = _data
    private val _sdpResponse = MutableLiveData<ApiResult<SessionBody>>()
    val sessionResp : LiveData<ApiResult<SessionBody>> = _sdpResponse

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetch() {
        viewModelScope.launch {
            _data.value = repository.fetch()
        }
    }


    fun createSession(client: webRTCclient){
        viewModelScope.launch {
            try {
                client.createOffer(client.pc) // sets the localDescription internally
                val result = repository.startSession(client.sanitizedSDP.description)
                result.onSuccess { responseSdp ->
                    // Handle the SDP response
                    Log.d("SDP", "Received SDP: $responseSdp")
                    val successResult: ApiResult<SessionBody> = ApiResult.Success(
                        SessionBody(
                            sdp = responseSdp
                        )
                    )
                    _sdpResponse.value = successResult
                }.onFailure { error ->
                    Log.e("SDP", "Error: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e("SDP", "Exception: ${e.message}")
            }
        }
    }
}

// Define your data model (same as above)
data class ResponseCreate(
    val type: String,
    val response: Response
)

data class Response(
    val modalities: List<String>,
    val instructions: String
)

// Function to send the data
fun sendResponseCreate(dataChannel: DataChannel, msg : String) {
    // Create the object
    val responseCreate = ResponseCreate(
        type = "response.create",
        response = Response(
            modalities = listOf("text"),
            instructions = msg
        )
    )

    // Serialize the object to JSON using Gson
    val gson = Gson()
    val jsonString = gson.toJson(responseCreate)

    // Convert JSON string to ByteBuffer
    val buffer = ByteBuffer.wrap(jsonString.toByteArray(Charsets.UTF_8))

    // Create DataChannel.Buffer and send it
    val dataBuffer = DataChannel.Buffer(buffer, false) // 'false' because it's text-based
    Log.d("[sendResponseCreate]","sending buffer to dataChannel: ${dataBuffer.data}")
    dataChannel.send(dataBuffer)
}