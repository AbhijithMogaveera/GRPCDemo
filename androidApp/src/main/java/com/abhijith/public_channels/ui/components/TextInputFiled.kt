package com.abhijith.public_channels.ui.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

@Composable
fun TextInputFiled(
    userName: String,
    onInputChanges: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint:String,
    shape: Shape = CircleShape
) {
    OutlinedTextField(
        value = userName,
        onValueChange = onInputChanges,
        modifier = modifier,
        shape = shape,
        placeholder = {
            Text(text = hint)
        }
    )
}