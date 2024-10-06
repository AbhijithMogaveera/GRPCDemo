package com.abhijith.grpc_demo.ui.components.chat.models

import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.abhijith.grpc_demo.ui.components.chat.util.MessageShapeDefault
import java.util.UUID

sealed interface ChatItem{
    val key:String
}

data class ChatItemMessage(
    val gravity: ChatGravity,
    val text: String,
    val shape: Shape = MessageShapeDefault,
    val theme: ChatItemTheme = ChatItemTheme.NormalMessage,
    override val key: String = UUID.randomUUID().toString(),
) : ChatItem

data class ChatItemNotice(
    val message: String,
    val type: NoticeType,
    override val key: String = UUID.randomUUID().toString()
) : ChatItem

data class ChatItemSpace(
    val space:Dp,
    override val key: String = UUID.randomUUID().toString()
):ChatItem

data class ChatItemSpaceAuto(
    val space: Dp,
    override val key: String = UUID.randomUUID().toString()
) : ChatItem

enum class ChatItemTheme {
    NormalMessage, ErrorMessage
}