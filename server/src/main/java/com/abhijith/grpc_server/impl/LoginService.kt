package com.abhijith.grpc_server.impl

import com.abhijith.grpc_server.FakeLogin
import com.abhijith.login_service.v1.LoginRequest
import com.abhijith.login_service.v1.LoginResponse
import com.abhijith.login_service.v1.LoginServiceGrpc
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.stub.StreamObserver

object LoginService:LoginServiceGrpc.LoginServiceImplBase(){
    override fun login(request: LoginRequest?, responseObserver: StreamObserver<LoginResponse>) {
        Thread.sleep(1000)
        if(request == FakeLogin.FakeLoginRequest){
            responseObserver.onNext(LoginResponse.newBuilder().setBToken(FakeLogin.FakeToken).build())
            responseObserver.onCompleted()
        }else{
            responseObserver.onError(StatusException(Status.UNAUTHENTICATED.withDescription("Failed authenticate user")))
        }
    }
}


