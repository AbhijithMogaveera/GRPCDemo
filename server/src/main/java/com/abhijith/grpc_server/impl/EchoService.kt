package com.abhijith.grpc_server.impl

import com.abhijith.echo_service.v1.EchoRequest
import com.abhijith.echo_service.v1.EchoResponse
import com.abhijith.echo_service.v1.EchoServiceGrpc
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.stub.StreamObserver

object EchoService : EchoServiceGrpc.EchoServiceImplBase() {
    private var throwError = true
    override fun echo(responseObserver: StreamObserver<EchoResponse>): StreamObserver<EchoRequest> {
        throwError = !throwError
        return object : StreamObserver<EchoRequest> {
            override fun onNext(request: EchoRequest?) {
                val response = EchoResponse.newBuilder()
                    .setMessage("Echo: ${request?.message}")
                    .build()
                if (throwError) {
                    responseObserver.onError(
                        StatusException(Status.ABORTED.withDescription("verear iaculis viderer lacus primis senectus quaestio te civibus veritus tincidunt commune adipisci reprehendunt facilisi reprehendunt signiferumque montes sapien egestas vocent commodo comprehensam"))
                    )
                    return
                }
                responseObserver.onNext(response)
            }

            override fun onError(t: Throwable) {}

            override fun onCompleted() {
                responseObserver.onCompleted()
            }
        }
    }
}