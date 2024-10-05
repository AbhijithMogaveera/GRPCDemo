package com.abhijith.grpc_demo.screens.server_stream

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhijith.public_channels.rpc.GRPCClientHelper
import com.abhijith.public_channels.rpc.StreamState
import com.abhijith.public_channels.rpc.getStatusCode
import com.abhijith.public_channels.ui.components.ChatGravity
import com.abhijith.public_channels.ui.components.ChatItem
import com.abhijith.public_channels.ui.components.ChatItemMessage
import com.abhijith.public_channels.ui.components.ChatItemNotice
import com.abhijith.public_channels.ui.components.ChatItemTheme
import com.abhijith.public_channels.ui.components.NoticeType
import com.abhijith.public_channels.ui.components.messageShapeDefault
import com.abhijith.public_channels.ui.components.transformAndUpdate
import com.abhijith.weather_report_service.v1.GrpcWeatherReportServiceClient
import com.abhijith.weather_report_service.v1.WeatherUpdatesRequest
import kotlinx.coroutines.CoroutineScope
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

class ServerStreamingViewmodel : ViewModel() {

    private val stub = GrpcWeatherReportServiceClient(GRPCClientHelper.client)
    val streamingState = MutableStateFlow(StreamState.NO_STARTED)
    val weatherUpdates: MutableStateFlow<List<ChatItem>> = MutableStateFlow(emptyList())
    private var callScope: CoroutineScope? = null

    fun streamWeatherUpdates() {

        callScope?.cancel()
        callScope = CoroutineScope(Dispatchers.IO + Job())

        val callScope = requireNotNull(callScope)
        var hasError = false

        callScope.launch {

            val (req, res) = stub.WeatherUpdates().executeIn(callScope)
            streamingState.emit(StreamState.ON_GOING)
            weatherUpdates.transformAndUpdate {
                it + ChatItemNotice("Server streaming started", type = NoticeType.Normal)
            }
            callScope.launch {
                res.consumeAsFlow()
                    .onEach { item->
                        callScope.launch {
                            weatherUpdates.transformAndUpdate {
                                it + ChatItemMessage(
                                    gravity = ChatGravity.Left,
                                    text = item.toString(),
                                    shape = messageShapeDefault
                                )
                            }
                        }
                    }.catch { err ->
                        hasError = true
                        viewModelScope.launch {
                            weatherUpdates.transformAndUpdate {
                                it + ChatItemMessage(
                                    gravity = ChatGravity.Left,
                                    text = err.message ?: "UnKnowError",
                                    shape = messageShapeDefault,
                                    theme = ChatItemTheme.ErrorMessage
                                ) + ChatItemNotice(
                                    "Server streaming ended with error \n${err.getStatusCode()}",
                                    type = NoticeType.Error
                                )
                            }
                        }
                        streamingState.emit(StreamState.ERROR)
                    }.collect()
                if (!hasError) {
                    viewModelScope.launch {
                        weatherUpdates.transformAndUpdate {
                            it + ChatItemNotice("Server streaming ended", type = NoticeType.Normal)
                        }
                    }
                }
                streamingState.emit(StreamState.END)
            }
            req.send(WeatherUpdatesRequest(location = "Bangaluru"))
            req.close()
        }
    }
}