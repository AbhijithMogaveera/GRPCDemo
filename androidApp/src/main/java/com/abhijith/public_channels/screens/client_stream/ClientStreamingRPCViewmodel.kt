package com.abhijith.public_channels.screens.client_stream

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhijith.heart_rate_service.v1.HeartRateMonitorProto.MonitorHeartRateRequest
import com.abhijith.heart_rate_service.v1.HeartRateMonitorProto.MonitorHeartRateResponse
import com.abhijith.heart_rate_service.v1.HeartRateServiceGrpc
import com.abhijith.public_channels.rpc.GRPCClient
import com.abhijith.public_channels.rpc.StreamValue
import com.abhijith.public_channels.rpc.Streamer
import com.abhijith.public_channels.rpc.getStatusCode
import com.abhijith.public_channels.rpc.stream
import com.abhijith.public_channels.rpc.streamer
import com.abhijith.public_channels.ui.components.ChatGravity
import com.abhijith.public_channels.ui.components.ChatItem
import com.abhijith.public_channels.ui.components.ChatItemMessage
import com.abhijith.public_channels.ui.components.ChatItemNotice
import com.abhijith.public_channels.ui.components.ChatItemTheme
import com.abhijith.public_channels.ui.components.NoticeType
import com.abhijith.public_channels.ui.components.messageShapeCenter
import com.abhijith.public_channels.ui.components.transformAndUpdate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ClientStreamingRPCViewmodel : ViewModel() {


    private val asyncStub = HeartRateServiceGrpc.newStub(GRPCClient.channel)

    val heartRateChatItem = MutableStateFlow<List<ChatItem>>(emptyList())

    fun getHeartRatePublisher(): Streamer<Double> {
        var isActive = true
        append("Client stream started", NoticeType.Normal)
        val requestStreamObserver = asyncStub.monitorHeartRate(stream { streamValue ->
            when (streamValue) {
                is StreamValue.Value -> {
                    isActive = false
                    appendToChatList(streamValue.value)
                }

                is StreamValue.Error -> {
                    isActive = false
                    appendToChatList(streamValue)
                    append(
                        "Client stream ended with error ${streamValue.error.getStatusCode()}",
                        NoticeType.Error
                    )
                }

                is StreamValue.Complete -> {
                    append("Client stream ended", NoticeType.Normal)
                }
            }
        })
        return streamer(
            isActive = { isActive },
            onComplete = requestStreamObserver::onCompleted
        ) { streamValue ->
            streamValue.onValue { value ->
                requestStreamObserver.onNext(
                    MonitorHeartRateRequest
                        .newBuilder()
                        .setHeartRate(value)
                        .build()
                )
                appendToChatList(value)
            }
        }
    }

    private fun appendToChatList(streamValue: StreamValue.Error<MonitorHeartRateResponse>) {
        viewModelScope.launch {
            heartRateChatItem.transformAndUpdate { chatItems ->
                chatItems + ChatItemMessage(
                    gravity = ChatGravity.Left,
                    text = let { _ ->
                        var message = "Oops! something went wrong"
                        streamValue.onStatusException {
                            message = it.message ?: message
                        }
                        streamValue.onStatusRuntimeException {
                            message = it.message ?: message
                        }
                        message
                    },
                    shape = messageShapeCenter,
                    theme = ChatItemTheme.ErrorMessage
                )
            }
        }

    }

    private fun appendToChatList(heartRate: Double) {
        viewModelScope.launch {
            heartRateChatItem.transformAndUpdate { chatItems ->
                chatItems + ChatItemMessage(
                    gravity = ChatGravity.Right,
                    text = "❤️ $heartRate sent to server",
                    shape = messageShapeCenter
                )
            }
        }

    }

    private fun append(string: String, noticeType: NoticeType) {
        viewModelScope.launch {
            heartRateChatItem.transformAndUpdate {
                it + ChatItemNotice(
                    message = string,
                    type = noticeType
                )
            }
        }
    }

    private fun appendToChatList(res: MonitorHeartRateResponse) {
        viewModelScope.launch {
            heartRateChatItem.transformAndUpdate { chatItems ->
                chatItems + ChatItemMessage(
                    gravity = ChatGravity.Left, text = res.message,
                    shape = messageShapeCenter
                )
            }
        }
    }


}
