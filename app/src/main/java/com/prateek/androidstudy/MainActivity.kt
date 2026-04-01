package com.prateek.androidstudy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.TrackSelectionUtil
import com.prateek.androidstudy.presentation.news.NewsListScreen
import com.prateek.androidstudy.presentation.websocket.WebSocketChatScreen
import com.prateek.androidstudy.ui.theme.AndroidStudyTheme
import com.prateek.androidstudy.viewmodel.News.NewsApiViewModel
import com.prateek.androidstudy.ExoPlayerInternals
import com.prateek.androidstudy.data.remote.liveStream.ActiveStream
import com.prateek.androidstudy.viewmodel.websocket.WebSocketViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel : NewsApiViewModel = hiltViewModel()

//            val webSocketViewModel : WebSocketViewModel = hiltViewModel()
            AndroidStudyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    WebSocketChatScreen(modifier = Modifier.padding(innerPadding),viewModel = webSocketViewModel)
                    val state by viewModel.streamState.collectAsState()
//                    NewsListScreen(state, modifier = Modifier.padding(innerPadding), paginateNews = {
//                        viewModel.getNews()
//                    }, onArticleClick = {})
                    LaunchedEffect(Unit) {
                        viewModel.getStreams()
                    }

                    LazyColumn(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.data) { stream ->
                            StreamCard(stream = stream){
                                val intent = Intent(this@MainActivity, ExoPlayerInternals::class.java)
                                intent.putExtra("url", it)
                                startActivity(intent)
                            }
                        }
                    }
                }
            }
        }
    }
    @Composable
    fun StreamCard(stream: ActiveStream, modifier: Modifier = Modifier,onClick:(String)->Unit) {
        Card(
            modifier = modifier.fillMaxWidth().clickable(
                onClick={
                    onClick(stream.playbackUrl)
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stream.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Streamer: ${stream.streamerId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Started: ${stream.startedAt}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
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