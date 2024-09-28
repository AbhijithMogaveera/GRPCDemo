package com.abhijith.public_channels.ui.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TextInputFiled(
    userName: String,
    onInputChanges: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint:String
) {
    OutlinedTextField(
        value = userName,
        onValueChange = onInputChanges,
        modifier = modifier,
        shape = CircleShape,
        placeholder = {
            Text(text = hint)
        }
    )
}