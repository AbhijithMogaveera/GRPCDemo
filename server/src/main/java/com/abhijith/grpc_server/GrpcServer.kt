package com.abhijith.grpc_server

import com.abhijith.grpc_server.impl.EchoService
import com.abhijith.grpc_server.impl.GreetingService
import com.abhijith.grpc_server.impl.HeartRateMonitorService
import com.abhijith.grpc_server.impl.WeatherService
import io.grpc.Server
import io.grpc.ServerBuilder
import java.io.IOException

class GrpcServer(private val port: Int) {

    private val server: Server = ServerBuilder.forPort(port)
        .addService(GreetingService) /*unary*/
        .addService(WeatherService) /*server streaming*/
        .addService(HeartRateMonitorService) /*client streaming*/
        .addService(EchoService) /*bidirectional streaming*/
        .build()

    @Throws(IOException::class)
    fun start() {
        server.start()
        println("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(Thread {
            println("Shutting down gRPC server")
            this@GrpcServer.stop()
            println("Server shut down")
        })
    }

    fun stop() {
        server.shutdown()
    }

    @Throws(InterruptedException::class)
    fun blockUntilShutdown() {
        server.awaitTermination()
    }
}