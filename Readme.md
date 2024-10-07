## Why grpc not Rest ..?
gRPC is faster than REST due to its use of HTTP/2 (supports multiplexing and binary framing), Protocol Buffers (compact, efficient serialization), and persistent connections. gRPC enables real-time streaming, reduces message size, and improves performance with lower latency and resource usage. REST, using HTTP/1.1 and JSON/XML, is slower due to text-based protocols and connection overhead. gRPC excels in microservices and high-performance applications

### Use branch [master](https://github.com/AbhijithMogaveera/GRPCDemo/tree/master) for traditional gRPC implementation
* on Android side this branch has `protobuf java lite` version implementation
* useful links [protocol-buffers-on-android](https://github.com/protocolbuffers/protobuf/blob/main/java/README.md#use-java-protocol-buffers-on-android), [Automate with Gradle plugin](https://github.com/google/protobuf-gradle-plugin), [protobuf.dev](https://protobuf.dev/)

### Use brnach [wire-based-kotlin-impl](https://github.com/AbhijithMogaveera/GRPCDemo/tree/wire-based-kotlin-impl) for Wire gRPC implementation
* You can explore more about wire and its added adwantage in [wire doc](https://square.github.io/wire/)

### Useful Plugins
* https://plugins.jetbrains.com/plugin/19147-buf-for-protocol-buffers

### Server
* Server code present inside `server` module, you can start your locally hosted server from [here](https://github.com/AbhijithMogaveera/GRPCDemo/blob/master/server/src/main/java/com/abhijith/grpc_server/Main.kt)

### Screenshots
* Unary RPC : Client sends a single request and receives a single response.
* Client Streaming : Client sends multiple requests, receiving a single response from the server after streaming ends.
* Server Streaming : Client sends a single request, and the server sends multiple responses streamed back to the client.
*  Bidirectional Streaming : Both client and server send streams of messages to each other, independent of one another.
<p align="center">
  <img src="https://github.com/user-attachments/assets/1d6cb443-a714-4fe6-98c9-63060b6d6dd1" alt="Image 1" width="45%"/>
  <img src="https://github.com/user-attachments/assets/6d6955d8-fdf6-4e8b-b6f9-1c376e4e99e1" alt="Image 2" width="45%"/>
</p>
<p align="center">
  <img src="https://github.com/user-attachments/assets/a7a9c7e6-afb7-482a-a70d-288edf2bc66d" alt="Image 1" width="45%"/>
  <img src="https://github.com/user-attachments/assets/d785ef11-99cd-451d-b8d0-7b1881ed210d" alt="Image 2" width="45%"/>
</p>
<p align="center">
  <img src="https://github.com/user-attachments/assets/9d4175ba-e40e-46f5-8a4c-3a959e927247" alt="Image 1" width="45%"/>
</p>
