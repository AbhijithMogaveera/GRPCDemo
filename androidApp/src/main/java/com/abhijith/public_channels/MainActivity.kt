package com.abhijith.public_channels

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhijith.login_service.v1.LoginServiceGrpc
import com.abhijith.public_channels.rpc.GRPCClient
import com.abhijith.public_channels.rpc.StreamValue
import com.abhijith.public_channels.rpc.authToken
import com.abhijith.public_channels.rpc.getStatusCode
import com.abhijith.public_channels.rpc.stream
import com.abhijith.public_channels.screens.bidiriectional_streaming.BidirectionalStreamingRPCActivity
import com.abhijith.public_channels.screens.client_stream.ClientStreamingRPCActivity
import com.abhijith.public_channels.screens.server_stream.ServerStreamingRPCActivity
import com.abhijith.public_channels.screens.unary.UnaryRPCActivity
import com.abhijith.public_channels.ui.components.PrimaryButton
import com.abhijith.public_channels.ui.theme.ShoppingCatalogueTheme
import io.grpc.Deadline
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
class MainActivity : ComponentActivity() {

    private val loginServiceGrpc: LoginServiceGrpc.LoginServiceStub = LoginServiceGrpc
        .newStub(GRPCClient.channel)


    private var isLoginInProgress: Boolean by mutableStateOf(false)

    private fun login() {
        isLoginInProgress = true
        loginServiceGrpc
            .withDeadline(Deadline.after(3, TimeUnit.SECONDS))
            .login(
                FakeLogin.FakeLoginRequest,
                stream {
                    runOnUiThread {
                        isLoginInProgress = when (it) {
                            is StreamValue.Complete -> {
                                false
                            }

                            is StreamValue.Error -> {
                                Toast.makeText(
                                    this,
                                    (it as? StreamValue.Error)?.error.getStatusCode(),
                                    Toast.LENGTH_SHORT
                                ).show()
                                false
                            }

                            is StreamValue.Value -> {
                                authToken = it.value.bToken
                                false
                            }
                        }
                    }
                }
            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShoppingCatalogueTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(text = "gRPC Client")
                            }
                        )
                    }
                ) { innerPadding ->
                    AnimatedVisibility(
                        visible = authToken != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        OnLogin(innerPadding)

                    }
                    AnimatedVisibility(
                        visible = authToken == null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp)
                        ) {
                            PrimaryButton(
                                text = "Fake Login",
                                onClick = ::login,
                                enabled = true,
                                modifier = Modifier.align(Alignment.Center),
                                isLoading = isLoginInProgress
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun OnLogin(innerPadding: PaddingValues) {
        FlowRow(
            maxItemsInEachRow = 2,
            modifier = Modifier
                .padding(innerPadding)
                .padding(5.dp),
        ) {
            Item(
                text = "Unary \nRPC",
                onClick = { UnaryRPCActivity.start(this@MainActivity) }
            )
            Item(
                text = "Server \nStreaming \nRPC",
                onClick = { ServerStreamingRPCActivity.start(context = this@MainActivity) }
            )
            Item(
                text = "Client \nStreaming \nRPC",
                onClick = { ClientStreamingRPCActivity.start(context = this@MainActivity) }
            )
            Item(
                "Bidirectional \nStreaming \nRPC",
                onClick = {
                    BidirectionalStreamingRPCActivity.start(context = this@MainActivity)
                }
            )
        }
    }

    @Composable
    private fun Item(
        text: String,
        onClick: () -> Unit = {}
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .aspectRatio(1 / 1f)
                .padding(10.dp),
            onClick = onClick
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = text,
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontWeight = FontWeight.Black, fontSize = 22.sp)
                )
            }
        }
    }
}