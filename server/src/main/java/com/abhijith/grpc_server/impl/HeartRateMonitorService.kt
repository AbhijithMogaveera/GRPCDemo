package com.abhijith.grpc_server.impl

import com.example.heartratemonitor.HeartRateMonitorGrpc
import com.example.heartratemonitor.HeartRateMonitorProto
import io.grpc.stub.StreamObserver


object HeartRateMonitorService : HeartRateMonitorGrpc.HeartRateMonitorImplBase() {

    override fun streamHeartRate(
        responseObserver: StreamObserver<HeartRateMonitorProto.AnomalyResponse>
    ): StreamObserver<HeartRateMonitorProto.HeartRateRequest> {
        var irregularHeartBeat = false
        return object : StreamObserver<HeartRateMonitorProto.HeartRateRequest> {
            override fun onNext(heartRateRequest: HeartRateMonitorProto.HeartRateRequest) {
                val heartRate: Double = heartRateRequest.heartRate
                println("Received heart rate: $heartRate")
                irregularHeartBeat = heartRate !in (MIN_HEART_RATE..MAX_HEART_RATE)
            }

            override fun onError(t: Throwable) {
                System.err.println("Error in stream: " + t.message)
            }

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
