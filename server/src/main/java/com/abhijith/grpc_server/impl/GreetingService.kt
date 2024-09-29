package com.abhijith.grpc_server.impl

import com.abhijith.greeting_service.v1.GreetingServiceGrpc
import com.abhijith.greeting_service.v1.SayHelloRequest
import com.abhijith.greeting_service.v1.SayHelloResponse
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver

object GreetingService : GreetingServiceGrpc.GreetingServiceImplBase() {
    private var throwError = true
    override fun sayHello(request: SayHelloRequest, responseObserver: StreamObserver<SayHelloResponse>) {
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
        val response = SayHelloResponse.newBuilder()
            .setMessage("Hello, ${request.name}")
            .build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}