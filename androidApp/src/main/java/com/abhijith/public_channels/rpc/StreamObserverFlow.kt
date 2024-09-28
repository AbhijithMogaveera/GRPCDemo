package com.abhijith.public_channels.rpc

import io.grpc.stub.StreamObserver
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun <T> streamObserverFlow(
    asyncCall: (StreamObserver<T>) -> Unit
): Flow<Result<T>> = callbackFlow {
    val responseObserver = object : StreamObserver<T> {
        override fun onNext(value: T) { trySend(Result.success(value)) }
        override fun onError(t: Throwable) { trySend(Result.failure(t)) }
        override fun onCompleted() { close() }
    }
    asyncCall(responseObserver)
    awaitClose {}
}
