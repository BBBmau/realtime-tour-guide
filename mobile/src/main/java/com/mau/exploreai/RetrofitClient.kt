package com.mau.exploreai

import android.util.Log
import com.mau.exploreai.assistant.EPHEMERAL_KEY
import com.mau.exploreai.assistant.ExploreAiAssistantService
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
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
        return when (type) {
            String::class.java -> SDPBodyConverter()
            else -> null
        }
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        return when (type) {
            String::class.java -> SDPResponseConverter()
            else -> null
        }
    }
}

class SDPBodyConverter : Converter<String, RequestBody> {
    override fun convert(sdp: String): RequestBody {
        // Use the raw bytes of the SDP string to avoid charset being added
        val mediaType = "application/sdp".toMediaType()
        return RequestBody.create(mediaType, sdp.toByteArray(Charsets.UTF_8))
    }
}

class SDPResponseConverter : Converter<ResponseBody, String> {
    override fun convert(value: ResponseBody): String {
        return value.string()
    }
}
val logging = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}

private fun okHttpClient(ephemeralKey: String) = OkHttpClient().newBuilder()
    .addInterceptor(
        object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val request: Request = chain.request()
                    .newBuilder()
                    .header("Content-Type", "application/sdp".toMediaType().toString())
                    .header("Authorization", "Bearer $ephemeralKey")
                    .build()
                return chain.proceed(request)
            }
        }
    )
    .addInterceptor(logging)

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
            .addConverterFactory(SDPConverterFactory())
            .build()
    }

    val openAiService: ExploreAiAssistantService by lazy {
        Log.d("[RetrofitClient]", "baseUrl: ${openAi.baseUrl()}")
        openAi.create(ExploreAiAssistantService::class.java)
    }
}