package com.abhijith.grpc_server.impl

import com.example.heartratemonitor.HeartRateMonitorGrpc
import com.example.heartratemonitor.HeartRateMonitorProto
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.stub.StreamObserver
import kotlin.random.Random


object HeartRateMonitorService : HeartRateMonitorGrpc.HeartRateMonitorImplBase() {
    var throwError = Random.nextBoolean()

    override fun streamHeartRate(
        responseObserver: StreamObserver<HeartRateMonitorProto.AnomalyResponse>
    ): StreamObserver<HeartRateMonitorProto.HeartRateRequest> {
        var irregularHeartBeat = false
        throwError = !throwError
        return object : StreamObserver<HeartRateMonitorProto.HeartRateRequest> {
            var count = 0
            override fun onNext(heartRateRequest: HeartRateMonitorProto.HeartRateRequest) {
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
                val response: HeartRateMonitorProto.AnomalyResponse =
                    HeartRateMonitorProto.AnomalyResponse.newBuilder()
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
