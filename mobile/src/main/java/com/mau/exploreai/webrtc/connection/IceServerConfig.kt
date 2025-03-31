package com.mau.exploreai.webrtc.webrtc.connection

import org.webrtc.PeerConnection

/**
 * Configuration for ICE servers
 */
class IceServerConfig {
    /**
     * Get default ICE servers for WebRTC connection
     */
    fun getDefaultIceServers(): List<PeerConnection.IceServer> {
        return listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer()
        )
    }
    
    /**
     * Get custom ICE servers with authentication if needed
     */
    fun getCustomIceServers(username: String, credential: String): List<PeerConnection.IceServer> {
        return listOf(
            PeerConnection.IceServer.builder("turn:your-turn-server.com")
                .setUsername(username)
                .setPassword(credential)
                .createIceServer()
        )
    }
    
    /**
     * Get a complete RTCConfiguration with specified ICE servers
     */
    fun getRTCConfiguration(iceServers: List<PeerConnection.IceServer>): PeerConnection.RTCConfiguration {
        return PeerConnection.RTCConfiguration(iceServers).apply {
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }
    }
} 