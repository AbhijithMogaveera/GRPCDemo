package com.abhijith.grpc_server.impl

import com.abhijith.public_channels.GreetingServiceGrpc
import com.abhijith.public_channels.HelloRequest
import com.abhijith.public_channels.HelloResponse
import io.grpc.stub.StreamObserver

object GreetingService : GreetingServiceGrpc.GreetingServiceImplBase() {
    override fun sayHello(request: HelloRequest, responseObserver: StreamObserver<HelloResponse>) {
        val response = HelloResponse.newBuilder()
            .setMessage("Hello, ${request.name}")
            .build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}