package com.abhijith.grpc_demo

import com.abhijith.login_service.v1.LoginRequest

object FakeLogin {
    val FakeLoginRequest =
        LoginRequest("abhijith", "abhijith")
}