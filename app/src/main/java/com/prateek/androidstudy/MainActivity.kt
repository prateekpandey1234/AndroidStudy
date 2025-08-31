package com.prateek.androidstudy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.prateek.androidstudy.presentation.news.NewsListScreen
import com.prateek.androidstudy.presentation.websocket.WebSocketChatScreen
import com.prateek.androidstudy.ui.theme.AndroidStudyTheme
import com.prateek.androidstudy.viewmodel.News.NewsApiViewModel
import com.prateek.androidstudy.viewmodel.websocket.WebSocketViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel : NewsApiViewModel = hiltViewModel()

            val webSocketViewModel : WebSocketViewModel = hiltViewModel()

            AndroidStudyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WebSocketChatScreen(modifier = Modifier.padding(innerPadding),viewModel = webSocketViewModel)
//                    val state by viewModel.uiState.collectAsState()
//                    NewsListScreen(state, modifier = Modifier.padding(innerPadding), paginateNews = {
//                        viewModel.getNews()
//                    }, onArticleClick = {})
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidStudyTheme {
        Greeting("Android")
    }
}