package com.abhijith.grpc_demo.screens.bidiriectional_streaming

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhijith.echo_service.v1.EchoRequest
import com.abhijith.echo_service.v1.GrpcEchoServiceClient
import com.abhijith.public_channels.rpc.GRPCClientHelper
import com.abhijith.public_channels.rpc.Streamer
import com.abhijith.public_channels.rpc.getStatusCode
import com.abhijith.public_channels.rpc.streamer
import com.abhijith.public_channels.ui.components.ChatGravity
import com.abhijith.public_channels.ui.components.ChatItem
import com.abhijith.public_channels.ui.components.ChatItemMessage
import com.abhijith.public_channels.ui.components.ChatItemNotice
import com.abhijith.public_channels.ui.components.NoticeType
import com.abhijith.public_channels.ui.components.messageShapeDefault
import com.abhijith.public_channels.ui.components.transformAndUpdate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import com.abhijith.echo_service.v1.EchoServiceGrpc
import com.abhijith.grpc_demo.rpc.StreamValue
import com.abhijith.grpc_demo.rpc.Streamer
import com.abhijith.grpc_demo.rpc.getStatusCode
import com.abhijith.grpc_demo.rpc.stream
import com.abhijith.grpc_demo.rpc.streamer
import com.abhijith.grpc_demo.ui.components.chat.models.ChatGravity
import com.abhijith.grpc_demo.ui.components.chat.models.ChatItem
import com.abhijith.grpc_demo.ui.components.chat.models.ChatItemMessage
import com.abhijith.grpc_demo.ui.components.chat.models.ChatItemNotice
import com.abhijith.grpc_demo.ui.components.chat.models.NoticeType
import com.abhijith.grpc_demo.ui.components.chat.util.transformAndUpdate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class BidirectionalStreamingRPCViewmodel : ViewModel() {

    val echos = MutableStateFlow<List<ChatItem>>(emptyList())
    private var callScope: CoroutineScope? = null
    private val stub = GrpcEchoServiceClient(GRPCClientHelper.client)
    private var streamerOrNull: Streamer<String>? = null

    var isConnected: Boolean by mutableStateOf(false)

    fun connectToServer() {
        callScope = CoroutineScope(Job() + Dispatchers.IO)
        streamerOrNull = getStreamer()
        isConnected = true
    }

    fun echo(string: String): Boolean = streamerOrNull?.onNext(string)?.isSuccess ?: false

    fun disConnect() {
        streamerOrNull?.onCompleted()
        streamerOrNull = null
        isConnected = false
        callScope?.cancel()
    }

    private fun getStreamer(): Streamer<String> {
        val callScope = requireNotNull(callScope)
        var isActive = true
        viewModelScope.launch {
            echos.transformAndUpdate { items ->
                items + ChatItemNotice(
                    message = "Connected",
                    type = NoticeType.Normal
                )
            }
        }
        val (req, res) = stub.Echo().executeIn(callScope)
        callScope.launch {
            var hasError = false
            try {
                res
                    .receiveAsFlow()
                    .onEach {
                        viewModelScope.launch {
                            echos.transformAndUpdate { items ->
                                items + ChatItemMessage(
                                    gravity = ChatGravity.Left,
                                    shape = messageShapeDefault,
                                    text = it.message
                                )
                            }
                        }
                    }
                    .catch {
                        echos.transformAndUpdate { items ->
                            items + ChatItemNotice(
                                message = "Disconnected with error ${it.getStatusCode()}",
                                type = NoticeType.Error
                            )
                        }
                        disConnect()
                        hasError = true
                    }.collect()
            } finally {
                if(!hasError) {
                    viewModelScope.launch {
                        echos.transformAndUpdate { items ->
                            items + ChatItemNotice(
                                message = "Disconnected",
                                type = NoticeType.Normal
                            )
                        }
                    }
                }
            }
            isActive = false
        }

        return streamer(
            isActive = { isActive },
            onComplete = {

                req.close()
            }
        ) {
            it.onValue {
                val echoRequest = EchoRequest(message = it)
                callScope.launch {
                    req.send(echoRequest)
                }
                viewModelScope.launch {
                    echos.transformAndUpdate { items ->
                        items + ChatItemMessage(
                            gravity = ChatGravity.Right,
                            text = echoRequest.message
                        )
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disConnect()
    }
}
