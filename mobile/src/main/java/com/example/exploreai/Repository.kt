package com.example.exploreai

import android.companion.AssociatedDevice
import android.util.Log
import com.example.exploreai.assistant.AssistantRequest
import com.example.exploreai.assistant.AssistantResponse
import com.example.exploreai.assistant.ExploreAiEphemeralResp
import com.example.exploreai.assistant.SessionBody
import com.example.exploreai.assistant.SessionResponse

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
    suspend fun startSession(request: SessionBody): Result<SessionResponse> {
        return try {
            val response = AssistantClient.openAiService.startSession(request)
            if (response.isSuccessful) {
                Log.d("[startSession]", "sessionResp received: ${response.body()}")
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("API call failed with code: ${response.code()};\nsdp used: ${request.sdp}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
