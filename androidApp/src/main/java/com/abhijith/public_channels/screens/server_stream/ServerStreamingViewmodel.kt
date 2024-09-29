package com.abhijith.public_channels.screens.server_stream

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhijith.public_channels.rpc.GRPCClient
import com.abhijith.public_channels.rpc.StreamState
import com.abhijith.public_channels.rpc.getStatusCode
import com.abhijith.public_channels.ui.components.ChatGravity
import com.abhijith.public_channels.ui.components.ChatItem
import com.abhijith.public_channels.ui.components.ChatItemMessage
import com.abhijith.public_channels.ui.components.ChatItemNotice
import com.abhijith.public_channels.ui.components.NoticeType
import com.abhijith.public_channels.ui.components.messageShapeDefault
import com.abhijith.public_channels.ui.components.transformAndUpdate
import com.example.weather.WeatherServiceGrpc
import com.example.weather.WeatherServiceProto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ServerStreamingViewmodel : ViewModel() {

    private val stub = WeatherServiceGrpc.newBlockingStub(GRPCClient.channel)
    val streamingState = MutableStateFlow(StreamState.NO_STARTED)
    val weatherUpdates: MutableStateFlow<List<ChatItem>> = MutableStateFlow(emptyList())

    suspend fun streamWeatherUpdates() {
        var hasError = false
        withContext(Dispatchers.IO) {
            stub.getWeatherUpdates(
                WeatherServiceProto.WeatherRequest
                    .newBuilder()
                    .setLocation("Bangaluru")
                    .build()
            )
                .asFlow()
                .onStart {
                    streamingState.emit(StreamState.ON_GOING)
                    weatherUpdates.transformAndUpdate {
                        it + ChatItemNotice("Server streaming started", type = NoticeType.Normal)
                    }
                }
                .catch { err ->
                    hasError = true
                    viewModelScope.launch {
                        weatherUpdates.transformAndUpdate {
                            it + ChatItemNotice(
                                "Server streaming ended with error \n${err.getStatusCode()}",
                                type = NoticeType.Error
                            )
                        }
                    }
                    streamingState.emit(StreamState.ERROR)
                }
                .onEach { res ->
                    viewModelScope.launch {
                        weatherUpdates.transformAndUpdate {
                            it + ChatItemMessage(
                                gravity = ChatGravity.Left,
                                text = res.toString(),
                                shape = messageShapeDefault
                            )
                        }
                    }
                }
                .collect()
            if (!hasError) {
                viewModelScope.launch {
                    weatherUpdates.transformAndUpdate {
                        it + ChatItemNotice("Server streaming ended", type = NoticeType.Normal)
                    }
                }
            }
            streamingState.emit(StreamState.END)
        }
    }
}