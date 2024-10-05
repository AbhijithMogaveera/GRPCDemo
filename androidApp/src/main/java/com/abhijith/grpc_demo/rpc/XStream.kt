package com.abhijith.grpc_demo.rpc

import com.squareup.wire.GrpcException

sealed class StreamValue<T>() {

    class Complete<T>(val lastValue: T?) : StreamValue<T>()

    data class Value<T>(
        val value: T
    ) : StreamValue<T>()

    data class Error<T>(
        val error: Throwable,
        val lastValue: T?
    ) : StreamValue<T>() {

        val isStatusException = error is GrpcException

        inline fun onGrpcException(action: (GrpcException) -> Unit) {
            if (isStatusException) {
                (error as GrpcException).apply(action)
            }
        }
    }

    inline fun onValue(value: (T) -> Unit) {
        if (this is Value<T>) {
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

fun Throwable?.getStatusCode():String{
    if(this is GrpcException){
        return grpcStatus.name
    }
    return "UN_KNOW"
}
