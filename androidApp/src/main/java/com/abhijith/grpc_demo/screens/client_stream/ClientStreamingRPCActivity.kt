package com.abhijith.grpc_demo.screens.client_stream

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.abhijith.grpc_demo.ui.components.MyTopAppBar
import com.abhijith.grpc_demo.ui.components.PrimaryButton
import com.abhijith.grpc_demo.ui.components.chat.ChatScreen
import com.abhijith.grpc_demo.ui.theme.ShoppingCatalogueTheme

class ClientStreamingRPCActivity : ComponentActivity() {

    private val clientStreamingRPCViewmodel: ClientStreamingRPCViewmodel by viewModels()

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


    @Composable
    private fun Content(innerPadding: PaddingValues) {
        val value = clientStreamingRPCViewmodel.heartRateChatItem.collectAsState().value
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

            PrimaryButton(
                text = "Start sending hart rate to server",
                onClick = {
                   clientStreamingRPCViewmodel.readAndSendHeartBeats()
                },
                enabled = !clientStreamingRPCViewmodel.isClientIsStreaming,
                isLoading = clientStreamingRPCViewmodel.isClientIsStreaming,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        }
    }
}
