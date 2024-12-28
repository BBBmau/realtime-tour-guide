package com.example.exploreai.assistant
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.Response
interface ExploreAiAssistantService {

    // requests OpenAi Ephemeral Key from GCP Server after it receives it from OpenAI directly
    @GET("sessions")
    suspend fun getData(): Response<ExploreAiEphemeralResp>

    @POST("?model={model}")
    suspend fun postData(@Body data: AssistantRequest): Response<AssistantResponse>
}

// Data classes for your API responses
data class ExploreAiEphemeralResp(
    val field1: String,
    val field2: Int,
    // ... other fields
)

data class AssistantRequest(
    val model: String
    // ... other fields
)

data class AssistantResponse(
    val responseField: String
    // ... other fields
)