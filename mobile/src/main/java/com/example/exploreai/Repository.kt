package com.mau.exploreai

import android.util.Log
import androidx.annotation.WorkerThread
import com.mau.exploreai.assistant.AssistantRequest
import com.mau.exploreai.assistant.AssistantResponse
import com.mau.exploreai.assistant.ExploreAiEphemeralResp
import com.mau.exploreai.assistant.Message
import com.mau.exploreai.assistant.SessionBody
import kotlinx.coroutines.flow.Flow

// Repository class to handle data operations
class Repository(private val conversationDao: ConversationDao, private val messageDao: MessageDao) {


    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allConversations: Flow<List<Conversation>> = conversationDao.getAll()

    fun getMessagesForConversation(conversationId: Int): Flow<List<ConversationMessage>> {
        return messageDao.getMessagesForConversation(conversationId)
    }

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertConversation(conversation: Conversation): Long {
        return conversationDao.insertConversation(conversation)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertMessage(message: ConversationMessage): Long {
        return messageDao.insertMessage(message)
    }

    // the fetch is for the ephemeral key
    suspend fun fetch(location: String, destination: String): ExploreAiEphemeralResp? {
        return try{
            val resp = AssistantClient.apiService.getResponse(location, destination)
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
            val response = AssistantClient.openAiService.startSession(sdp, "model=gpt-4o-mini-realtime-preview-2024-12-17")
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
