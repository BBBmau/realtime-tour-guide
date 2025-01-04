package com.example.exploreai

import android.util.Log
import com.example.exploreai.assistant.EPHEMERAL_KEY
import com.example.exploreai.assistant.ExploreAiAssistantService
import com.example.exploreai.assistant.SessionBody
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
class SDPConverterFactory : Converter.Factory() {
    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody>? {
        if (type == SessionBody::class.java) {
            return SDPBodyConverter()
        }
        return null
    }
}

class SDPBodyConverter : Converter<SessionBody, RequestBody> {
    override fun convert(value: SessionBody): RequestBody {
        // Convert SessionBody to SDP format
        val sdpContent = """
            v=0
            o=- ${System.currentTimeMillis()} 2 IN IP4 127.0.0.1
            s=-
            t=0 0
            ${value.sdp}
        """.trimIndent()

        // MediaType.parse is deprecated, use MediaType.get instead
        return RequestBody.create(
            "application/sdp".toMediaType(),
            sdpContent
        )
        // Or use the newer syntax:
        // return sdpContent.toRequestBody("application/sdp".toMediaType())
    }
}

private fun okHttpClient(ephemeralKey: String) = OkHttpClient().newBuilder()
    .addInterceptor(
        object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val request: Request = chain.request()
                    .newBuilder()
                    .header("Content-Type", "application/sdp")
                    .header("Authorization", ephemeralKey)
                    .build()
                return chain.proceed(request)
            }
        }
    )

// Singleton for Retrofit instance
object AssistantClient {
    private const val EXPLORE_URL = "https://explore-ai-445408.wl.r.appspot.com/"
    private const val OPENAI_URL = "https://api.openai.com/v1/"

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
            .client(okHttpClient(EPHEMERAL_KEY).build())
            .build()
    }

    val openAiService: ExploreAiAssistantService by lazy {
        Log.d("[RetrofitClient]", "baseUrl: ${openAi.baseUrl()}")
        openAi.create(ExploreAiAssistantService::class.java)
    }
}