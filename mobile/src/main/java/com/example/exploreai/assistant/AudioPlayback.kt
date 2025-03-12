package com.mau.exploreai.assistant

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.AudioTrack.MODE_STREAM
import kotlin.concurrent.thread
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class AudioPlayback {
    private var audioTrack: AudioTrack
    private var bufferSize: Int
    init {
        // Set up audio attributes
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        // Set up audio format
        val sampleRate = 44100 // Sample rate in Hz
        val channelConfig = AudioFormat.CHANNEL_OUT_MONO
        val encoding = AudioFormat.ENCODING_PCM_16BIT

        // Calculate buffer size
        bufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, encoding)

        audioTrack = AudioTrack(
            audioAttributes,
            AudioFormat.Builder()
                .setEncoding(encoding)
                .setSampleRate(sampleRate)
                .setChannelMask(channelConfig)
                .build(),
            bufferSize,
            MODE_STREAM,
            101
        )
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun playAudio(base64Audio: String) {
        val pcmData: ByteArray = Base64.decode(base64Audio)

        // Start playback in a separate thread
        thread {
            try {
                // Start the audio track for playback
                audioTrack.play()

                // Write decoded PCM data to the AudioTrack
                var offset = 0
                while (offset < pcmData.size) {
                    val writeSize = minOf(bufferSize, pcmData.size - offset)
                    audioTrack.write(pcmData, offset, writeSize)
                    offset += writeSize
                }

                // Stop and release resources after playback is complete
                audioTrack.stop()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                audioTrack.release()
            }
        }
    }
}