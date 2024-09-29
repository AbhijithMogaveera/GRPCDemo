package com.abhijith.grpc_server.impl

import com.abhijith.public_channels.GreetingServiceGrpc
import com.abhijith.public_channels.HelloRequest
import com.abhijith.public_channels.HelloResponse
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import kotlin.random.Random

object GreetingService : GreetingServiceGrpc.GreetingServiceImplBase() {
    private var throwError = true
    override fun sayHello(request: HelloRequest, responseObserver: StreamObserver<HelloResponse>) {
        throwError = !throwError
        Thread.sleep(500)
        if (throwError) {
            responseObserver.onError(
                StatusRuntimeException(
                    Status.ABORTED.withDescription(
                        "iisque nec unum principes arcu blandit himenaeos mattis sem alienum" +
                                " porro dui splendide sociis habemus similique mattis dicunt ultrices" +
                                " dolorum adversarium tantas tale debet nonumy aeque sonet non " +
                                "interpretaris tation"
                    )
                )
            )
            return
        }
        val response = HelloResponse.newBuilder()
            .setMessage("Hello, ${request.name}")
            .build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}