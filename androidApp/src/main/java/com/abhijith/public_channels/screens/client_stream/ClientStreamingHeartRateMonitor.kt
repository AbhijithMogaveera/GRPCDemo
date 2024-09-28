package com.abhijith.public_channels.screens.client_stream

import com.abhijith.public_channels.ui.components.ChatItem
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.abhijith.public_channels.rpc.GRPCClient
import com.abhijith.public_channels.ui.components.ChatGravity
import com.example.heartratemonitor.HeartRateMonitorGrpc
import com.example.heartratemonitor.HeartRateMonitorProto
import com.example.heartratemonitor.HeartRateMonitorProto.HeartRateRequest
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class ClientStreamingHeartRateMonitor {
    private val defaultCornerSize = 25.dp

    interface Publisher {
        fun onNext(heartRate: Double)
        fun onCompleted()
    }

    private val asyncStub = HeartRateMonitorGrpc.newStub(GRPCClient.channel)
    val heartRateChatItem = MutableStateFlow<List<ChatItem>>(emptyList())

    fun getHeartRatePublisher(): Publisher {
        val streamRequest = asyncStub.streamHeartRate(
            createResponseObserver(
                onResponse = ::append,
                onComplete = { errOrNull ->
                    errOrNull?.printStackTrace()
                },
            )
        )
        return object : Publisher {
            override fun onNext(heartRate: Double) {
                streamRequest.onNext(
                    HeartRateRequest
                        .newBuilder()
                        .setHeartRate(heartRate)
                        .build()
                )
                append(heartRate)
            }

            override fun onCompleted() {
                streamRequest.onCompleted()
            }

        }
    }


    private fun append(heartRate: Double) {
        heartRateChatItem.update {
            it + ChatItem(
                gravity = ChatGravity.Right,
                text = "❤️ $heartRate",
                shape = RoundedCornerShape(
                    topStart = defaultCornerSize,
                    topEnd = defaultCornerSize,
                    bottomStart = defaultCornerSize,
                    bottomEnd = defaultCornerSize * 0.6f
                )
            )
        }
    }

    private fun append(res: HeartRateMonitorProto.AnomalyResponse) {
        heartRateChatItem.update {
            it + ChatItem(
                gravity = ChatGravity.Left,
                text = res.message,
                shape = RoundedCornerShape(
                    topStart = defaultCornerSize,
                    topEnd = defaultCornerSize,
                    bottomStart = defaultCornerSize * 0.6f,
                    bottomEnd = defaultCornerSize
                )
            )
        }
    }


    private fun createResponseObserver(
        onResponse: (HeartRateMonitorProto.AnomalyResponse) -> Unit,
        onComplete: (error: Throwable?) -> Unit
    ): StreamObserver<HeartRateMonitorProto.AnomalyResponse> {
        return object : StreamObserver<HeartRateMonitorProto.AnomalyResponse> {
            override fun onNext(anomalyResponse: HeartRateMonitorProto.AnomalyResponse) {
                onResponse(anomalyResponse)
            }

            override fun onError(t: Throwable) {
                onComplete(t)
            }

            override fun onCompleted() {
                onComplete(null)
            }
        }
    }

}
