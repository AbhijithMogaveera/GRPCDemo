package com.abhijith.grpc_demo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abhijith.grpc_demo.ui.components.chat.util.MessageShapeDefault

@Composable
fun SecondaryButton(
    text: String,
    deleteAction: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor =
        if (deleteAction) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = contentColor,
        ),
        border = BorderStroke(2.dp, contentColor),
        modifier = modifier,
        shape = MessageShapeDefault
    ) {
        Row(
            modifier = Modifier.defaultMinSize(minHeight = 30.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text, modifier = Modifier,
                textAlign = TextAlign.Center
            )
        }

    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    isLoading: Boolean = false,
    modifier: Modifier,
    shape: Shape = CircleShape
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .defaultMinSize(minHeight = 25.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .size(25.dp)
                )
            }
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .size(25.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier,
                        strokeWidth = 2.dp
                    )
                }
            }
        }

    }
}
