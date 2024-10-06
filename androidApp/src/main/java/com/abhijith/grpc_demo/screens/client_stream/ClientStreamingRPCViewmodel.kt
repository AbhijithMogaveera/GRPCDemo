package com.abhijith.grpc_demo.screens.client_stream

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhijith.grpc_demo.rpc.StreamValue
import com.abhijith.grpc_demo.rpc.Streamer
import com.abhijith.grpc_demo.rpc.getStatusCode
import com.abhijith.grpc_demo.rpc.stream
import com.abhijith.grpc_demo.rpc.streamer
import com.abhijith.grpc_demo.ui.components.chat.models.ChatGravity
import com.abhijith.grpc_demo.ui.components.chat.models.ChatItem
import com.abhijith.grpc_demo.ui.components.chat.models.ChatItemMessage
import com.abhijith.grpc_demo.ui.components.chat.models.ChatItemNotice
import com.abhijith.grpc_demo.ui.components.chat.models.ChatItemTheme
import com.abhijith.grpc_demo.ui.components.chat.models.NoticeType
import com.abhijith.grpc_demo.ui.components.chat.util.MessageShapeCenter
import com.abhijith.grpc_demo.ui.components.chat.util.transformAndUpdate
import com.abhijith.heart_rate_service.v1.HeartRateMonitorProto.MonitorHeartRateRequest
import com.abhijith.heart_rate_service.v1.HeartRateMonitorProto.MonitorHeartRateResponse
import com.abhijith.heart_rate_service.v1.HeartRateServiceGrpc
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random


class ClientStreamingRPCViewmodel : ViewModel() {

    private val asyncStub = HeartRateServiceGrpc.newStub(com.abhijith.grpc_demo.rpc.GRPCClientHelper.channel)
    private var job: Job? = null

    var isClientIsStreaming by mutableStateOf(false)
        private set

    private val _chatItems = MutableStateFlow<List<ChatItem>>(emptyList())

    val heartRateChatItem = _chatItems.asStateFlow()
    /**
     * Initiates the process of reading and sending heartbeats.
     * Randomly decides to send either valid or invalid heartbeats for demonstration purposes.
     */
    fun readAndSendHeartBeats() {
        job = viewModelScope.launch {
            val mockNormal = Random.nextBoolean()
            isClientIsStreaming = true
            if (mockNormal) {
                sendValidHeartBeats()
            } else {
                sendInvalidHeartBeats()
            }
            isClientIsStreaming = false
        }
    }


    /**
     * Sends a predefined sequence of invalid heart rate values to the server.
     * Each send is delayed by 300 milliseconds to simulate real-time data streaming.
     */
    private suspend fun sendInvalidHeartBeats() {
        val publish = getHeartRatePublisher()
        repeat(6) {
            publish.onNext(0.0).getOrThrow()
            delay(300)
        }
        publish.onCompleted()
    }

    /**
     * Sends a predefined sequence of valid heart rate values to the server.
     * Each send is delayed by 300 milliseconds to simulate real-time data streaming.
     */
    private suspend fun sendValidHeartBeats() {
        val streamBuilder = getHeartRatePublisher()
        val validHeartRates = listOf(45.0, 50.0, 45.0, 50.0, 45.0, 50.0)
        for (heartRate in validHeartRates) {
            streamBuilder.onNext(heartRate).getOrThrow()
            delay(300)
        }
        streamBuilder.onCompleted()
    }

    private fun getHeartRatePublisher(): Streamer<Double> {
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
            _chatItems.transformAndUpdate { chatItems ->
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
                    shape = MessageShapeCenter,
                    theme = ChatItemTheme.ErrorMessage
                )
            }
        }

    }

    private fun appendToChatList(heartRate: Double) {
        viewModelScope.launch {
            _chatItems.transformAndUpdate { chatItems ->
                chatItems + ChatItemMessage(
                    gravity = ChatGravity.Right,
                    text = "❤️ $heartRate sent to server",
                    shape = MessageShapeCenter
                )
            }
        }

    }

    private fun append(string: String, noticeType: NoticeType) {
        viewModelScope.launch {
            _chatItems.transformAndUpdate {
                it + ChatItemNotice(
                    message = string,
                    type = noticeType
                )
            }
        }
    }

    private fun appendToChatList(res: MonitorHeartRateResponse) {
        viewModelScope.launch {
            _chatItems.transformAndUpdate { chatItems ->
                chatItems + ChatItemMessage(
                    gravity = ChatGravity.Left, text = res.message,
                    shape = MessageShapeCenter
                )
            }
        }
    }


}
