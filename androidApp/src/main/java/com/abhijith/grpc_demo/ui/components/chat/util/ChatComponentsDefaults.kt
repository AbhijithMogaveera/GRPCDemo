package com.abhijith.grpc_demo.ui.components.chat.util

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

val MessageShapeCenter = RoundedCornerShape(10.dp)
val MessageDefaultCornerSize = 15.dp
val MessageShapeDefault = RoundedCornerShape(MessageDefaultCornerSize)
val size = 10.dp//MessageDefaultCornerSize * 0.3f
val MessageShapeTopSent = MessageShapeDefault.copy(bottomEnd = CornerSize(size))
val MessageShapeBottomSent = MessageShapeDefault.copy(topEnd = CornerSize(size))
val MessageShapeTopReceived =
    MessageShapeDefault.copy(bottomStart = CornerSize(size))
val messageShapeBottomReceived =
    MessageShapeDefault.copy(topStart = CornerSize(size))