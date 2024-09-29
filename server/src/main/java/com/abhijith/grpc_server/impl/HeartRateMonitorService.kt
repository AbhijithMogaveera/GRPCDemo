package com.abhijith.grpc_server.impl

import com.abhijith.heart_rate_service.v1.HeartRateMonitorProto
import com.abhijith.heart_rate_service.v1.HeartRateMonitorProto.MonitorHeartRateRequest
import com.abhijith.heart_rate_service.v1.HeartRateMonitorProto.MonitorHeartRateResponse
import com.abhijith.heart_rate_service.v1.HeartRateServiceGrpc.HeartRateServiceImplBase
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.stub.StreamObserver
import kotlin.random.Random


object HeartRateMonitorService : HeartRateServiceImplBase() {
    var throwError = Random.nextBoolean()

    override fun monitorHeartRate(responseObserver: StreamObserver<MonitorHeartRateResponse>): StreamObserver<HeartRateMonitorProto.MonitorHeartRateRequest> {
        var irregularHeartBeat = false
        throwError = !throwError
        return object : StreamObserver<MonitorHeartRateRequest> {
            var count = 0
            override fun onNext(heartRateRequest: MonitorHeartRateRequest) {
                val heartRate: Double = heartRateRequest.heartRate
                count++
                if (throwError && count == 2) {
                    responseObserver.onError(
                        StatusException(
                            Status.CANCELLED.withDescription("labores luctus possit persius voluptaria nascetur noluisse interdum dicat harum nobis nisl ubique in causae lobortis electram constituam alterum percipit")
                        )
                    )
                }
                irregularHeartBeat = heartRate !in (MIN_HEART_RATE..MAX_HEART_RATE)
            }

            override fun onError(t: Throwable) {}

            override fun onCompleted() {
                val response: MonitorHeartRateResponse =
                    MonitorHeartRateResponse.newBuilder()
                        .setMessage(if (irregularHeartBeat) "Heart rate is abnormal" else "Heart rate is normal.")
                        .setIsAnomaly(false)
                        .build()
                responseObserver.onNext(response)
                responseObserver.onCompleted()
            }
        }
    }

    private const val MAX_HEART_RATE = 100.0
    private const val MIN_HEART_RATE = 50.0
}
