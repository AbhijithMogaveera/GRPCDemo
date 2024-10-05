plugins {
    kotlin("jvm")
    id("com.squareup.wire") version "4.9.2"
}

group = "org.example"
version = "1.0-SNAPSHOT"
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    api(libs.squareup.wire.grpc.server)
    api(libs.kotlinx.coroutines.core)
    api(libs.wire.runtime)
    api(libs.wire.moshi.adapter)
    api(libs.wire.runtime)

    api(libs.grpc.kotlin.stub)
    api(libs.grpc.protobuf)
    api(libs.grpc.netty.shaded)

}

tasks.test {
    useJUnitPlatform()
}

wire {
    kotlin {
        proto{
            sourcePath("$rootDir/proto/files")
        }
        javaInterop = false
        rpcRole = "server"
        grpcServerCompatible = true
        singleMethodServices = false
        rpcCallStyle = "suspending"
    }
}