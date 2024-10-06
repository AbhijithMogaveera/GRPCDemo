package com.abhijith.grpc_demo.screens.server_stream

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhijith.grpc_demo.rpc.StreamState
import com.abhijith.grpc_demo.rpc.getStatusCode
import com.abhijith.grpc_demo.ui.components.chat.models.ChatGravity
import com.abhijith.grpc_demo.ui.components.chat.models.ChatItem
import com.abhijith.grpc_demo.ui.components.chat.models.ChatItemMessage
import com.abhijith.grpc_demo.ui.components.chat.models.ChatItemNotice
import com.abhijith.grpc_demo.ui.components.chat.models.NoticeType
import com.abhijith.grpc_demo.ui.components.chat.util.transformAndUpdate
import com.abhijith.weather_report_service.v1.WeatherReportServiceGrpc
import com.abhijith.weather_report_service.v1.WeatherServiceProto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ServerStreamingViewmodel : ViewModel() {

    private val stub =
        WeatherReportServiceGrpc.newBlockingStub(com.abhijith.grpc_demo.rpc.GRPCClientHelper.channel)
    val streamingState = MutableStateFlow(StreamState.NO_STARTED)

    private val _chatList: MutableStateFlow<List<ChatItem>> = MutableStateFlow(emptyList())

    val chatList = _chatList.asStateFlow()


    fun streamWeatherUpdates() {
        viewModelScope.launch {
            var hasError = false
            withContext(Dispatchers.IO) {
                stub.weatherUpdates(
                    WeatherServiceProto.WeatherUpdatesRequest
                        .newBuilder()
                        .setLocation("Bangaluru")
                        .build()
                )
                    .asFlow()
                    .onStart {
                        streamingState.emit(StreamState.ON_GOING)
                        _chatList.transformAndUpdate {
                            it + ChatItemNotice("Server streaming started", type = NoticeType.Normal)
                        }
                    }
                    .catch { err ->
                        hasError = true
                        _chatList.transformAndUpdate {
                            it + ChatItemNotice(
                                "Server streaming ended with error \n${err.getStatusCode()}",
                                type = NoticeType.Error
                            )
                        }
                        streamingState.emit(StreamState.ERROR)
                    }
                    .onEach { res ->
                        _chatList.transformAndUpdate {
                            it + ChatItemMessage(
                                gravity = ChatGravity.Left,
                                text = res.toString(),
                            )
                        }
                    }
                    .collect()
                if (!hasError) {
                    _chatList.transformAndUpdate {
                        it + ChatItemNotice("Server streaming ended", type = NoticeType.Normal)
                    }
                }
                streamingState.emit(StreamState.END)
            }
        }
    }
}