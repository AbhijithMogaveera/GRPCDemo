package com.abhijith.grpc_demo.rpc

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.squareup.wire.GrpcClient
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request

var authToken: String? by mutableStateOf(null)

object GRPCClientHelper {

    private const val HOST = "10.0.2.2"

    private const val PORT = 8080

    val client = GrpcClient.Builder().baseUrl("http://$HOST:$PORT/")
        .client(
            OkHttpClient.Builder()
                .protocols(listOf(Protocol.H2_PRIOR_KNOWLEDGE))
                .addInterceptor(
                    Interceptor { chain ->
                        val tokenValue = authToken
                        val originalRequest: Request = chain.request()
                        val newRequestBuilder = originalRequest.newBuilder()
                        tokenValue?.let { token ->
                            newRequestBuilder.addHeader("Authorization", "Bearer $token")
                        }
                        val newRequest = newRequestBuilder.build()
                        chain.proceed(newRequest)
                    }
                ).build()
        )
        .build()

}
