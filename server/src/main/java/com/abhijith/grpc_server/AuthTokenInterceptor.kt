package com.abhijith.grpc_server

import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status

class AuthTokenInterceptor() : ServerInterceptor {

    val ignoreForMethodStartsWith = listOf(
        "proto.login_service.v1"
    )

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {

        val methodName = call.methodDescriptor.fullMethodName
        println(methodName)
        if (ignoreForMethodStartsWith.any { methodName.startsWith(it) }) {
            return next.startCall(call, headers)
        }
        val AUTHORIZATION_KEY: Metadata.Key<String> =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER)
        val token = headers.get(AUTHORIZATION_KEY)

        return if (token != null && token == "Bearer ${FakeLogin.FakeToken}") {
            next.startCall(call, headers)
        } else {
            call.close(
                Status.UNAUTHENTICATED.withDescription("Invalid or missing token"),
                headers
            )
            object : ServerCall.Listener<ReqT>() {}
        }
    }
}



