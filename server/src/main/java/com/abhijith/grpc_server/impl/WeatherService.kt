package com.abhijith.grpc_server.impl

import com.example.weather.WeatherServiceGrpc.WeatherServiceImplBase
import com.example.weather.WeatherServiceProto.WeatherRequest
import com.example.weather.WeatherServiceProto.WeatherUpdate
import io.grpc.stub.StreamObserver

object WeatherService : WeatherServiceImplBase() {
    override fun getWeatherUpdates(
        request: WeatherRequest,
        responseObserver: StreamObserver<WeatherUpdate?>
    ) {
        val location = request.location

        for (i in 1..5) {
            val update = WeatherUpdate.newBuilder()
                .setLocation(location)
                .setDescription("Sunny with clouds")
                .setTemperature(25.0 + i)
                .setHumidity(60.0 - i)
                .setWindSpeed(10.0 + i)
                .build()


            responseObserver.onNext(update)

            try {
                Thread.sleep(5000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        responseObserver.onCompleted()
    }
}
