
plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    id("com.squareup.wire") version "5.1.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}


dependencies {

    testImplementation(libs.junit)
    api(libs.kotlinx.coroutines.core)
//
//    api(libs.grpc.protobuf.lite)
//    api(libs.grpc.stub)
//    api(libs.grpc.kotlin.stub)
//    api(libs.protobuf.java.lite)

    // Proto buffers
    api(libs.grpc.okhttp)
    api(libs.wire.runtime)
    api(libs.wire.moshi.adapter)
    api(libs.wire.runtime)
    api(libs.wire.grpc.client)
}

wire {
    kotlin {
        proto{
            sourcePath("$rootDir/proto/files")
        }
        javaInterop = false
        rpcRole = "client"
        rpcCallStyle = "suspending"
    }
}