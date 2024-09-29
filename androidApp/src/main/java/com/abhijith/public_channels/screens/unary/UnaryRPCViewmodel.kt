package com.abhijith.public_channels.screens.unary

import androidx.lifecycle.ViewModel
import com.abhijith.public_channels.GreetingServiceGrpc
import com.abhijith.public_channels.HelloRequest
import com.abhijith.public_channels.HelloResponse
import com.abhijith.public_channels.rpc.GRPCClient
import com.abhijith.public_channels.rpc.StreamValue
import com.abhijith.public_channels.rpc.streamAsFlow
import kotlinx.coroutines.flow.Flow

class UnaryRPCViewmodel: ViewModel() {

    private val blockingStub = GreetingServiceGrpc.newBlockingStub(GRPCClient.channel)
    private val asyncStub = GreetingServiceGrpc.newStub(GRPCClient.channel)

    fun sayHello(name: String): HelloResponse {
        val request = HelloRequest.newBuilder()
            .setName(name)
            .build()
        return blockingStub.sayHello(request)
    }

    fun sayHelloAsync(
        name: String
    ): Flow<StreamValue<HelloResponse>> {
        return streamAsFlow {
            val request = HelloRequest.newBuilder()
                .setName(name)
                .build()
            asyncStub.sayHello(request, it)
        }
    }

}