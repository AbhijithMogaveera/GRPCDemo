package com.abhijith.grpc_server.impl

import com.abhijith.grpc_server.FakeLogin
import com.abhijith.login_service.v1.LoginRequest
import com.abhijith.login_service.v1.LoginResponse
import com.abhijith.login_service.v1.LoginServiceServer
import io.grpc.Status
import io.grpc.StatusException
import kotlinx.coroutines.delay

object LoginService : LoginServiceServer {

    override suspend fun Login(request: LoginRequest): LoginResponse {
        delay(1000)
        if (request == FakeLogin.FakeLoginRequest) {
            return LoginResponse(b_token = FakeLogin.FakeToken);
        }
        throw StatusException(Status.UNAUTHENTICATED.withDescription("Failed authenticate user"))
    }

}



