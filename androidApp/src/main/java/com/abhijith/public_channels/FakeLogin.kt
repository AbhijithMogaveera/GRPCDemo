package com.abhijith.public_channels

import com.abhijith.login_service.v1.LoginRequest

object FakeLogin {
    val FakeLoginRequest =
        LoginRequest("abhijith", "abhijith")
    val FakeToken:String = "<FAKE_TOKEN>"
}