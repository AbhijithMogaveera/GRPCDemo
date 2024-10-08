import com.google.protobuf.gradle.id

plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.protobuf)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    sourceSets.getByName("main"){
        proto.srcDirs("$rootDir/proto/files")
    }
}

dependencies {
    implementation(libs.grpc.netty.shaded)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.stub)
    implementation(libs.javax.annotation.api)
    implementation(libs.grpc.kotlin.stub)
    implementation(libs.protobuf.java)
}

protobuf {
    protoc {
        artifact = libs.protoc.compiler.get().toString()
    }
    plugins {
        create("grpc") {
            artifact = libs.protoc.gen.grpc.java.get().toString()
        }
        create("grpckt") {
            artifact = libs.protoc.gen.grpc.kotlin.get().toString()
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