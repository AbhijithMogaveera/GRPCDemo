package com.abhijith.public_channels.screens.server_stream

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import com.abhijith.public_channels.rpc.StreamState
import com.abhijith.public_channels.ui.components.ChatScreen
import com.abhijith.public_channels.ui.components.MyTopAppBar
import com.abhijith.public_channels.ui.components.PrimaryButton
import com.abhijith.public_channels.ui.theme.ShoppingCatalogueTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ServerStreamingRPCActivity : ComponentActivity() {

    private var job: Job? = null
    private val vm: ServerStreamingViewmodel by viewModels()

    private fun startStream() {
        job?.cancel()
        job = lifecycle.coroutineScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                vm.streamWeatherUpdates()
            }
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
                            title = "Server Streaming"
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val streamState: StreamState = vm.streamingState.collectAsState().value
            val items = vm.weatherUpdates.collectAsState().value
            ChatScreen(chatItems = items, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(10.dp))
            PrimaryButton(
                text = "Stream weather updates",
                onClick = ::startStream,
                enabled = streamState != StreamState.ON_GOING,
                isLoading = streamState == StreamState.ON_GOING,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        }
    }
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ServerStreamingRPCActivity::class.java)
            context.startActivity(intent)
        }
    }
}
