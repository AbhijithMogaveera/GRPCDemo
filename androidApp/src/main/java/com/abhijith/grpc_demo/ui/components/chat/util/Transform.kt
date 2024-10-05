package com.abhijith.grpc_demo.ui.components.chat.util

import androidx.compose.ui.unit.dp
import com.abhijith.grpc_demo.ui.components.chat.models.ChatGravity
import com.abhijith.grpc_demo.ui.components.chat.models.ChatItem
import com.abhijith.grpc_demo.ui.components.chat.models.ChatItemMessage
import com.abhijith.grpc_demo.ui.components.chat.models.ChatItemSpaceAuto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext


fun transform(
    oldItems: List<ChatItem>,
    transform: (List<ChatItem>) -> List<ChatItem>
): List<ChatItem> {
    val chatItems = oldItems.filter { it !is ChatItemSpaceAuto }
    return transform(chatItems)
        .mapIndexed { index, chatItem ->
            if (chatItem is ChatItemMessage) {
                var newShape = MessageShapeCenter
                val previousChatItemOrNull =
                    chatItems.getOrNull(index - 1) as? ChatItemMessage
                val nextChatItemOrNull =
                    chatItems.getOrNull(index + 1) as? ChatItemMessage
                if (previousChatItemOrNull != null) {
                    if (previousChatItemOrNull.gravity == ChatGravity.Left) {
                        if (chatItem.gravity == ChatGravity.Left) {
                            newShape = MessageShapeCenter
                        } else {
                            newShape = MessageShapeTopSent
                        }
                    } else if (previousChatItemOrNull.gravity == ChatGravity.Right) {
                        if (chatItem.gravity == ChatGravity.Right) {
                            newShape = MessageShapeCenter
                        } else {
                            newShape = MessageShapeTopReceived
                        }
                    }
                }

                if (nextChatItemOrNull == null || nextChatItemOrNull.gravity != chatItem.gravity) {
                    newShape = when (chatItem.gravity) {
                        ChatGravity.Left -> messageShapeBottomReceived
                        ChatGravity.Right -> MessageShapeBottomSent
                    }
                }
                if (previousChatItemOrNull == null) {
                    newShape = when (chatItem.gravity) {
                        ChatGravity.Left -> MessageShapeTopReceived
                        ChatGravity.Right -> MessageShapeTopSent
                    }
                }
                chatItem.copy(shape = newShape)
            } else
                chatItem
        }
        .let {
            buildList {
                add(ChatItemSpaceAuto(15.dp))
                it.forEachIndexed { index, chatItem ->
                    val previousChatItemOrNull =
                        chatItems.getOrNull(index - 1) as? ChatItemMessage
                    if (previousChatItemOrNull?.gravity != (chatItem as? ChatItemMessage)?.gravity) {
                        add(ChatItemSpaceAuto(15.dp))
                    }
                    add(chatItem)
                }
                add(ChatItemSpaceAuto(15.dp))
            }
        }
}


suspend fun MutableStateFlow<List<ChatItem>>.transformAndUpdate(
    newList: (List<ChatItem>) -> List<ChatItem>
) {
    withContext(Dispatchers.Main) {
        update { items ->
            transform(items, newList)
        }
    }
}