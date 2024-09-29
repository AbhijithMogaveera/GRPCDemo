package com.abhijith.public_channels.screens.bidiriectional_streaming

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhijith.public_channels.ui.components.ChatItem
import com.abhijith.echo_service.v1.EchoRequest
import kotlinx.coroutines.flow.MutableStateFlow
import com.abhijith.echo_service.v1.EchoServiceGrpc
import com.abhijith.public_channels.rpc.GRPCClient
import com.abhijith.public_channels.rpc.StreamValue
import com.abhijith.public_channels.rpc.Streamer
import com.abhijith.public_channels.rpc.getStatusCode
import com.abhijith.public_channels.rpc.stream
import com.abhijith.public_channels.rpc.streamer
import com.abhijith.public_channels.ui.components.ChatGravity
import com.abhijith.public_channels.ui.components.ChatItemMessage
import com.abhijith.public_channels.ui.components.ChatItemNotice
import com.abhijith.public_channels.ui.components.NoticeType
import com.abhijith.public_channels.ui.components.messageShapeDefault
import com.abhijith.public_channels.ui.components.transformAndUpdate
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.launch

class BidirectionalStreamingRPCViewmodel : ViewModel() {

    val echos = MutableStateFlow<List<ChatItem>>(emptyList())

    private val stub: EchoServiceGrpc.EchoServiceStub = EchoServiceGrpc.newStub(GRPCClient.channel)
    private var streamerOrNull: Streamer<String>? = null
    var isConnected: Boolean by mutableStateOf(false)

    fun connectToServer() {
        streamerOrNull = getStreamer()
        isConnected = true
    }

    fun echo(string: String): Boolean = streamerOrNull?.onNext(string)?.isSuccess ?: false

    fun disConnect() {
        streamerOrNull?.onCompleted()
        streamerOrNull = null
        isConnected = false
    }

    private fun getStreamer(): Streamer<String> {
        var isActive = true
        viewModelScope.launch {
            echos.transformAndUpdate { items ->
                items + ChatItemNotice(
                    message = "Connected",
                    type = NoticeType.Normal
                )
            }
        }
        val observer = stub.echo(
            stream {
                when (it) {
                    is StreamValue.Error -> {
                        viewModelScope.launch {
                            echos.transformAndUpdate { items ->
                                items + ChatItemNotice(
                                    message = "Disconnected with error ${it.error.getStatusCode()}",
                                    type = NoticeType.Error
                                )
                            }
                        }
                        disConnect()
                    }

                    is StreamValue.Complete -> {
                        viewModelScope.launch {
                            echos.transformAndUpdate { items ->
                                items + ChatItemNotice(
                                    message = "Disconnected",
                                    type = NoticeType.Normal
                                )
                            }
                        }
                        disConnect()
                    }

                    is StreamValue.Value -> {
                        viewModelScope.launch {
                            echos.transformAndUpdate { items ->
                                items + ChatItemMessage(
                                    gravity = ChatGravity.Left,
                                    shape = messageShapeDefault,
                                    text = it.value.message
                                )
                            }
                        }
                    }
                }
            }
        )
        return streamer(
            isActive = { isActive },
            onComplete = {
                observer.onCompleted()
            }
        ) {
            it.onValue {
                val echoRequest = EchoRequest.newBuilder().setMessage(it).build()
                observer.onNext(echoRequest)
                viewModelScope.launch {
                    echos.transformAndUpdate { items ->
                        items + ChatItemMessage(
                            gravity = ChatGravity.Right,
                            shape = messageShapeDefault,
                            text = echoRequest.message
                        )
                    }
                }
            }
        }
    }
}
