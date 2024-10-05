package com.abhijith.grpc_server.impl

import com.abhijith.echo_service.v1.EchoRequest
import com.abhijith.echo_service.v1.EchoResponse
import com.abhijith.echo_service.v1.EchoServiceServer
import io.grpc.Status
import io.grpc.StatusException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

object EchoService : EchoServiceServer {
    private var throwError = true

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun Echo(
        request: ReceiveChannel<EchoRequest>,
        response: SendChannel<EchoResponse>
    ) {
        throwError = !throwError
        request
            .receiveAsFlow()
            .onEach { req ->
                if (throwError) {
                    response.close(StatusException(Status.ABORTED.withDescription("verear iaculis viderer lacus primis senectus quaestio te civibus veritus tincidunt commune adipisci reprehendunt facilisi reprehendunt signiferumque montes sapien egestas vocent commodo comprehensam")))
                }
                response.trySend(EchoResponse(message = "Echo: ${req.message}"))
            }.collect()
        if(!response.isClosedForSend){
            response.close()
        }
    }
}