package com.abhijith.grpc_demo.ui.components.chat.util

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

val MessageShapeCenter = RoundedCornerShape(10.dp)
val MessageDefaultCornerSize = 15.dp
val MessageShapeDefault = RoundedCornerShape(MessageDefaultCornerSize)
val MessageShapeTopSent = MessageShapeDefault.copy(bottomEnd = CornerSize(MessageDefaultCornerSize * 0.3f))
val MessageShapeBottomSent = MessageShapeDefault.copy(topEnd = CornerSize(MessageDefaultCornerSize * 0.3f))
val MessageShapeTopReceived =
    MessageShapeDefault.copy(bottomStart = CornerSize(MessageDefaultCornerSize * 0.3f))
val messageShapeBottomReceived =
    MessageShapeDefault.copy(topStart = CornerSize(MessageDefaultCornerSize * 0.3f))