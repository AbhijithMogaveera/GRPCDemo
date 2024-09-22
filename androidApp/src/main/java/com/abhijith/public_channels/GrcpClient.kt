package com.abhijith.public_channels

import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver


class GrcpClient {
    fun connectToServer() {
        val channel = ManagedChannelBuilder.forAddress("your_server_ip", 50051)
            .usePlaintext()
            .build()

        val greetingService = GreetingServiceGrpc.newStub(channel)

        val request = HelloRequest.newBuilder().setName("World").build()
        greetingService.sayHello(request, object :StreamObserver<HelloResponse>{
            override fun onNext(value: HelloResponse?) {

            }

            override fun onError(t: Throwable?) {
            }

            override fun onCompleted() {

            }
        })
    }
}