package com.abhijith.public_channels.screens.unary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abhijith.public_channels.HelloResponse
import com.abhijith.public_channels.rpc.StreamValue
import com.abhijith.public_channels.ui.components.DisplayError
import com.abhijith.public_channels.ui.components.DisplayResponse
import com.abhijith.public_channels.ui.components.MyTopAppBar
import com.abhijith.public_channels.ui.components.PrimaryButton
import com.abhijith.public_channels.ui.components.TextInputFiled
import com.abhijith.public_channels.ui.theme.ShoppingCatalogueTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class UnaryRPCActivity : ComponentActivity() {

    private var job: Job? = null
    private val vm by viewModels<UnaryRPCViewmodel>()

    @Composable
    private fun Content() {
        var userName by remember { mutableStateOf("") }
        var isLoading: Boolean by remember {
            mutableStateOf(false)
        }
        var responseOrNull: StreamValue<HelloResponse>? by remember { mutableStateOf(null) }
        val shape = RoundedCornerShape(16.dp)
        val scope = rememberCoroutineScope()
        val sayHelloWith: (String) -> Unit = {
            job?.cancel()
            job = scope.launch {
                vm.sayHelloAsync(it).onStart {
                    isLoading = true
                }.collectLatest {
                    isLoading = false
                    responseOrNull = it
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        TextInputFiled(
            userName,
            onInputChanges = { userName = it },
            modifier = Modifier.fillMaxWidth(0.9f),
            hint = "Type your name here...",
            shape = shape
        )
        Spacer(modifier = Modifier.height(10.dp))
        PrimaryButton(
            "$userName Say Hello to gRPC sever 👋",
            onClick = {
                sayHelloWith(userName)
            },
            enabled = userName.isNotBlank(),
            modifier = Modifier.fillMaxWidth(0.9f),
            isLoading = isLoading,
            shape = shape
        )
        when (val response = responseOrNull) {

            is StreamValue.Value -> {
                DisplayResponse(response.value, modifier = Modifier)
            }

            is StreamValue.Complete -> {
                DisplayResponse(response.lastValue, modifier = Modifier)
            }


            is StreamValue.Error -> {
                with(response) {
                    val modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(vertical = 20.dp)
                    onStatusException { statusException ->
                        DisplayError(
                            throwable = statusException,
                            retry = { sayHelloWith(userName) },
                            modifier = modifier,
                            shape = shape
                        )
                    }
                    onStatusRuntimeException { statusRuntimeException ->
                        DisplayError(
                            throwable = statusRuntimeException,
                            retry = { sayHelloWith(userName) },
                            modifier = modifier,
                            shape = shape
                        )
                    }
                }

            }

            null -> {
                /*DISPLAY_NOTHING*/
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
                    topBar = { MyTopAppBar(onNavigationIconClick = ::finish, title = "Unary RPC") }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Content()
                    }
                }
            }
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, UnaryRPCActivity::class.java)
            context.startActivity(intent)
        }
    }

}

