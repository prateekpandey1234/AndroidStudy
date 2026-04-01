package com.prateek.androidstudy

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import com.pcompany.fitandupdate.R
import com.prateek.androidstudy.ui.theme.AndroidStudyTheme
import com.prateek.androidstudy.viewmodel.videoStreams.PlayerUiState
import com.prateek.androidstudy.viewmodel.videoStreams.VideoPlayerViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
@UnstableApi
class ExoPlayerInternals : ComponentActivity() {

    @SuppressLint("ContextCastToActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra("url")

        setContent {
            val viewModel: VideoPlayerViewModel = hiltViewModel()
            val playerUiState by viewModel.uiState.observeAsState()
            val context = LocalContext.current as Activity
            LaunchedEffect(Unit) {
                Log.d("streamUrl",url.toString())
                viewModel.initPlayer(url!!)
            }

            AndroidStudyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    playerUiState?.let { uiState ->
                        // Handle orientation change
                        SideEffect {
                            context.requestedOrientation = if (uiState.isFullScreen) {
                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            } else {
                                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                            }
                        }

                        val playerModifier = if (uiState.isFullScreen) {
                            Modifier.fillMaxSize()
                        } else {
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                        }

                        if (uiState.isFullScreen) {
                            VideoPlayer(
                                modifier = playerModifier,
                                uiState = uiState,
                                onResolutionSelected = { viewModel.setResolution(it) },
                                onSeekChanged = { viewModel.seekBarLoad(it) },
                                onPlayPauseToggle = { viewModel.togglePlayPause() },
                                onFullScreenToggle = { viewModel.toggleFullScreen() }
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center
                            ) {
                                VideoPlayer(
                                    modifier = playerModifier,
                                    uiState = uiState,
                                    onResolutionSelected = { viewModel.setResolution(it) },
                                    onSeekChanged = { viewModel.seekBarLoad(it) },
                                    onPlayPauseToggle = { viewModel.togglePlayPause() },
                                    onFullScreenToggle = { viewModel.toggleFullScreen() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun VideoPlayer(
        modifier: Modifier,
        uiState: PlayerUiState,
        onResolutionSelected: (String) -> Unit,
        onSeekChanged: (Float) -> Unit,
        onPlayPauseToggle: () -> Unit,
        onFullScreenToggle: () -> Unit
    ) {
        VideoSurface(modifier, uiState, onResolutionSelected, onSeekChanged, onPlayPauseToggle, onFullScreenToggle)
    }

    @Composable
    fun VideoSurface(
        modifier: Modifier,
        uiState: PlayerUiState,
        onResolutionSelected: (String) -> Unit,
        onSeekChanged: (Float) -> Unit,
        onPlayPauseToggle: () -> Unit,
        onFullScreenToggle: () -> Unit
    ) {
        var showResolutions by remember { mutableStateOf(false) }

        Box(modifier = modifier) {
            // 1. Surface View (Background)
            AndroidView(
                factory = { context ->
                    SurfaceView(context).also { view ->
                        uiState.player?.setVideoSurfaceView(view)
                    }
                },
                update = { view ->
                    uiState.player?.setVideoSurfaceView(view)
                },
                modifier = Modifier.fillMaxSize()
            )

            // 2. Buffering Indicator or Play/Pause Button (Center)
             if (uiState.isBuffering) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            } else {
                 IconButton(
                     onClick = onPlayPauseToggle,
                     modifier = Modifier.align(Alignment.Center).size(64.dp)
                 ) {
                     Icon(
                         imageVector = if (uiState.isPlaying) {
                             ImageVector.vectorResource(id = R.drawable.baseline_pause_24)
                         } else {
                             Icons.Default.PlayArrow
                         },
                         contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                         tint = Color.White,
                         modifier = Modifier.size(48.dp)
                     )
                 }
             }

            // 3. Top Left - Fullscreen Icon
            IconButton(
                onClick = onFullScreenToggle,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = if (uiState.isFullScreen) ImageVector.vectorResource(R.drawable.outline_close_fullscreen_24) else ImageVector.vectorResource(R.drawable.outline_fullscreen_24),
                    contentDescription = "Toggle Fullscreen",
                    tint = Color.White
                )
            }

            // 4. Top Right - Settings Icon
             Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                IconButton(
                    onClick = { showResolutions = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }

                DropdownMenu(
                    expanded = showResolutions,
                    onDismissRequest = { showResolutions = false }
                ) {
                    uiState.videoTracks.forEach { resolution ->
                        DropdownMenuItem(
                            text = {
                                val isSelected = resolution == uiState.currentVideoTrack
                                Text(text = if (isSelected) "✓ $resolution" else resolution)
                            },
                            onClick = {
                                onResolutionSelected(resolution)
                                showResolutions = false
                            }
                        )
                    }
                }
            }

            // 5. Bottom Controls (Slider + Timestamps)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if(uiState.currentTimeStamp!=null){
                        Text(
                            text = uiState.currentTimeStamp!!,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }


                    if (uiState.videoEndTimeMs == null) {
                        Text(
                            text = "Live",
                            color = Color.Red,
                            style = MaterialTheme.typography.labelSmall
                        )
                    } else {
                        Text(
                            text = uiState.finalTimeStamp ?: "00:00:00",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Slider(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.seekPointer,
                    onValueChange = { newValue ->
                        onSeekChanged(newValue)
                    },
                    valueRange = 0f..1f,
                    enabled = uiState.videoEndTimeMs != null
                )
            }
        }
    }
}
