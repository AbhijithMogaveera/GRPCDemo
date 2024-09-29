package com.abhijith.public_channels.screens.unary

import androidx.lifecycle.ViewModel
import com.abhijith.greeting_service.v1.GreetingServiceGrpc
import com.abhijith.greeting_service.v1.SayHelloRequest
import com.abhijith.greeting_service.v1.SayHelloResponse
import com.abhijith.public_channels.rpc.GRPCClient
import com.abhijith.public_channels.rpc.StreamValue
import com.abhijith.public_channels.rpc.streamAsFlow
import kotlinx.coroutines.flow.Flow

class UnaryRPCViewmodel: ViewModel() {

    private val blockingStub = GreetingServiceGrpc.newBlockingStub(GRPCClient.channel)
    private val asyncStub = GreetingServiceGrpc.newStub(GRPCClient.channel)

    fun sayHello(name: String): SayHelloResponse {
        val request = SayHelloRequest.newBuilder()
            .setName(name)
            .build()
        return blockingStub.sayHello(request)
    }

    fun sayHelloAsync(
        name: String
    ): Flow<StreamValue<SayHelloResponse>> {
        return streamAsFlow {
            val request = SayHelloRequest.newBuilder()
                .setName(name)
                .build()
            asyncStub.sayHello(request, it)
        }
    }

}