package com.abhijith.grpc_server.impl

import com.abhijith.weather_report_service.v1.WeatherReportServiceGrpc
import com.abhijith.weather_report_service.v1.WeatherServiceProto.WeatherUpdatesRequest
import com.abhijith.weather_report_service.v1.WeatherServiceProto.WeatherUpdatesResponse
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.stub.StreamObserver

object WeatherService : WeatherReportServiceGrpc.WeatherReportServiceImplBase() {
    private var throwError = false
    override fun weatherUpdates(
        request: WeatherUpdatesRequest,
        responseObserver: StreamObserver<WeatherUpdatesResponse?>
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
            val update = WeatherUpdatesResponse.newBuilder()
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
