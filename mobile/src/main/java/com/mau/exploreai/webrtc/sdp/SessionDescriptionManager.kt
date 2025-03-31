package com.mau.exploreai.webrtc.webrtc.sdp

import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SessionDescriptionManager {

    /**
     * Creates an SDP offer and sets it as the local description
     */
    suspend fun createOffer(peerConnection: PeerConnection): String = coroutineScope {
        suspendCancellableCoroutine { continuation ->
            val offerObserver = object : SdpObserver {
                override fun onCreateSuccess(sessionDescription: SessionDescription) {
                    try {
                        var sdp = sessionDescription.description
                        // Modify SDP if needed
                        sdp = sdp.replace("a=setup:active", "a=setup:actpass")
                        val sanitizedSDP = SessionDescription(
                            SessionDescription.Type.OFFER,
                            sdp
                        )
                        Log.d("[createOffer]", "Setting local description with SDP: ${sanitizedSDP.description}")

                        // Wait for setLocalDescription to complete
                        setLocalDescription(peerConnection, sanitizedSDP).invokeOnCompletion { throwable ->
                            if (throwable != null) { continuation.resumeWithException(throwable) }
                            else {
                                // Only continue when local description is set
                                peerConnection.localDescription?.let { desc ->
                                    continuation.resume(desc.description)
                                } ?: continuation.resumeWithException(Exception("Local description is null"))
                            }
                        }
                    } catch (e: Exception) { continuation.resumeWithException(e) }
                }

                override fun onCreateFailure(error: String) { continuation.resumeWithException(Exception("Failed to create offer: $error")) }

                override fun onSetSuccess() { Log.d("[createOffer]", "SUCCESS: set local description") }
                
                override fun onSetFailure(error: String) { Log.d("[createOffer]", error) }
            }

            val constraints = MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
            }

            try {
                peerConnection.createOffer(offerObserver, constraints)
                Log.d("[createOffer]","signaling state now: ${peerConnection.signalingState()}")
            } catch (e: Exception) { continuation.resumeWithException(e) }
        }
    }

    /**
     * Sets the local session description
     */
    private fun setLocalDescription(
        peerConnection: PeerConnection,
        description: SessionDescription
    ) = CompletableDeferred<Unit>().apply {
        peerConnection.setLocalDescription(object : SdpObserver {
            override fun onSetSuccess() {
                Log.d("[setLocalDescription]", "Set local description success")
                complete(Unit)
            }

            override fun onSetFailure(error: String) {
                Log.e("[setLocalDescription]", "Set local description failed: $error")
                completeExceptionally(Exception("Failed to set local description: $error"))
            }

            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onCreateFailure(p0: String?) {}
        }, description)
    }

    /**
     * Sets the remote session description
     */
    fun setRemoteDescription(
        peerConnection: PeerConnection,
        description: SessionDescription
    ) = CompletableDeferred<Unit>().apply {
        peerConnection.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {}

            override fun onSetSuccess() {
                Log.d("[setRemoteDescription]", "Set remote description success")
                Log.d("[setRemoteDescription]", "remoteDescription: ${peerConnection.remoteDescription.description}")
                Log.d("[peerConnection]","current connection state: ${peerConnection.connectionState()}")
                complete(Unit)
            }
            
            override fun onCreateFailure(error: String) {
                Log.e("[createRemoteDescription]", "Unable to set remote description: $error")
                completeExceptionally(Exception("Failed to create remote description: $error"))
            }
            
            override fun onSetFailure(error: String) {
                Log.e("[createRemoteDescription]", error)
                completeExceptionally(Exception("Failed to set remote description: $error"))
            }
        }, description)
    }
} 