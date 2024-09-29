package com.abhijith.protos

import com.abhijith.login_service.v1.LoginRequest

object FakeLogin {
    val FakeLoginRequest =
        LoginRequest.newBuilder()
            .setUserName("abhijith")
            .setPassword("abhijith")
            .build()
    val FakeToken:String = "<FAKE_TOKEN>"
}