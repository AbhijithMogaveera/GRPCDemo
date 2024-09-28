package com.abhijith.public_channels.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abhijith.public_channels.HelloResponse
import com.example.weather.WeatherServiceProto.WeatherUpdate

@Composable
fun DisplayResponse(
    response: HelloResponse?,
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
    response: WeatherUpdate,
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