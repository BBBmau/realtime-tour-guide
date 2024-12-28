package com.example.exploreai.assistant
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.Response
interface ExploreAiAssistantService {

    // requests OpenAi Ephemeral Key from GCP Server after it receives it from OpenAI directly
    @GET("session")
    suspend fun getResponse(): ExploreAiEphemeralResp

    @POST("?model={model}")
    suspend fun postData(@Body data: AssistantRequest): Response<AssistantResponse>
}

// Data classes for your API responses
data class ExploreAiEphemeralResp(
    @SerializedName("client_secret") val clientSecret: ClientSecret
)

data class ClientSecret(
    @SerializedName("value") val value: String
)

data class AssistantRequest(
    val model: String
    // ... other fields
)

data class AssistantResponse(
    val responseField: String
    // ... other fields
)