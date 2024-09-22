import com.google.protobuf.gradle.id

plugins {
    kotlin("jvm")
    alias(libs.plugins.protobuf)
}

group = "org.example"
version = "1.0-SNAPSHOT"

dependencies {
    api(libs.grpc.protobuf)
    api(libs.grpc.stub)
    api(libs.javax.annotation.api)
}

tasks.test {
    useJUnitPlatform()
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.21.12"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.56.0"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("grpc")
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