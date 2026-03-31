package com.prateek.androidstudy.viewmodel.videoStreams

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import com.prateek.androidstudy.ui.theme.AndroidStudyTheme
import okhttp3.OkHttpClient

@UnstableApi
class ExoPlayerInternals : ComponentActivity() {
    lateinit var exoPlayer: ExoPlayer
    lateinit var trackSelector: DefaultTrackSelector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidStudyTheme {
                Column(modifier = Modifier.fillMaxSize()) {
                    VideoPlayer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                    )

                    Text("Video content here")
                }
            }
        }

    }


    @Composable
    fun VideoPlayer(modifier: Modifier){
        val context = LocalContext.current
        DisposableEffect(Unit) {

            onDispose {
                exoPlayer.release()
            }
        }

        AndroidView(
            factory = { context->
                PlayerView(context).apply {
                    player = exoPlayer
                }
            }
        )
    }


    // dummy dash url workig
    // https://livesim.dashif.org/livesim/testpic_2s/Manifest.mpd :- live stream
    // https://dash.akamaized.net/akamai/bbb_30fps/bbb_30fps.mpd :- stream to test quality changing

    // go here to learn bit more in depth https://github.com/prateekpandey1234/AndroidLearnings/blob/main/Video.md
    @OptIn(UnstableApi::class)
    fun initPlayer(){
        if(exoPlayer==null){
            // the factory won't it self change the track we have to manipulate the track selector here to do so
            val trackSelectionFactory = AdaptiveTrackSelection.Factory()
            trackSelector  = DefaultTrackSelector(this, trackSelectionFactory)

            configureTrackSelector(trackSelector)

            // Step 2.4: Create optimized load control for adaptive streaming
            val loadControl = createOptimizedLoadControl()
            val okHttpClient = OkHttpClient()
            val dataSourceFactory = DefaultDataSourceFactory(
                this,
                OkHttpDataSource.Factory(okHttpClient)
            )

            // Step 2.6: Create media source factory with DASH, HLS, and SmoothStreaming support
            val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

            // Step 2.7: Build player with adaptive streaming configuration
            exoPlayer = ExoPlayer.Builder(this)
                .setTrackSelector(trackSelector!!)
                .setLoadControl(loadControl)
                .setMediaSourceFactory(mediaSourceFactory)
                .build()
                .apply {
                    repeatMode = Player.REPEAT_MODE_OFF
                    playWhenReady = false

                    // Add analytics listener for quality changes
                    addAnalyticsListener(object : AnalyticsListener {
                        override fun onVideoSizeChanged(
                            eventTime: AnalyticsListener.EventTime,
                            videoSize: VideoSize
                        ) {
                        }
                    })
                }

            // Step 2.8: Adjust quality based on network conditions
            adjustQualityForNetwork()

        }
    }
    // to setup track
    private fun configureTrackSelector(trackSelector: DefaultTrackSelector) {
        val parameters = trackSelector.buildUponParameters()
            // Video constraints
            .setMaxVideoSize(1920, 1080) // Max 1080p
            .setMaxVideoBitrate(5_000_000) // 5 Mbps cap

            // Audio preferences
            .setPreferredAudioLanguage("en")

            // Subtitle preferences
            .setPreferredTextLanguage("en")
            .setSelectUndeterminedTextLanguage(true)

            // Performance tuning
            .setForceHighestSupportedBitrate(false)
            .setAllowVideoMixedMimeTypeAdaptiveness(true)
            .build()

        trackSelector.setParameters(parameters)
    }

    private fun createOptimizedLoadControl(): LoadControl {
        return DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                20_000,  // minBufferMs - prevents drip-feeding
                60_000,  // maxBufferMs - higher for stable connections
                2_000,   // bufferForPlaybackMs - start quickly
                5_000    // bufferForPlaybackAfterRebufferMs
            )
            .setTargetBufferBytes(DefaultLoadControl.DEFAULT_TARGET_BUFFER_BYTES)
            .setPrioritizeTimeOverSizeThresholds(true) // Better for live streams
            .build()
    }

    private fun adjustQualityForNetwork() {
        val connectivityManager = this.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        val networkCapabilities = connectivityManager.activeNetwork?.let {
            connectivityManager.getNetworkCapabilities(it)
        }

        val maxBitrate = when {
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> {
                5_000_000 // 5 Mbps for WiFi
            }
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> {
                2_000_000 // 2 Mbps for cellular
            }
            else -> {
                1_000_000 // 1 Mbps for unknown/slow connections
            }
        }
        // here we configure the current track been used
        trackSelector?.setParameters(
            trackSelector!!.buildUponParameters()
                .setMaxVideoBitrate(maxBitrate)
                .build()
        )
    }

    



}
