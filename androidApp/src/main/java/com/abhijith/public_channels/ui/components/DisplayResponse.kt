package com.abhijith.public_channels.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.abhijith.greeting_service.v1.SayHelloResponse
import com.abhijith.weather_report_service.v1.WeatherUpdatesResponse
import com.squareup.wire.GrpcException

@Composable
fun DisplayResponse(
    response: SayHelloResponse?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.height(100.dp)) {
        AnimatedVisibility(
            visible = (response != null),
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val pointDownEmoji = "\uD83D\uDC47"
                Text(text = "Response from server $pointDownEmoji")
                Text(text = response?.message.toString())
            }
        }
    }
}

@Composable
fun DisplayResponse(
    response: WeatherUpdatesResponse,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Box(modifier = Modifier.padding(10.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(10.dp)
            ) {
                Text(text = response.toString())
            }
        }
    }
}


@Composable
fun DisplayError(
    throwable: GrpcException,
    retry: () -> Unit,
    modifier: Modifier,
    shape: Shape = RoundedCornerShape(16.dp)
) {
    ErrorScreen(
        errorMessage = throwable.grpcMessage ?: "An unknown error occurred",
        statusCode = throwable.grpcStatus.name,
        retry = retry,
        modifier = modifier,
        shape = shape
    )
}

@Composable
fun ErrorScreen(
    errorMessage: String,
    statusCode: String,
    retry: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp)
) {
    Card(
        shape =shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            // Highlighted error status code
            Text(
                text = "Error Code: $statusCode",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                ),
            )

            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .padding()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { retry() },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(text = "Retry")
            }
        }
    }
}

