package com.abhijith.grpc_server.impl

import com.abhijith.heart_rate_service.v1.HeartRateServiceServer
import com.abhijith.heart_rate_service.v1.MonitorHeartRateRequest
import com.abhijith.heart_rate_service.v1.MonitorHeartRateResponse
import io.grpc.Status
import io.grpc.StatusException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlin.random.Random


object HeartRateMonitorService : HeartRateServiceServer {
    private var throwError = Random.nextBoolean()

    private const val MAX_HEART_RATE = 100.0
    private const val MIN_HEART_RATE = 50.0

    override suspend fun MonitorHeartRate(request: ReceiveChannel<MonitorHeartRateRequest>): MonitorHeartRateResponse {

        var irregularHeartBeat = false
        var count = 0

        throwError = !throwError

        request
            .receiveAsFlow()
            .map {
                it.heart_rate
            }.collect { heartRate ->
                if (throwError && count == 2) {
                    throw StatusException(
                        Status.CANCELLED.withDescription("labores luctus possit persius voluptaria nascetur noluisse interdum dicat harum nobis nisl ubique in causae lobortis electram constituam alterum percipit")
                    )
                }
                count++
                irregularHeartBeat = heartRate !in (MIN_HEART_RATE..MAX_HEART_RATE)
            }

        return MonitorHeartRateResponse(
            message = if (irregularHeartBeat) "Heart rate is abnormal" else "Heart rate is normal.",
            is_anomaly = false,
        )
    }
}
