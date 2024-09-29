package com.abhijith.public_channels.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext


val messageShapeCenter = RoundedCornerShape(10.dp)

val defaultCornerSize = 15.dp
val messageShapeDefault = RoundedCornerShape(defaultCornerSize)

val messageShapeTopSent = messageShapeDefault.copy(bottomEnd = CornerSize(defaultCornerSize * 0.3f))
val messageShapeBottomSent = messageShapeDefault.copy(topEnd = CornerSize(defaultCornerSize * 0.3f))

val messageShapeTopReceived =
    messageShapeDefault.copy(bottomStart = CornerSize(defaultCornerSize * 0.3f))
val messageShapeBottomReceived =
    messageShapeDefault.copy(topStart = CornerSize(defaultCornerSize * 0.3f))

suspend fun MutableStateFlow<List<ChatItem>>.transformAndUpdate(
    newList: (List<ChatItem>) -> List<ChatItem>
) {
    withContext(Dispatchers.Main) {
        update { items ->
            transform(items, newList)
        }
    }
}

private fun transform(
    oldItems: List<ChatItem>,
    transform: (List<ChatItem>) -> List<ChatItem>
): List<ChatItem> {
    val chatItems = oldItems.filter { it !is ChatItemSpace }
    return transform(chatItems)
        .mapIndexed { index, chatItem ->
            if (chatItem is ChatItemMessage) {
                var newShape = messageShapeCenter
                val previousChatItemOrNull =
                    chatItems.getOrNull(index - 1) as? ChatItemMessage
                val nextChatItemOrNull =
                    chatItems.getOrNull(index + 1) as? ChatItemMessage
                if (previousChatItemOrNull != null) {
                    if (previousChatItemOrNull.gravity == ChatGravity.Left) {
                        if (chatItem.gravity == ChatGravity.Left) {
                            newShape = messageShapeCenter
                        } else {
                            newShape = messageShapeTopSent
                        }
                    } else if (previousChatItemOrNull.gravity == ChatGravity.Right) {
                        if (chatItem.gravity == ChatGravity.Right) {
                            newShape = messageShapeCenter
                        } else {
                            newShape = messageShapeTopReceived
                        }
                    }
                }

                if (nextChatItemOrNull == null || nextChatItemOrNull.gravity != chatItem.gravity) {
                    newShape = when (chatItem.gravity) {
                        ChatGravity.Left -> messageShapeBottomReceived
                        ChatGravity.Right -> messageShapeBottomSent
                    }
                }
                if (previousChatItemOrNull == null) {
                    newShape = when (chatItem.gravity) {
                        ChatGravity.Left -> messageShapeTopReceived
                        ChatGravity.Right -> messageShapeTopSent
                    }
                }
                chatItem.copy(shape = newShape)
            } else
                chatItem
        }
        .let {
            buildList {
                add(ChatItemSpace(15.dp))
                it.forEachIndexed { index, chatItem ->
                    val previousChatItemOrNull =
                        chatItems.getOrNull(index - 1) as? ChatItemMessage
                    if (previousChatItemOrNull?.gravity != (chatItem as? ChatItemMessage)?.gravity) {
                        add(ChatItemSpace(15.dp))
                    }
                    add(chatItem)
                }
                add(ChatItemSpace(15.dp))
            }
        }
}


enum class ChatGravity {
    Left, Right
}

enum class ChatItemTheme {
    NormalMessage, ErrorMessage
}

sealed interface ChatItem

data class ChatItemMessage(
    val gravity: ChatGravity,
    val text: String,
    val shape: Shape,
    val theme: ChatItemTheme = ChatItemTheme.NormalMessage
) : ChatItem

enum class NoticeType {
    Normal, Error
}

data class ChatItemNotice(
    val message: String,
    val type: NoticeType
) : ChatItem

data class ChatItemSpace(
    val space: Dp
) : ChatItem

@Composable
fun ChatScreen(
    chatItems: List<ChatItem>, modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        items(chatItems) { chatItem ->
            when (chatItem) {
                is ChatItemMessage -> {
                    ChatBubble(chatItem)
                }

                is ChatItemNotice -> {
                    Notice(chatItem)
                }

                is ChatItemSpace -> {
                    Spacer(modifier = Modifier.height(chatItem.space))
                }
            }
        }
    }
}

@Composable
private fun Notice(chatItem: ChatItemNotice) {
    val space = 15.dp
    Column {
        Spacer(modifier = Modifier.height(space))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(space))

            val color = when (chatItem.type) {
                NoticeType.Normal -> MaterialTheme.colorScheme.primary
                NoticeType.Error -> MaterialTheme.colorScheme.error
            }
            Divider(
                thickness = 1.dp,
                modifier = Modifier.weight(1f),
                color = color
            )
            Text(
                text = chatItem.message,
                color = color,
                modifier = Modifier.padding(horizontal = space),
                textAlign = TextAlign.Center
            )
            Divider(
                thickness = 1.dp,
                modifier = Modifier.weight(1f),
                color = color
            )
            Spacer(modifier = Modifier.width(space))
        }
        Spacer(modifier = Modifier.height(space))
    }
}

@Composable
fun ChatBubble(
    chatItem: ChatItemMessage
) {
    val backgroundColor = getBgColor(chatItem)
    val textColor = getTextColor(chatItem)
    val horizontalArrangement = getHorizontalArrangement(chatItem)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpacerLeft(chatItem)
        Surface(
            shape = chatItem.shape,
            color = backgroundColor,
            modifier = Modifier
                .padding(1.dp)
                .defaultMinSize(minWidth = 100.dp)
                .weight(1f, false)
        ) {
            Text(
                text = chatItem.text,
                color = textColor,
                modifier = Modifier.padding(16.dp)
            )
        }
        SpacerRight(chatItem)
    }
}

@Composable
fun SpacerRight(chatItem: ChatItemMessage) {
    if (chatItem.gravity == ChatGravity.Left) {
        Spacer(modifier = Modifier.size(30.dp))
    }
}

@Composable
fun SpacerLeft(chatItem: ChatItemMessage) {
    if (chatItem.gravity == ChatGravity.Right) {
        Spacer(modifier = Modifier.size(30.dp))
    }
}

@Composable
private fun getHorizontalArrangement(chatItem: ChatItemMessage) =
    if (chatItem.gravity == ChatGravity.Left)
        Arrangement.Start
    else
        Arrangement.End

@Composable
private fun getTextColor(chatItem: ChatItemMessage): Color {
    return when (chatItem.theme) {
        ChatItemTheme.NormalMessage -> if (chatItem.gravity == ChatGravity.Left)
            MaterialTheme.colorScheme.onSecondary
        else
            MaterialTheme.colorScheme.onPrimary

        ChatItemTheme.ErrorMessage -> MaterialTheme.colorScheme.errorContainer
    }
}

@Composable
private fun getBgColor(chatItem: ChatItemMessage): Color {
    return when (chatItem.theme) {
        ChatItemTheme.NormalMessage -> {
            if (chatItem.gravity == ChatGravity.Left)
                MaterialTheme.colorScheme.secondary
            else
                MaterialTheme.colorScheme.primary
        }

        ChatItemTheme.ErrorMessage -> {
            MaterialTheme.colorScheme.onErrorContainer
        }
    }
}

@Preview
@Composable
fun PreviewChatScreen() {
    var chatItems by remember {
        mutableStateOf(emptyList<ChatItem>())
    }
    val list = buildList<ChatItem> {
        add(ChatItemNotice("demo", type = NoticeType.Error))
        add(
            ChatItemMessage(
                gravity = ChatGravity.Right,
                text = "Hello",
                shape = messageShapeDefault
            )
        )
        add(
            ChatItemMessage(
                gravity = ChatGravity.Right,
                text = "Hello",
                shape = messageShapeDefault
            )
        )
        add(
            ChatItemMessage(
                gravity = ChatGravity.Right,
                text = "Hello",
                shape = messageShapeDefault
            )
        )
        add(
            ChatItemMessage(
                gravity = ChatGravity.Left,
                text = "Hello",
                shape = messageShapeDefault
            )
        )
        add(
            ChatItemMessage(
                gravity = ChatGravity.Left,
                text = "Hello",
                shape = messageShapeDefault
            )
        )
        add(
            ChatItemMessage(
                gravity = ChatGravity.Left,
                text = "Hello",
                shape = messageShapeDefault
            )
        )
        add(ChatItemNotice("demo", type = NoticeType.Normal))
    }
    chatItems = transform(list, transform = { list })
    ChatScreen(chatItems)
}

@Preview
@Composable
fun PreviewChatScreen2() {
    var chatItems by remember {
        mutableStateOf(emptyList<ChatItem>())
    }
    val list = buildList<ChatItem> {
        add(ChatItemNotice("demo", type = NoticeType.Error))
        add(
            ChatItemMessage(
                gravity = ChatGravity.Right,
                text = "Hello",
                shape = messageShapeDefault
            )
        )
        add(
            ChatItemMessage(
                gravity = ChatGravity.Left,
                text = "Hello",
                shape = messageShapeDefault
            )
        )
        add(
            ChatItemMessage(
                gravity = ChatGravity.Left,
                text = "Hello",
                shape = messageShapeDefault
            )
        )
        add(ChatItemNotice("demo", type = NoticeType.Normal))
    }
    chatItems = transform(list, transform = { list })
    ChatScreen(chatItems)
}
