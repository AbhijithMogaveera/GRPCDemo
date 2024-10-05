package com.abhijith.grpc_demo.screens.bidiriectional_streaming

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhijith.echo_service.v1.EchoRequest
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

    private val stub: EchoServiceGrpc.EchoServiceStub = EchoServiceGrpc.newStub(com.abhijith.grpc_demo.rpc.GRPCClientHelper.channel)
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
                                    text = it.value.message
                                )
                            }
                        }
                    }
                }
            }
        )
        return streamer(
            isActive = { isConnected },
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
                            text = echoRequest.message
                        )
                    }
                }
            }
        }
    }
}
