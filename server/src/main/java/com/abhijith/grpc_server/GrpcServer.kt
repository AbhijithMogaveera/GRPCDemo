package com.abhijith.grpc_server

import com.abhijith.echo_service.v1.EchoServiceWireGrpc
import com.abhijith.greeting_service.v1.GreetingServiceWireGrpc
import com.abhijith.grpc_server.impl.EchoService
import com.abhijith.grpc_server.impl.GreetingService
import com.abhijith.grpc_server.impl.HeartRateMonitorService
import com.abhijith.grpc_server.impl.LoginService
import com.abhijith.grpc_server.impl.WeatherService
import com.abhijith.heart_rate_service.v1.HeartRateServiceWireGrpc
import com.abhijith.login_service.v1.LoginServiceWireGrpc
import com.abhijith.weather_report_service.v1.WeatherReportServiceWireGrpc
import io.grpc.Server
import io.grpc.ServerBuilder
import java.io.IOException

class GrpcServer(private val port: Int) {
    
    private val server: Server = ServerBuilder.forPort(port)
        .intercept(AuthTokenInterceptor())
        .addService(LoginServiceWireGrpc.BindableAdapter(service = { LoginService }))
        .addService(GreetingServiceWireGrpc.BindableAdapter(service = { GreetingService })) /*unary*/
        .addService(WeatherReportServiceWireGrpc.BindableAdapter(service = { WeatherService })) /*server streaming*/
        .addService(HeartRateServiceWireGrpc.BindableAdapter(service = { HeartRateMonitorService })) /*client streaming*/
        .addService(EchoServiceWireGrpc.BindableAdapter(service = { EchoService })) /*bidirectional streaming*/
        .build()

    @Throws(IOException::class)
    fun start() {
        server.start()
        println("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(Thread {
            println("Shutting down gRPC server")
            this@GrpcServer.stop()
            println("Server shut down")
        })
    }

    fun stop() {
        server.shutdown()
    }

    @Throws(InterruptedException::class)
    fun blockUntilShutdown() {
        server.awaitTermination()
    }
}