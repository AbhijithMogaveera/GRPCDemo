package com.abhijith.grpc_demo.screens.server_stream

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhijith.grpc_demo.rpc.GRPCClientHelper
import com.abhijith.grpc_demo.rpc.StreamState
import com.abhijith.grpc_demo.rpc.getStatusCode
import com.abhijith.grpc_demo.ui.components.chat.models.ChatGravity
import com.abhijith.grpc_demo.ui.components.chat.models.ChatItem
import com.abhijith.grpc_demo.ui.components.chat.models.ChatItemMessage
import com.abhijith.grpc_demo.ui.components.chat.models.ChatItemNotice
import com.abhijith.grpc_demo.ui.components.chat.models.ChatItemTheme
import com.abhijith.grpc_demo.ui.components.chat.models.NoticeType
import com.abhijith.grpc_demo.ui.components.chat.util.MessageShapeDefault
import com.abhijith.grpc_demo.ui.components.chat.util.transformAndUpdate
import com.abhijith.weather_report_service.v1.GrpcWeatherReportServiceClient
import com.abhijith.weather_report_service.v1.WeatherUpdatesRequest
import com.abhijith.weather_report_service.v1.WeatherUpdatesResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing server-streamed weather updates
 * and updating the UI's chat interface accordingly.
 *
 * This ViewModel interacts with the [GrpcWeatherReportServiceClient] to
 * initiate and manage a gRPC server-streaming call. It maintains the
 * state of the streaming process and updates a list of [ChatItem]s to
 * reflect the current status and received messages.
 */
class ServerStreamingViewmodel : ViewModel() {

    private val grpcWeatherReportServiceClient =
        GrpcWeatherReportServiceClient(GRPCClientHelper.client)


    val streamingState = MutableStateFlow(StreamState.NO_STARTED)

    private val _chatList: MutableStateFlow<List<ChatItem>> = MutableStateFlow(emptyList())

    val chatList = _chatList.asStateFlow()

    private var callScope: CoroutineScope? = null

    /**
     * Initiates the server-streaming of weather updates.
     *
     * This function performs the following steps:
     * 1. Cancels any existing streaming calls.
     * 2. Creates a new coroutine scope for the streaming process.
     * 3. Launches a coroutine to handle the streaming lifecycle.
     * 4. Sends a [WeatherUpdatesRequest] to the server.
     *
     * Updates the [streamingState] and [chatList] based on the streaming progress and received messages.
     */
    fun streamWeatherUpdates() {

        callScope?.cancel()
        callScope = CoroutineScope(Dispatchers.IO + Job())

        val callScope = requireNotNull(callScope)

        callScope.launch {
            var hasError = false
            val (req, res) = grpcWeatherReportServiceClient.WeatherUpdates().executeIn(callScope)
            streamingState.emit(StreamState.ON_GOING)
            _chatList.appendServerStreamingStartedMessage()
            callScope.launch {
                try {
                    res.consumeAsFlow()
                        .onEach { item ->
                            _chatList.appendReceivedMessage(item)
                        }.catch { err ->
                            hasError = true
                            handleError(err)
                            streamingState.emit(StreamState.ERROR)
                        }.collect()
                } finally {
                    if (!hasError) {
                        _chatList.appendPeacefulTermination()
                    }
                    streamingState.emit(StreamState.END)
                }
            }
            req.send(WeatherUpdatesRequest(location = "Bangaluru"))
            req.close()
        }
    }

    /**
     * Appends a notice message indicating that server streaming has started.
     *
     * This is an extension function on [MutableStateFlow] holding a list of [ChatItem]s.
     */
    private suspend fun MutableStateFlow<List<ChatItem>>.appendServerStreamingStartedMessage() {
        transformAndUpdate {
            it + ChatItemNotice("Server streaming started", type = NoticeType.Normal)
        }
    }

    /**
     * Appends a notice message indicating that server streaming has ended peacefully.
     *
     * This is an extension function on [MutableStateFlow] holding a list of [ChatItem]s.
     */
    private fun MutableStateFlow<List<ChatItem>>.appendPeacefulTermination() {
        viewModelScope.launch {
            transformAndUpdate {
                it + ChatItemNotice("Server streaming ended", type = NoticeType.Normal)
            }
        }
    }

    /**
     * Handles errors that occur during the streaming process.
     *
     * This function appends an error message and a notice to the chat list,
     * reflecting the error encountered.
     *
     * @param err The [Throwable] representing the error.
     */
    private fun handleError(err: Throwable) {
        viewModelScope.launch {
            _chatList.transformAndUpdate {
                it + ChatItemMessage(
                    gravity = ChatGravity.Left,
                    text = err.message ?: "Unknown Error",
                    shape = MessageShapeDefault,
                    theme = ChatItemTheme.ErrorMessage
                ) + ChatItemNotice(
                    "Server streaming ended with error \n${err.getStatusCode()}",
                    type = NoticeType.Error
                )
            }
        }
    }

    /**
     * Appends a received weather update message to the chat list.
     *
     * This is an extension function on [MutableStateFlow] holding a list of [ChatItem]s.
     *
     * @param item The [WeatherUpdatesResponse] received from the server.
     */
    private fun MutableStateFlow<List<ChatItem>>.appendReceivedMessage(item: WeatherUpdatesResponse) {
        viewModelScope.launch {
            transformAndUpdate {
                it + ChatItemMessage(
                    gravity = ChatGravity.Left,
                    text = item.toString(),
                    shape = MessageShapeDefault
                )
            }
        }
    }
}
