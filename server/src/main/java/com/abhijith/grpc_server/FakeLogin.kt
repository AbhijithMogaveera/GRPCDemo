package com.abhijith.grpc_server

import com.abhijith.login_service.v1.LoginRequest

object FakeLogin {
    val FakeLoginRequest =
        LoginRequest(user_name = "abhijith", password = "abhijith")
    val FakeToken: String = "<FAKE_TOKEN>"
}