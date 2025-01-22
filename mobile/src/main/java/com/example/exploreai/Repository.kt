package com.example.exploreai

import android.companion.AssociatedDevice
import android.util.Log
import androidx.annotation.WorkerThread
import com.example.exploreai.assistant.AssistantRequest
import com.example.exploreai.assistant.AssistantResponse
import com.example.exploreai.assistant.ExploreAiEphemeralResp
import com.example.exploreai.assistant.Message
import com.example.exploreai.assistant.SessionBody
import kotlinx.coroutines.flow.Flow

// Repository class to handle data operations
class Repository(private val conversationDao: ConversationDao, private val messageDao: MessageDao) {


    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allConversations: Flow<List<Conversation>> = conversationDao.getAll()
    val allMessages: Flow<List<ConversationMessage>> = messageDao.getAll()

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertConversation(conversation: Conversation) {
        conversationDao.insertConversation(conversation)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertMessage(message: ConversationMessage) {
        messageDao.insertMessage(message)
    }

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
            val response = AssistantClient.openAiService.startSession(sdp, "model=gpt-4o-realtime-preview-2024-12-17")
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
