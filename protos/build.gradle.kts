import com.google.protobuf.gradle.id

plugins {
    kotlin("jvm")
    alias(libs.plugins.protobuf)
}

group = "org.example"
version = "1.0-SNAPSHOT"

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.grpc.protobuf)
    api(libs.grpc.stub)
    api(libs.javax.annotation.api)
    api(libs.grpc.kotlin.stub)
}

tasks.test {
    useJUnitPlatform()
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.21.12"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.56.0"
        }
        create("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.4.1:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            if (it.name.startsWith("generateTestProto")) {
                it.dependsOn("jar")
            }

            it.plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}
tasks {
    named("compileJava") {
        dependsOn("generateProto")
    }

    named("compileTestJava") {
        dependsOn("generateTestProto")
    }
}