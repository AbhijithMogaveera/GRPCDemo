package com.abhijith.public_channels.screens.client_stream

import com.abhijith.public_channels.ui.components.ChatScreen
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.abhijith.public_channels.ui.components.MyTopAppBar
import com.abhijith.public_channels.ui.components.PrimaryButton
import com.abhijith.public_channels.ui.theme.ShoppingCatalogueTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class ClientStreamingRPCActivity : ComponentActivity() {

    private val heartRateMonitor: ClientStreamingRPCViewmodel by viewModels()

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ClientStreamingRPCActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShoppingCatalogueTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        MyTopAppBar(
                            onNavigationIconClick = ::finish,
                            title = "Client Streaming"
                        )
                    }
                ) { innerPadding ->
                    Content(innerPadding)
                }
            }
        }
    }

    var job: Job? = null

    @Composable
    private fun Content(innerPadding: PaddingValues) {
        val scope = rememberCoroutineScope()
        val value = heartRateMonitor.heartRateChatItem.collectAsState().value
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ChatScreen(
                chatItems = value,
                modifier = Modifier.weight(1f)
            )
            var isClientIsStreaming by remember {
                mutableStateOf(false)
            }
            PrimaryButton(
                text = "Start sending hart rate to server",
                onClick = {
                    job = scope.launch {
                        try {
                            val mockNormal = Random.nextBoolean()
                            isClientIsStreaming = true
                            if (mockNormal) {
                                val streamBuilder = heartRateMonitor.getHeartRatePublisher()
                                streamBuilder.onNext(45.0).getOrThrow()
                                delay(300)
                                streamBuilder.onNext(50.0).getOrThrow()
                                delay(300)
                                streamBuilder.onNext(45.0).getOrThrow()
                                delay(300)
                                streamBuilder.onNext(50.0).getOrThrow()
                                delay(300)
                                streamBuilder.onNext(45.0).getOrThrow()
                                delay(300)
                                streamBuilder.onNext(50.0).getOrThrow()
                                streamBuilder.onCompleted()
                            } else {
                                val publish = heartRateMonitor.getHeartRatePublisher()
                                publish.onNext(0.0).getOrThrow()
                                delay(300)
                                publish.onNext(0.0).getOrThrow()
                                delay(300)
                                publish.onNext(0.0).getOrThrow()
                                delay(300)
                                publish.onNext(0.0).getOrThrow()
                                delay(300)
                                publish.onNext(0.0).getOrThrow()
                                delay(300)
                                publish.onNext(0.0).getOrThrow()
                                publish.onCompleted()
                            }
                        } catch (_: Exception) {
                            /*Handled in Chat List*/
                        } finally {
                            isClientIsStreaming = false
                        }

                    }
                },
                enabled = !isClientIsStreaming,
                isLoading = isClientIsStreaming,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        }
    }
}
