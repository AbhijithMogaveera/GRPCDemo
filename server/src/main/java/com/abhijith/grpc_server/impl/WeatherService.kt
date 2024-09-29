package com.abhijith.grpc_server.impl

import com.example.weather.WeatherServiceGrpc.WeatherServiceImplBase
import com.example.weather.WeatherServiceProto.WeatherRequest
import com.example.weather.WeatherServiceProto.WeatherUpdate
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.stub.StreamObserver

object WeatherService : WeatherServiceImplBase() {
    var throwError = false
    override fun getWeatherUpdates(
        request: WeatherRequest,
        responseObserver: StreamObserver<WeatherUpdate?>
    ) {
        throwError = !throwError
        val location = request.location
        for (i in 1..5) {
            if (i == 2 && throwError) {
                responseObserver.onError(
                    StatusException(
                        Status.CANCELLED.withDescription("labores luctus possit persius voluptaria nascetur noluisse interdum dicat harum nobis nisl ubique in causae lobortis electram constituam alterum percipit")
                    )
                )
                return
            }
            val update = WeatherUpdate.newBuilder()
                .setLocation(location)
                .setDescription("Sunny with clouds")
                .setTemperature(25.0 + i)
                .setHumidity(60.0 - i)
                .setWindSpeed(10.0 + i)
                .build()
            responseObserver.onNext(update)
            Thread.sleep(500)
        }
        responseObserver.onCompleted()
    }
}
