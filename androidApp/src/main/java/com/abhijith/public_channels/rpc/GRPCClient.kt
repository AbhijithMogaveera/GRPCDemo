package com.abhijith.public_channels.rpc

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder

object GRPCClient {

    private const val HOST = "10.0.2.2"

    private const val PORT = 8080

    val channel: ManagedChannel by lazy {
        ManagedChannelBuilder
            .forAddress(HOST, PORT)
            .usePlaintext().build()
    }

    fun shutdown() {
        if (!channel.isShutdown) {
            channel.shutdown()
        }
    }

}
