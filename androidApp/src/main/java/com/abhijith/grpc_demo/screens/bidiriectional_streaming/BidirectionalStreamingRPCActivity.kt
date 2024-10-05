package com.abhijith.grpc_demo.screens.bidiriectional_streaming

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abhijith.grpc_demo.ui.components.MyTopAppBar
import com.abhijith.grpc_demo.ui.components.PrimaryButton
import com.abhijith.grpc_demo.ui.components.SecondaryButton
import com.abhijith.grpc_demo.ui.components.TextInputFiled
import com.abhijith.grpc_demo.ui.components.chat.ChatScreen
import com.abhijith.grpc_demo.ui.theme.ShoppingCatalogueTheme

class BidirectionalStreamingRPCActivity : ComponentActivity() {

    private val vm: BidirectionalStreamingRPCViewmodel by viewModels()

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, BidirectionalStreamingRPCActivity::class.java)
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
                            title = "Bidirectional Streaming RPC"
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ChatScreen(
                chatItems = vm.echos.collectAsState().value,
                modifier = Modifier.weight(1f)
            )
            if (vm.isConnected) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SecondaryButton(
                        text = "Disconnect",
                        onClick = {
                            vm.disConnect()
                        },
                        deleteAction = true,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
                    )
                    EchoMessageInputField()
                }
            } else {
                PrimaryButton(
                    text = "Connect to server",
                    onClick = {
                        vm.connectToServer()
                    },
                    enabled = true,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }


    @Composable
    private fun EchoMessageInputField() {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var echoMessage: String by remember { mutableStateOf("") }
            TextInputFiled(
                userName = echoMessage,
                onInputChanges = { echoMessage = it },
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 25.dp),
                hint = "Type message for echo",
            )
            Spacer(modifier = Modifier.width(10.dp))
            PrimaryButton(
                text = "Echo",
                onClick = { vm.echo(echoMessage) },
                enabled = true,
                isLoading = false,
                modifier = Modifier.width(100.dp),
            )
        }
    }
}
