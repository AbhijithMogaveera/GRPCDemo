package com.abhijith.grpc_demo.screens.server_stream

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
                try {
                    _chatList.transformAndUpdate {
                        it+ChatItemMessage(
                            gravity = ChatGravity.Right,
                            text = "Give live updates about Bangaluru weather"
                        )
                    }
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
                                it + ChatItemNotice(
                                    "Server streaming started",
                                    type = NoticeType.Normal
                                )
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
                        .onEach { res: WeatherServiceProto.WeatherUpdatesResponse ->
                            _chatList.transformAndUpdate {
                                it + ChatItemMessage(
                                    gravity = ChatGravity.Left,
                                    text = buildAnnotatedString {
                                        append("Temperature: ")
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(
                                            0xFFFF9100
                                        )
                                        )) {
                                            append("${res.temperature}°C")
                                        }
                                        append("\n")

                                        // Humidity
                                        append("Humidity: ")
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(
                                            0xFF2979FF
                                        )
                                        )) {
                                            append("${res.humidity}%")
                                        }
                                        append("\n")

                                        // Wind Speed
                                        append("Wind Speed: ")
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(
                                            0xFF00E676
                                        )
                                        )) {
                                            append("${res.windSpeed} m/s")
                                        }
                                    },
                                )
                            }
                        }
                        .collect()
                } finally {
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
}