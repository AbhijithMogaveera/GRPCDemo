package com.abhijith.grpc_server.impl

import com.abhijith.echo.EchoRequest
import com.abhijith.echo.EchoServiceGrpc
import io.grpc.stub.StreamObserver
object EchoService : EchoServiceGrpc.EchoServiceImplBase() {
        override fun bidirectionalStreaming(responseObserver: StreamObserver<EchoRequest>): StreamObserver<EchoRequest> {
            return object : StreamObserver<EchoRequest> {
                override fun onNext(request: EchoRequest) {

                    val response = EchoRequest.newBuilder()
                        .setMessage("Echo: ${request.message}")
                        .build()

                    responseObserver.onNext(response)
                }

                override fun onError(t: Throwable) {
                    println("Error occurred: ${t.message}")
                }

                override fun onCompleted() {
                    // Complete the stream
                    responseObserver.onCompleted()
                }
            }
        }
    }