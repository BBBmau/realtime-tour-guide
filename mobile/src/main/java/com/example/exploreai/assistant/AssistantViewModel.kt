package com.example.exploreai.assistant

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exploreai.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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


    fun startSession(sdp: String){
        viewModelScope.launch {
            try {
                val result = repository.startSession(sdp)
                result.onSuccess { responseSdp ->
                    // Handle the SDP response
                    Log.d("SDP", "Received SDP: $responseSdp")
                }.onFailure { error ->
                    Log.e("SDP", "Error: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e("SDP", "Exception: ${e.message}")
            }
        }
    }
}