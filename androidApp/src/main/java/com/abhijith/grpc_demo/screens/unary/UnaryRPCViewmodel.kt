package com.abhijith.grpc_demo.screens.unary

import androidx.lifecycle.ViewModel
import com.abhijith.greeting_service.v1.GrpcGreetingServiceClient
import com.abhijith.greeting_service.v1.SayHelloRequest
import com.abhijith.greeting_service.v1.SayHelloResponse
import com.abhijith.grpc_demo.rpc.GRPCClientHelper
import com.abhijith.grpc_demo.rpc.StreamValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * ViewModel responsible for handling unary gRPC calls to the Greeting Service.
 *
 * This ViewModel provides a method to asynchronously send a greeting request
 * and receive a response from the server. It leverages Kotlin Coroutines and
 * Flow to manage asynchronous data streams and handle success and error states.
 */
class UnaryRPCViewmodel : ViewModel() {


    private val grpcGreetingServiceClient = GrpcGreetingServiceClient(GRPCClientHelper.client)

    /**
     * Sends an asynchronous unary gRPC request to greet a user by name.
     *
     * This function constructs a [SayHelloRequest] with the provided [name],
     * executes the gRPC call, and emits the result as a [Flow] of [StreamValue].
     * It handles both successful responses and errors, encapsulating them within
     * the [StreamValue] sealed class.
     *
     * @param name The name of the user to greet.
     * @return A [Flow] emitting [StreamValue] instances representing the result
     *         of the gRPC call.
     */
    fun sayHelloAsync(
        name: String
    ): Flow<StreamValue<SayHelloResponse>> {
        return flow {
            kotlin.runCatching {
                grpcGreetingServiceClient.SayHello().execute(
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
