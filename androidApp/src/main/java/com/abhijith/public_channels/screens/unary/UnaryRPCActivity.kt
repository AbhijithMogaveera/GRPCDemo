package com.abhijith.public_channels.screens.unary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.abhijith.public_channels.ui.components.DisplayResponse
import com.abhijith.public_channels.ui.components.MyTopAppBar
import com.abhijith.public_channels.ui.components.PrimaryButton
import com.abhijith.public_channels.ui.components.TextInputFiled
import com.abhijith.public_channels.ui.theme.ShoppingCatalogueTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UnaryRPCActivity : ComponentActivity() {

    private var job: Job? = null
    private val unaryRPCSayHello = UnaryRPCSayHello()

    @Composable
    private fun Content() {
        var userName by remember { mutableStateOf("") }
        var response: HelloResponse? by remember { mutableStateOf(null) }
        val scope = rememberCoroutineScope()
        val onClick: (String) -> Unit = {
            job?.cancel()
            job = scope.launch {
                unaryRPCSayHello.sayHelloAsync(it)
                    .catch {

                    }.collectLatest {
                        response = it.getOrNull()
                    }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        TextInputFiled(
            userName,
            onInputChanges = { userName = it },
            modifier = Modifier.fillMaxWidth(0.9f),
            hint = "Type your name here..."
        )
        Spacer(modifier = Modifier.height(10.dp))
        PrimaryButton(
            "$userName Say Hello to gRPC sever 👋",
            onClick = {
                onClick(userName)
            },
            enabled = userName.isNotBlank(),
            modifier = Modifier.fillMaxWidth(0.9f)
        )
        DisplayResponse(response, modifier = Modifier)
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

