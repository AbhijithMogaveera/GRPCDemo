import com.google.protobuf.gradle.id

plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.protobuf)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    sourceSets.getByName("main") {
        proto.srcDirs("$rootDir/proto/files")
    }
}


dependencies {

    testImplementation(libs.junit)
    api(libs.kotlinx.coroutines.core)

    // Proto buffers
    api(libs.grpc.okhttp)
    api(libs.grpc.protobuf.lite)
    api(libs.grpc.stub)
    api(libs.grpc.kotlin.stub)
    api(libs.protobuf.java.lite)
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
            it.builtins {
                getByName("java") {
                    option("lite")
                }
            }
            it.plugins {
                id("grpc") {
                    option("lite")
                }
                id("grpckt")
            }
        }
    }
}