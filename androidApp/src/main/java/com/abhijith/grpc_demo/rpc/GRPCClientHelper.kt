package com.abhijith.grpc_demo.rpc

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.MethodDescriptor

var authToken: String? by mutableStateOf(null)

object GRPCClientHelper {

    private const val HOST = "10.0.2.2"

    private const val PORT = 8080

    val channel: ManagedChannel by lazy {
        ManagedChannelBuilder
            .forAddress(
                HOST,
                PORT
            )
            .intercept(
                AuthTokenInterceptor(
                    token = {
                        authToken
                    }
                )
            )
            .usePlaintext().build()
    }

    fun shutdown() {
        if (!channel.isShutdown) {
            channel.shutdown()
        }
    }

}

class AuthTokenInterceptor(private val token: () -> String?) : ClientInterceptor {
    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        method: MethodDescriptor<ReqT, RespT>?,
        callOptions: CallOptions?,
        next: Channel?
    ): ClientCall<ReqT, RespT> {
        return object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
            next!!.newCall(method, callOptions)
        ) {
            override fun start(responseListener: Listener<RespT>?, headers: Metadata?) {
                token()?.let { token ->
                    val authKey: Metadata.Key<String> =
                        Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER)
                    headers?.put(authKey, "Bearer ${token}")
                }
                super.start(responseListener, headers)
            }
        }
    }
}