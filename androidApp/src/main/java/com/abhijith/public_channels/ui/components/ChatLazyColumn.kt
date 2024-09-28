package com.abhijith.public_channels.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

enum class ChatGravity {
    Left, Right
}

data class ChatItem(
    val gravity: ChatGravity,
    val text: String,
    val shape: Shape
)

@Composable
fun ChatScreen(
    chatItems: List<ChatItem>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        items(chatItems) { chatItem ->
            ChatBubble(chatItem)
        }
    }
}

@Composable
fun ChatBubble(
    chatItem: ChatItem,

    ) {
    val backgroundColor = if (chatItem.gravity == ChatGravity.Left) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
    val textColor = if (chatItem.gravity == ChatGravity.Left) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
        horizontalArrangement = if (chatItem.gravity == ChatGravity.Left) Arrangement.Start else Arrangement.End
    ) {
        Surface(
            shape = chatItem.shape,
            color = backgroundColor,
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = chatItem.text,
                color = textColor,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview
@Composable
fun PreviewChatScreen() {
    val sampleChatItems = listOf(
        ChatItem(ChatGravity.Left, "Hello!", CircleShape),
        ChatItem(ChatGravity.Left, "Hello!", CircleShape),
        ChatItem(ChatGravity.Right, "Hi there!", CircleShape),
        ChatItem(ChatGravity.Left, "How are you?", CircleShape),
        ChatItem(ChatGravity.Left, "How are you?", CircleShape),
        ChatItem(ChatGravity.Left, "How are you?", CircleShape),
        ChatItem(ChatGravity.Left, "How are you?", CircleShape),
        ChatItem(ChatGravity.Right, "I'm good, thanks!", CircleShape),
        ChatItem(ChatGravity.Right, "I'm good, thanks!", CircleShape),
        ChatItem(ChatGravity.Right, "I'm good, thanks!", CircleShape)
    )
    ChatScreen(sampleChatItems)
}
