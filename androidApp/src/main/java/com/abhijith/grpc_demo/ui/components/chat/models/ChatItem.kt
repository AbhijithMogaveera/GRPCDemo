package com.abhijith.grpc_demo.ui.components.chat.models

import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.abhijith.grpc_demo.ui.components.chat.util.MessageShapeDefault

sealed interface ChatItem

data class ChatItemMessage(
    val gravity: ChatGravity,
    val text: String,
    val shape: Shape = MessageShapeDefault,
    val theme: ChatItemTheme = ChatItemTheme.NormalMessage
) : ChatItem

data class ChatItemNotice(
    val message: String,
    val type: NoticeType
) : ChatItem

data class ChatItemSpace(
    val space: Dp
) : ChatItem

enum class ChatItemTheme {
    NormalMessage, ErrorMessage
}