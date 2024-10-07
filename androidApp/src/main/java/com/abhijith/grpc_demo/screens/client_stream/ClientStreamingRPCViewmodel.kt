package com.abhijith.grpc_demo.screens.client_stream

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhijith.grpc_demo.rpc.GRPCClientHelper
import com.abhijith.grpc_demo.rpc.Streamer
import com.abhijith.grpc_demo.rpc.getStatusCode
import com.abhijith.grpc_demo.rpc.streamer
import com.abhijith.grpc_demo.ui.components.chat.models.ChatGravity
import com.abhijith.grpc_demo.ui.components.chat.models.ChatItem
import com.abhijith.grpc_demo.ui.components.chat.models.ChatItemMessage
import com.abhijith.grpc_demo.ui.components.chat.models.ChatItemNotice
import com.abhijith.grpc_demo.ui.components.chat.models.ChatItemTheme
import com.abhijith.grpc_demo.ui.components.chat.models.NoticeType
import com.abhijith.grpc_demo.ui.components.chat.util.MessageShapeCenter
import com.abhijith.grpc_demo.ui.components.chat.util.transformAndUpdate
import com.abhijith.heart_rate_service.v1.GrpcHeartRateServiceClient
import com.abhijith.heart_rate_service.v1.MonitorHeartRateRequest
import com.abhijith.heart_rate_service.v1.MonitorHeartRateResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ViewModel responsible for managing client-side streaming of heart rate data
 * using gRPC. It handles sending heart rate data to the server and receiving
 * responses, updating the UI state accordingly.
 */
class ClientStreamingRPCViewmodel : ViewModel() {

    private val grpcHeartRateServiceClient = GrpcHeartRateServiceClient(GRPCClientHelper.client)

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
            publish.onNext(0.0).onFailure {
                return
            }
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
            streamBuilder.onNext(heartRate).onFailure {
                return
            }
            delay(300)
        }
        streamBuilder.onCompleted()
    }

    /**
     * Establishes a client-side gRPC stream for monitoring heart rate.
     *
     * @return A [Streamer] instance that allows sending heart rate data.
     */
    private suspend fun getHeartRatePublisher(): Streamer<Double> {

        // Notify UI that the client stream has started
        _chatItems.appendNotice("Client stream started", NoticeType.Normal)

        // Execute the gRPC call within the viewModelScope
        val (req, res) = grpcHeartRateServiceClient.MonitorHeartRate().executeIn(viewModelScope)

        var isActive = true

        viewModelScope.launch {
            var hasError = false
            res.consumeAsFlow()
                .onEach { response ->
                    _chatItems.appendResponse(response)
                }
                .catch { exception ->
                    _chatItems.appendError(exception)
                    hasError = true
                    isActive = false
                }
                .collect()

            if (!hasError) {
                _chatItems.appendNotice("Client stream ended", NoticeType.Normal)
            }
        }

        return streamer(
            isActive = { isActive },
            onComplete = { req.close() }
        ) { streamValue ->
            streamValue.onValue { value ->
                req.trySend(MonitorHeartRateRequest(value))
                _chatItems.appendHeartRateSent(value)
            }
        }
    }

    /**
     * Appends a chat message indicating that a heart rate value has been sent to the server.
     *
     * @param heartRate The heart rate value that was sent.
     */
    private fun MutableStateFlow<List<ChatItem>>.appendHeartRateSent(heartRate: Double) {
        appendChatItem(
            ChatItemMessage(
                gravity = ChatGravity.Right,
                text = "❤️ $heartRate sent to server",
                shape = MessageShapeCenter
            )
        )
    }



    /**
     * Appends a chat message based on the server's [MonitorHeartRateResponse].
     *
     * @param response The response received from the server.
     */
    private fun MutableStateFlow<List<ChatItem>>.appendResponse(response: MonitorHeartRateResponse) {
        appendChatItem(
            ChatItemMessage(
                gravity = ChatGravity.Left,
                text = response.message,
                shape = MessageShapeCenter
            )
        )
    }

    /**
     * Appends an error message to the chat items based on the encountered [Throwable].
     *
     * @param throwable The exception encountered during streaming.
     */
    private fun MutableStateFlow<List<ChatItem>>.appendError(throwable: Throwable) {
        val errorMessage = throwable.getStatusCode().let {
            "Client stream ended with error: $it"
        }

        appendNotice(errorMessage, NoticeType.Error)
        appendChatItem(
            ChatItemMessage(
                gravity = ChatGravity.Left,
                text = errorMessage,
                shape = MessageShapeCenter,
                theme = ChatItemTheme.ErrorMessage
            )
        )
    }

    /**
     * Appends a notice message to the chat items.
     *
     * @param message The notice message to append.
     * @param type The type of notice (e.g., Normal, Error).
     */
    private fun MutableStateFlow<List<ChatItem>>.appendNotice(message: String, type: NoticeType) {
        appendChatItem(
            ChatItemNotice(message = message, type = type)
        )
    }

    /**
     * Appends a [ChatItem] to the current list of chat items.
     *
     * @param chatItem The chat item to append.
     */
    private fun MutableStateFlow<List<ChatItem>>.appendChatItem(chatItem: ChatItem) {
        viewModelScope.launch {
            transformAndUpdate { it + chatItem }
        }
    }
}
