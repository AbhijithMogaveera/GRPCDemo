package com.abhijith.grpc_server.impl

import com.abhijith.greeting_service.v1.GreetingServiceServer
import com.abhijith.greeting_service.v1.SayHelloRequest
import com.abhijith.greeting_service.v1.SayHelloResponse
import io.grpc.Status
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.delay

object GreetingService : GreetingServiceServer {

    private var throwError = true

    override suspend fun SayHello(request: SayHelloRequest): SayHelloResponse {
        throwError = !throwError
        delay(500)
        if (throwError) {
            throw StatusRuntimeException(
                Status.ABORTED
                    .withDescription(
                        /*description=*/"iisque nec unum principes arcu blandit himenaeos mattis sem alienum" +
                                " porro dui splendide sociis habemus similique mattis dicunt ultrices" +
                                " dolorum adversarium tantas tale debet nonumy aeque sonet non " +
                                "interpretaris tation"
                    )
            )
        }
        return SayHelloResponse("Hello, ${request.name}")
    }
}