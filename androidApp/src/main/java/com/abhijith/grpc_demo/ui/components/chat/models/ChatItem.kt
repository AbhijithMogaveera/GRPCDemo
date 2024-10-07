package com.abhijith.grpc_demo.ui.components.chat.models

import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import com.abhijith.grpc_demo.ui.components.chat.util.MessageShapeDefault
import java.util.UUID

sealed interface ChatItem {
    val key: String
}

data class ChatItemMessage(
    val gravity: ChatGravity,
    val text: AnnotatedString,
    val shape: Shape = MessageShapeDefault,
    val theme: ChatItemTheme = ChatItemTheme.NormalMessage,
    override val key: String = UUID.randomUUID().toString(),
) : ChatItem

fun ChatItemMessage(
    gravity: ChatGravity,
    text: String,
    shape: Shape = MessageShapeDefault,
    theme: ChatItemTheme = ChatItemTheme.NormalMessage,
    key: String = UUID.randomUUID().toString(),
) = ChatItemMessage(
    gravity,
    androidx.compose.ui.text.buildAnnotatedString { append(text) },
    shape,
    theme,
    key
)

data class ChatItemNotice(
    val message: String,
    val type: NoticeType,
    override val key: String = UUID.randomUUID().toString()
) : ChatItem

data class ChatItemSpace(
    val space: Dp,
    override val key: String = UUID.randomUUID().toString()
) : ChatItem

data class ChatItemSpaceAuto(
    val space: Dp,
    override val key: String = UUID.randomUUID().toString()
) : ChatItem

enum class ChatItemTheme {
    NormalMessage, ErrorMessage
}