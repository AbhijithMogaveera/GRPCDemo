package com.abhijith.public_channels.screens.bidiriectional_streaming

import android.util.Log
import androidx.compose.foundation.shape.CircleShape
import com.abhijith.public_channels.ui.components.ChatItem
import com.abhijith.echo.EchoRequest
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.flow.MutableStateFlow
import com.abhijith.echo.EchoServiceGrpc
import com.abhijith.public_channels.rpc.GRPCClient
import com.abhijith.public_channels.ui.components.ChatGravity

class BidirectionalStreamingEchoMachine {

    val echos = MutableStateFlow<List<ChatItem>>(emptyList())


    private val stub: EchoServiceGrpc.EchoServiceStub = EchoServiceGrpc.newStub(GRPCClient.channel)

    fun echo(string: String) {
        val requestObserver = stub.bidirectionalStreaming(object : StreamObserver<EchoRequest> {
            override fun onNext(response: EchoRequest) {
                Log.e("BIDI", "Onnext $response")
                val updatedList = echos.value + ChatItem(
                    text = response.message,
                    shape = CircleShape,
                    gravity = ChatGravity.Left
                )
                echos.value = updatedList
            }

            override fun onError(t: Throwable) {
                Log.e("BIDI", "ERROR", t)
                println("Error during streaming: ${t.message}")
            }

            override fun onCompleted() {
                Log.e("BIDI", "Completed")
            }
        })

        val request = EchoRequest.newBuilder()
            .setMessage(string)
            .build()
        val updatedList = echos.value + ChatItem(
            text = request.message,
            shape = CircleShape,
            gravity = ChatGravity.Right
        )
        echos.value = updatedList
        requestObserver.onNext(request)
    }
}
