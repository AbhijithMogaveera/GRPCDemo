package com.abhijith.grpc_demo.screens.bidiriectional_streaming

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhijith.echo_service.v1.EchoRequest
import com.abhijith.echo_service.v1.EchoResponse
import com.abhijith.echo_service.v1.GrpcEchoServiceClient
import com.abhijith.grpc_demo.rpc.GRPCClientHelper
import com.abhijith.grpc_demo.rpc.Streamer
import com.abhijith.grpc_demo.rpc.getStatusCode
import com.abhijith.grpc_demo.rpc.streamer
import com.abhijith.grpc_demo.ui.components.chat.models.ChatGravity
import com.abhijith.grpc_demo.ui.components.chat.models.ChatItem
import com.abhijith.grpc_demo.ui.components.chat.models.ChatItemMessage
import com.abhijith.grpc_demo.ui.components.chat.models.ChatItemNotice
import com.abhijith.grpc_demo.ui.components.chat.models.NoticeType
import com.abhijith.grpc_demo.ui.components.chat.util.MessageShapeDefault
import com.abhijith.grpc_demo.ui.components.chat.util.transformAndUpdate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing bidirectional streaming of echo messages
 * using gRPC. It handles sending messages to the server and receiving responses,
 * updating the UI state accordingly.
 */
class BidirectionalStreamingRPCViewmodel : ViewModel() {

    private val _chatItems = MutableStateFlow<List<ChatItem>>(emptyList())

    val chatItems = _chatItems.asStateFlow()

    /**
     * Indicates whether the client is currently connected to the server.
     * Used to control UI elements like connection buttons.
     */
    var isConnected: Boolean by mutableStateOf(false)
        private set

    /**
     * gRPC client for interacting with the Echo Service.
     */
    private val grpcEchoServiceClient = GrpcEchoServiceClient(GRPCClientHelper.client)

    /**
     * Coroutine scope for managing the streaming connection.
     * Initialized when connecting and canceled upon disconnection.
     */
    private var connectionScope: CoroutineScope? = null

    /**
     * Streamer instance for managing the bidirectional stream.
     * Used to send messages to the server.
     */
    private var streamerOrNull: Streamer<String>? = null

    /**
     * Establishes a bidirectional streaming connection to the server.
     * If already connected, it first disconnects before establishing a new connection.
     */
    fun connectToServer() {
        disConnect()
        // Initialize a new coroutine scope for the connection
        connectionScope = CoroutineScope(Job() + Dispatchers.IO)
        // Initialize the streamer for sending messages
        streamerOrNull = getStreamer()
        // Update the connection state
        isConnected = true
    }

    /**
     * Sends an echo message to the server.
     *
     * @param string The message to send.
     * @return `true` if the message was successfully sent; `false` otherwise.
     */
    fun echo(string: String): Boolean = streamerOrNull?.onNext(string)?.isSuccess ?: false

    /**
     * Disconnects the current streaming connection to the server.
     * If not connected, the method returns immediately.
     * Properly closes the stream and cancels the coroutine scope to release resources.
     */
    fun disConnect() {
        if (!isConnected) {
            return
        }
        // Complete the streamer by signaling the end of the stream
        streamerOrNull?.onCompleted()
        streamerOrNull = null
        // Update the connection state
        isConnected = false
        // Cancel the coroutine scope to stop any ongoing operations
        connectionScope?.cancel()
        connectionScope = null
    }

    /**
     * Establishes a streamer for bidirectional communication with the server.
     * Handles sending messages and receiving responses, updating the chat items accordingly.
     *
     * @return A [Streamer] instance for sending messages.
     */
    private fun getStreamer(): Streamer<String> {
        // Ensure that the connection scope is initialized
        val callScope = requireNotNull(connectionScope) {
            "Connection scope must not be null when initializing streamer."
        }

        // Notify the UI that the connection has been established
        viewModelScope.launch {
            _chatItems.appendNotice("Connected", NoticeType.Normal)
        }

        // Execute the Echo RPC within the connection scope
        val (req, res) = grpcEchoServiceClient.Echo().executeIn(callScope)

        // Flag to track the active state of the stream
        var isActive = true

        // Launch a coroutine to handle incoming responses from the server
        callScope.launch {
            var hasError = false
            try {
                res
                    .receiveAsFlow()
                    .onEach { response ->
                        _chatItems.appendReceivedMessage(response)
                    }
                    .catch { exception ->
                        _chatItems.appendDisconnectionWithError(exception)
                        disConnect()
                        hasError = true
                    }
                    .collect()
            } finally {
                if (!hasError) {
                    _chatItems.appendNormalTerminationMessage()
                }
                isActive = false
            }
        }

        // Return a Streamer that manages sending messages to the server
        return streamer(
            isActive = { isActive },
            onComplete = { req.close() }
        ) { streamValue ->
            streamValue.onValue { message ->
                // Create an EchoRequest with the message
                val echoRequest = EchoRequest(message = message)
                // Launch a coroutine to send the message to the server
                callScope.launch {
                    req.send(echoRequest)
                }
                // Append the sent message to the chat items
                _chatItems.appendSentMessage(echoRequest)
            }
        }
    }

    /**
     * Appends a received message from the server to the chat items.
     *
     * @param response The [EchoResponse] received from the server.
     */
    private fun MutableStateFlow<List<ChatItem>>.appendReceivedMessage(response: EchoResponse) {
        viewModelScope.launch {
            transformAndUpdate { items ->
                items + ChatItemMessage(
                    gravity = ChatGravity.Left,
                    shape = MessageShapeDefault,
                    text = response.message
                )
            }
        }
    }

    /**
     * Appends a disconnection notice with an error message to the chat items.
     *
     * @param throwable The [Throwable] that caused the disconnection.
     */
    private suspend fun MutableStateFlow<List<ChatItem>>.appendDisconnectionWithError(throwable: Throwable) {
        val errorMessage = throwable.getStatusCode().let {
            "Disconnected with error: $it"
        }

        // Append an error notice to the chat items
        transformAndUpdate { items ->
            items + ChatItemNotice(
                message = errorMessage,
                type = NoticeType.Error
            )
        }
    }

    /**
     * Appends a normal termination notice to the chat items.
     * Indicates that the connection has been closed without errors.
     */
    private fun MutableStateFlow<List<ChatItem>>.appendNormalTerminationMessage() {
        viewModelScope.launch {
            transformAndUpdate { items ->
                items + ChatItemNotice(
                    message = "Disconnected",
                    type = NoticeType.Normal
                )
            }
        }
    }

    /**
     * Appends a sent message to the chat items.
     *
     * @param echoRequest The [EchoRequest] that was sent to the server.
     */
    private fun MutableStateFlow<List<ChatItem>>.appendSentMessage(echoRequest: EchoRequest) {
        viewModelScope.launch {
            transformAndUpdate { items ->
                items + ChatItemMessage(
                    gravity = ChatGravity.Right,
                    text = echoRequest.message
                )
            }
        }
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
     * Appends a generic [ChatItem] to the chat items.
     *
     * @param chatItem The [ChatItem] to append.
     */
    private fun MutableStateFlow<List<ChatItem>>.appendChatItem(chatItem: ChatItem) {
        viewModelScope.launch {
            transformAndUpdate { it + chatItem }
        }
    }

    // ===========================
    //        Lifecycle
    // ===========================

    /**
     * Called when the ViewModel is no longer used and will be destroyed.
     * Ensures that any active streaming connections are properly disconnected
     * to prevent memory leaks.
     */
    override fun onCleared() {
        super.onCleared()
        disConnect()
    }
}
