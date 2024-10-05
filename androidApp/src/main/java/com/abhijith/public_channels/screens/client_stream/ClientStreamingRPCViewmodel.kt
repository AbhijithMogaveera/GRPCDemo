package com.abhijith.public_channels.screens.client_stream

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhijith.heart_rate_service.v1.GrpcHeartRateServiceClient
import com.abhijith.heart_rate_service.v1.MonitorHeartRateRequest
import com.abhijith.heart_rate_service.v1.MonitorHeartRateResponse
import com.abhijith.public_channels.rpc.GRPCClientHelper
import com.abhijith.public_channels.rpc.StreamValue
import com.abhijith.public_channels.rpc.Streamer
import com.abhijith.public_channels.rpc.getStatusCode
import com.abhijith.public_channels.rpc.streamer
import com.abhijith.public_channels.ui.components.ChatGravity
import com.abhijith.public_channels.ui.components.ChatItem
import com.abhijith.public_channels.ui.components.ChatItemMessage
import com.abhijith.public_channels.ui.components.ChatItemNotice
import com.abhijith.public_channels.ui.components.ChatItemTheme
import com.abhijith.public_channels.ui.components.NoticeType
import com.abhijith.public_channels.ui.components.messageShapeCenter
import com.abhijith.public_channels.ui.components.transformAndUpdate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ClientStreamingRPCViewmodel : ViewModel() {

    var callScope: CoroutineScope? = null

    private val asyncStub = GrpcHeartRateServiceClient(GRPCClientHelper.client)

    val heartRateChatItem = MutableStateFlow<List<ChatItem>>(emptyList())

    fun getHeartRatePublisher(): Streamer<Double> {
        callScope?.cancel()
        callScope = CoroutineScope(context = Job() + Dispatchers.IO)
        val callScope = requireNotNull(callScope)
        var isActive = true
        append("Client stream started", NoticeType.Normal)
        val (req, res) = asyncStub.MonitorHeartRate().executeIn(callScope)
        callScope.launch {
            var hasError: Boolean = false
            res.consumeAsFlow()
                .onEach {
                    isActive = false
                    appendToChatList(it)
                }.catch {
                    hasError = true
                    isActive = false
                    appendToChatList(it)
                    append(
                        "Client stream ended with error ${it.getStatusCode()}",
                        NoticeType.Error
                    )
                }.collect()
            if (!hasError) {
                append("Client stream ended", NoticeType.Normal)
            }
        }
        return streamer(
            isActive = { isActive },
            onComplete = {
                req.close()
            }
        ) { streamValue ->
            streamValue.onValue { value ->
                req.trySend(MonitorHeartRateRequest(value))
                appendToChatList(value)
            }
        }
    }

    private fun appendToChatList(streamValue: Throwable) {
        viewModelScope.launch {
            heartRateChatItem.transformAndUpdate { chatItems ->
                chatItems + ChatItemMessage(
                    gravity = ChatGravity.Left,
                    text = let { _ ->
                        var message = "Oops! something went wrong"
                        StreamValue.Error(streamValue, null).apply {
                            onGrpcException {
                                message = it.message ?: message
                            }
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


    override fun onCleared() {
        super.onCleared()
        callScope?.cancel()
    }
}
