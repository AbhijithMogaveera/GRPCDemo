package com.abhijith.grpc_server

import com.abhijith.public_channels.GreetingServiceGrpc
import com.abhijith.public_channels.HelloRequest
import com.abhijith.public_channels.HelloResponse
import io.grpc.stub.StreamObserver
import io.grpc.Server
import io.grpc.ServerBuilder
import java.io.IOException

class GreetingServiceImpl : GreetingServiceGrpc.GreetingServiceImplBase() {
    override fun sayHello(request: HelloRequest, responseObserver: StreamObserver<HelloResponse>) {
        val response = HelloResponse.newBuilder()
            .setMessage("Hello, ${request.name}")
            .build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}


class GrpcServer(private val port: Int) {

    private val server: Server = ServerBuilder.forPort(port)
        .addService(GreetingServiceImpl())
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

fun main() {
    val server = GrpcServer(8080)
    server.start()
    server.blockUntilShutdown()
}