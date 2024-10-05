package com.abhijith.grpc_server.impl

import com.abhijith.weather_report_service.v1.WeatherReportServiceServer
import com.abhijith.weather_report_service.v1.WeatherUpdatesRequest
import com.abhijith.weather_report_service.v1.WeatherUpdatesResponse
import io.grpc.Status
import io.grpc.StatusException
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay

object WeatherService : WeatherReportServiceServer {
    private var throwError = false

    override suspend fun WeatherUpdates(
        request: WeatherUpdatesRequest,
        response: SendChannel<WeatherUpdatesResponse>
    ) {
        throwError = !throwError
        val location = request.location
        also {
            repeat(5) { i ->
                if (i == 2 && throwError) {
                    response.close(
                        StatusException(
                            Status.CANCELLED.withDescription("labores luctus possit persius voluptaria nascetur noluisse interdum dicat harum nobis nisl ubique in causae lobortis electram constituam alterum percipit")
                        )
                    )
                    return@also
                }
                val update = WeatherUpdatesResponse(
                    location = (location),
                    description = ("Sunny with clouds"),
                    temperature = (25.0 + i),
                    humidity = (60.0 - i),
                    wind_speed = (10.0 + i)
                )
                response.send(update)
                delay(500)
            }
        }
        response.close()
    }
}
