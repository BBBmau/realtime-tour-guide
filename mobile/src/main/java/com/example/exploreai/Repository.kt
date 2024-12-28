package com.example.exploreai

import com.example.exploreai.assistant.AssistantRequest
import com.example.exploreai.assistant.AssistantResponse
import com.example.exploreai.assistant.ExploreAiEphemeralResp

// Repository class to handle data operations
class Repository {
    // the fetch is for the ephemeral key
    suspend fun fetchData(): Result<ExploreAiEphemeralResp> {
        return try {
            val response = AssistantClient.apiService.getData()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("API call failed with code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // used for when user interacts with the assistant with the resp being from the assistant
    suspend fun postData(request: AssistantRequest): Result<AssistantResponse> {
        return try {
            val response = AssistantClient.apiService.postData(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("API call failed with code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
