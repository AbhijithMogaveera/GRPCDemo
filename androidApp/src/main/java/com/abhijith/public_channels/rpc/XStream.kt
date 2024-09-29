package com.abhijith.public_channels.rpc

import io.grpc.stub.StreamObserver
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import io.grpc.StatusException
import io.grpc.StatusRuntimeException

sealed class StreamValue<T>() {

    class Complete<T>(val lastValue: T?) : StreamValue<T>()


    data class Value<T>(
        val value: T
    ) : StreamValue<T>()

    data class Error<T>(
        val error: Throwable,
        val lastValue: T?
    ) : StreamValue<T>() {
        val isStatusException = error is StatusException
        val isStatusRunTimeException = error is StatusRuntimeException
        inline fun onStatusException(action: (StatusException) -> Unit) {
            if (isStatusException) {
                (error as StatusException).apply(action)
            }
        }

        inline fun onStatusRuntimeException(action: (StatusRuntimeException) -> Unit) {
            if (isStatusRunTimeException) {
                (error as StatusRuntimeException).apply(action)
            }
        }
    }

    inline fun onValue(value: (T) -> Unit) {
        if (this is StreamValue.Value<T>) {
            value(this.value)
        }
    }
}

interface Streamer<T> {
    fun onNext(value: T): Result<Unit>
    fun onCompleted()
}

inline fun <T> streamer(
    crossinline isActive: () -> Boolean,
    crossinline onComplete: () -> Unit,
    crossinline onNext: (StreamValue<T>) -> Unit,
) = object : Streamer<T> {
    var lastValue: T? = null
    override fun onNext(value: T): Result<Unit> {
        lastValue = value
        return runCatching {
            require(isActive()) {
                "CLOSED! stream is no longer active"
            }
            onNext(StreamValue.Value(value))
        }
    }

    override fun onCompleted() {
        onComplete()
    }

}

inline fun <T> stream(
    crossinline onNextValue: (StreamValue<T>) -> Unit,
): StreamObserver<T> {
    var lastValue: T? = null
    return object : StreamObserver<T> {
        override fun onNext(response: T) {
            lastValue = response
            onNextValue(StreamValue.Value(response))
        }

        override fun onError(t: Throwable) {
            onNextValue(StreamValue.Error(t, lastValue))
        }

        override fun onCompleted() {
            onNextValue(StreamValue.Complete(lastValue))
        }
    }
}

fun <T> streamAsFlow(
    asyncCall: (StreamObserver<T>) -> Unit
): Flow<StreamValue<T>> = callbackFlow {
    var lastValue: T? = null
    val responseObserver = object : StreamObserver<T> {
        override fun onNext(value: T) {
            lastValue = value
            trySend(StreamValue.Value(value))
        }

        override fun onError(t: Throwable) {
            trySend(StreamValue.Error(t, lastValue))
        }

        override fun onCompleted() {
            trySend(StreamValue.Complete(lastValue))
        }
    }
    asyncCall(responseObserver)
    awaitClose {}
}

fun Throwable?.getStatusCode():String{
    if(this is StatusException){
        return status?.code?.toString()?:"UN_KNOW"
    }
    if(this is StatusRuntimeException){
        return status?.code?.toString()?:"UN_KNOW"
    }
    return "UN_KNOW"
}
