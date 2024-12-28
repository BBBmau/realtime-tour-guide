package com.example.exploreai

import com.example.exploreai.assistant.ExploreAiAssistantService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Singleton for Retrofit instance
object AssistantClient {
    private const val BASE_URL = "https://api.openai.com/v1/realtime"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ExploreAiAssistantService by lazy {
        retrofit.create(ExploreAiAssistantService::class.java)
    }
}