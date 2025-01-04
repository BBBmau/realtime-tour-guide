package com.example.exploreai.assistant

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
    private val _sdpResponse = MutableLiveData<ApiResult<SessionResponse>>()
    val sessionResp : LiveData<ApiResult<SessionResponse>> = _sdpResponse

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetch() {
        viewModelScope.launch {
            _data.value = repository.fetch()
        }
    }

    fun startSession(request: SessionBody){
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    repository.startSession(request)
                }

                result.fold(
                    onSuccess = { response ->
                        _sdpResponse.value = ApiResult.Success(response)
                    },
                    onFailure = { exception ->
                        _sdpResponse.value = ApiResult.Error(
                            exception.message ?: "Unknown error occurred"
                        )
                    }
                )
            }catch (e: Exception){
                _sdpResponse.value = ApiResult.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}