package com.abhijith.public_channels.screens.server_stream

import com.abhijith.public_channels.rpc.GRPCClient
import com.abhijith.public_channels.rpc.StreamState
import com.example.weather.WeatherServiceGrpc
import com.example.weather.WeatherServiceProto
import com.example.weather.WeatherServiceProto.WeatherUpdate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class ServerStreamingRPCWeatherReport {

    private val stub = WeatherServiceGrpc.newBlockingStub(GRPCClient.channel)
    val streamingState = MutableStateFlow(StreamState.NO_STARTED)
    val weatherUpdates: MutableStateFlow<List<WeatherUpdate>> = MutableStateFlow(emptyList())

    suspend fun streamWeatherUpdates() {
        withContext(Dispatchers.IO) {
            weatherUpdates.emit(emptyList())
            stub.getWeatherUpdates(
                    WeatherServiceProto.WeatherRequest
                        .newBuilder()
                        .setLocation("Bangaluru")
                        .build()
                )
                .asFlow()
                .onStart { streamingState.emit(StreamState.ON_GOING) }
                .catch {
                    it.printStackTrace()
                    streamingState.emit(StreamState.ERROR)
                }
                .onEach { weatherUpdates.update { list -> (list + it).reversed() } }
                .collect()
            streamingState.emit(StreamState.END)
        }
    }
}