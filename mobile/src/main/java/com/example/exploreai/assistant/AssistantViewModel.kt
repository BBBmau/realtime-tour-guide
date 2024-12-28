package com.example.exploreai.assistant

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exploreai.Repository
import kotlinx.coroutines.launch

class AssistantViewModel : ViewModel() {
    private val repository = Repository()
    private val _data = MutableLiveData<ExploreAiEphemeralResp>()
    val data: LiveData<ExploreAiEphemeralResp> = _data

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchData() {
        viewModelScope.launch {
            repository.fetchData()
                .onSuccess { result ->
                    _data.value = result
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
        }
    }

    fun postData(request: AssistantRequest) {
        viewModelScope.launch {
            repository.postData(request)
                .onSuccess { result ->
                    // Handle success
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
        }
    }
}