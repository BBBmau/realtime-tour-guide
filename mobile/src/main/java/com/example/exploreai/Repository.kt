package com.example.exploreai

import android.companion.AssociatedDevice
import android.util.Log
import com.example.exploreai.assistant.AssistantRequest
import com.example.exploreai.assistant.AssistantResponse
import com.example.exploreai.assistant.ExploreAiEphemeralResp
import com.example.exploreai.assistant.SessionBody

// Repository class to handle data operations
class Repository {
    // the fetch is for the ephemeral key
    suspend fun fetch(): ExploreAiEphemeralResp? {
        return try{
            val resp = AssistantClient.apiService.getResponse()
            Log.d("[REPOSITORY CALL]", "$resp")
            resp
        } catch (e: Exception){
            Log.e("[Repository API call]","API call failed with code: ${e.cause}")
            return null
        }
    }

    // used for when user interacts with the assistant with the resp being from the assistant
    suspend fun startSession(sdp: String): Result<String> {
        return try {
            val response = AssistantClient.apiService.startSession(sdp)
            if (response.isSuccessful) {
                Result.success(response.body() ?: "")
            } else {
                Result.failure(Exception("Failed with code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
