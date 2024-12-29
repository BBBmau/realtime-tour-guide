package com.example.exploreai

import com.example.exploreai.assistant.EPHEMERAL_KEY
import com.example.exploreai.assistant.ExploreAiAssistantService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private fun okHttpClient(ephemeralKey: String) = OkHttpClient().newBuilder()
    .addInterceptor(
        object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val request: Request = chain.request()
                    .newBuilder()
                    .header("accept", "application/sdp")
                    .header("Authorization", ephemeralKey)
                    .build()
                return chain.proceed(request)
            }
        }
    )

// Singleton for Retrofit instance
object AssistantClient {
    private const val EXPLORE_URL = "https://explore-ai-445408.wl.r.appspot.com/"
    private const val OPENAI_URL = "https://api.openai.com/v1/realtime"

    private val exploreAi: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(EXPLORE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ExploreAiAssistantService by lazy {
        exploreAi.create(ExploreAiAssistantService::class.java)
    }

    private val openAi: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(OPENAI_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient(EPHEMERAL_KEY).build())
            .build()
    }

    val openAiService: ExploreAiAssistantService by lazy {
        openAi.create(ExploreAiAssistantService::class.java)
    }
}