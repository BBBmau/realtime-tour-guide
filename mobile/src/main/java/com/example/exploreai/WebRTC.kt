package com.example.exploreai

import org.webrtc.PeerConnectionFactory

class WebRTC {
    // Initialize PeerConnectionFactory globals.
    val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(context)
        .createInitializationOptions()
    PeerConnectionFactory.initialize(initializationOptions)

    // Create a new PeerConnectionFactory instance.
    val options = PeerConnectionFactory.Options()
    val peerConnectionFactory = PeerConnectionFactory.builder()
        .setOptions(options)
        .createPeerConnectionFactory()
}
