package com.example.exploreai.assistant
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.Response
import retrofit2.http.Query

interface ExploreAiAssistantService {

    // requests OpenAi Ephemeral Key from GCP Server after it receives it from OpenAI directly
    @GET("session")
    suspend fun getResponse(): ExploreAiEphemeralResp

    @POST("realtime?model=gpt-4o-realtime-preview-2024-12-17")
    suspend fun startSession(
        @Body data: String,
        @Query("model") model: String,
    ): Response<String>

    @POST("sessions")
    suspend fun sendUserResponse(@Body data: AssistantRequest): Response<AssistantResponse>
}

// Data classes for your API responses
data class ExploreAiEphemeralResp(
    @SerializedName("client_secret") val clientSecret: ClientSecret
)

data class ClientSecret(
    @SerializedName("value") val value: String
)

data class SessionBody(
    val type: String,
    val sdp: String
)

data class AssistantRequest(
    val type: String,
    val response: UserRequest
    // ... other fields
)

data class UserRequest(
    val modalities: Array<String>,
    val instructions: String
)

data class AssistantResponse(
    val responseField: String
    // ... other fields
)