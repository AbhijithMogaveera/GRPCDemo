package com.abhijith.public_channels;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class GrpcClient {
    private final GreetingServiceGrpc.GreetingServiceBlockingStub blockingStub;
    private final GreetingServiceGrpc.GreetingServiceStub asyncStub;
    private final ManagedChannel channel;

    public GrpcClient(String host, int port) {
        // For development purposes, use plaintext. Use TLS in production.
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        blockingStub = GreetingServiceGrpc.newBlockingStub(channel);
        asyncStub = GreetingServiceGrpc.newStub(channel);
    }

    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }

    // Synchronous call
    public HelloResponse sayHello(String name) {
        HelloRequest request = HelloRequest.newBuilder()
                .setName(name)
                .build();
        return blockingStub.sayHello(request);
    }

    // Asynchronous call
    public void sayHelloAsync(String name, StreamObserver<HelloResponse> responseObserver) {
        HelloRequest request = HelloRequest.newBuilder()
                .setName(name)
                .build();
        asyncStub.sayHello(request, responseObserver);
    }
}
