package com.abhijith.public_channels.screens.bidiriectional_streaming

import com.abhijith.public_channels.ui.components.ChatScreen
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.abhijith.public_channels.ui.components.MyTopAppBar
import com.abhijith.public_channels.ui.components.PrimaryButton
import com.abhijith.public_channels.ui.components.TextInputFiled
import com.abhijith.public_channels.ui.theme.ShoppingCatalogueTheme
import kotlinx.coroutines.Job

class BidirectionalStreamingRPCActivity : ComponentActivity() {

    private val echoServiceGrpc by lazy {
        BidirectionalStreamingEchoMachine()
    }

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

    var job: Job? = null

    @Composable
    private fun Content(innerPadding: PaddingValues) {
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ChatScreen(
                chatItems = echoServiceGrpc.echos.collectAsState().value,
                modifier = Modifier.weight(1f)
            )

            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var echoMessage: String by remember {
                    mutableStateOf("")
                }
                TextInputFiled(
                    userName = echoMessage,
                    onInputChanges = { echoMessage = it },
                    modifier = Modifier.weight(0.7f),
                    hint = "Type message for echo"
                )
                Spacer(modifier = Modifier.width(10.dp))
                PrimaryButton(
                    text = "Echo",
                    onClick = {
                        echoServiceGrpc.echo(echoMessage)
                    },
                    enabled = true,
                    isLoading = false,
                    modifier = Modifier.weight(0.3f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
