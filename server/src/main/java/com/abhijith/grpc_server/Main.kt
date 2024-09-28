package com.abhijith.grpc_server


fun main() {
    val server = GrpcServer(8080)
    server.start()
    server.blockUntilShutdown()
}