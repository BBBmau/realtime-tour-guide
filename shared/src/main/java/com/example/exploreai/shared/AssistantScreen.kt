package com.example.exploreai.shared

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.content.Intent
import android.os.Bundle
import java.util.Locale
class AssistantScreen(carContext: CarContext) : Screen(carContext) {
    private var currentResponse: String = "How can I help you?"
    private var textToSpeech: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null

    init {
        initTextToSpeech()
        initSpeechRecognizer()
    }

    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(carContext.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.US
            }
        }
    }

    private fun initSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(carContext.applicationContext)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.get(0)?.let { processCommand(it) }
            }

            // Required RecognitionListener methods
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                respondWithVoice("Sorry, I didn't catch that. Please try again.")
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        try {
            speechRecognizer?.startListening(intent)
            currentResponse = "Listening..."
            invalidate()
        } catch (e: Exception) {
            respondWithVoice("Sorry, I couldn't start listening. Please try again.")
        }
    }

    private fun processCommand(command: String) {
        when {
            command.contains("navigate", ignoreCase = true) -> {
                respondWithVoice("Starting navigation...")
                // Add navigation logic
            }
            command.contains("call", ignoreCase = true) -> {
                respondWithVoice("Making a call...")
                // Add calling logic
            }
            command.contains("message", ignoreCase = true) -> {
                respondWithVoice("Sending a message...")
                // Add messaging logic
            }
            else -> {
                respondWithVoice("I'm sorry, I didn't understand that command")
            }
        }
    }

    override fun onGetTemplate(): Template {
        return MessageTemplate.Builder(currentResponse)
            .setHeaderAction(Action.APP_ICON)
            .addAction(
                Action.Builder()
                    .setTitle("Start Listening")
                    .setOnClickListener { startListening() }
                    .build()
            )
            .build()
    }

    private fun respondWithVoice(response: String) {
        textToSpeech?.speak(response, TextToSpeech.QUEUE_FLUSH, null, null)
        currentResponse = response
        invalidate()
    }
}