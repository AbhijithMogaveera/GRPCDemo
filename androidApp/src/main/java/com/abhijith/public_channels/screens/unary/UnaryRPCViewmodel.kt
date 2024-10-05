package com.abhijith.public_channels.screens.unary

import androidx.lifecycle.ViewModel
import com.abhijith.greeting_service.v1.GrpcGreetingServiceClient
import com.abhijith.greeting_service.v1.SayHelloRequest
import com.abhijith.greeting_service.v1.SayHelloResponse
import com.abhijith.public_channels.rpc.GRPCClientHelper
import com.abhijith.public_channels.rpc.StreamValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UnaryRPCViewmodel : ViewModel() {

    private val asyncStub = GrpcGreetingServiceClient(GRPCClientHelper.client)

    fun sayHelloAsync(
        name: String
    ): Flow<StreamValue<SayHelloResponse>> {
        return flow {
            kotlin.runCatching {
                asyncStub.SayHello().execute(
                    SayHelloRequest(
                        name = name
                    )
                )
            }.onSuccess { res ->
                emit(StreamValue.Value(res))
            }.onFailure { err ->
                err.printStackTrace()
                emit(StreamValue.Error(err, null))
            }
        }
    }

}