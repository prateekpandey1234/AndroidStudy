package com.prateek.androidstudy.viewmodel.videoStreams

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.TrackSelector
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltViewModel
class VideoPlayerViewModel @Inject constructor(@ApplicationContext private val context: Context) :
    ViewModel() {

    private var _uiState: MutableLiveData<PlayerUiState> = MutableLiveData(
        PlayerUiState(
            isPlaying = false,
            player = null,
            trackSelector = null,
            seekPointer = 0f,
            isFullScreen = false,
            isBuffering = false,
            isError = false,
            errorMessage = "",
            currentVideoTrack = "",
            videoTracks = emptyList(),
            currentTimeStamp = null,
            finalTimeStamp = null,
            videoEndTimeMs = null
        )
    );
    val uiState: LiveData<PlayerUiState> = _uiState

    init {

    }


    fun getAllTracks() {
        val player = _uiState.value?.player ?: return
        val videoTracks = mutableListOf<String>()
        videoTracks.add("Auto") // Add Auto option

        for (track in player.currentTracks.groups) {
            if (track.type == C.TRACK_TYPE_VIDEO) {
                for (i in 0 until track.length) {
                    val format = track.getTrackFormat(i)
                    val h = format.height
                    val w = format.width

                    if (h != androidx.media3.common.Format.NO_VALUE && w != androidx.media3.common.Format.NO_VALUE) {
                        videoTracks.add("${h}x${w}")
                        Log.d("VideoPlayerViewModel", "getAllTracks: ${h}x${w}")
                    }
                }
            }
        }

        if (videoTracks.size <= 1) { // Only "Auto"
            return
        }

        // Sort resolutions, keep "Auto" at top
        // dummy dash url workig
        // https://livesim.dashif.org/livesim/testpic_2s/Manifest.mpd :- live stream
        // https://dash.akamaized.net/akamai/bbb_30fps/bbb_30fps.mpd :- stream to test quality changing

        // go here to learn bit more in depth https://github.com/prateekpandey1234/AndroidLearnings/blob/main/Video.md

        val sortedTracks = videoTracks.subList(1, videoTracks.size)
            .sortedWith { a, b -> getResolution(a) - getResolution(b) }

        val finalTracks = listOf("Auto") + sortedTracks.distinct()

        _uiState.value = _uiState.value?.copy(
            videoTracks = finalTracks,
            currentVideoTrack = _uiState.value?.currentVideoTrack ?: "Auto"
        )
    }

    fun getResolution(str: String): Int {
        val x = str.split("x")
        return x[0].toInt() * x[1].toInt()
    }

    @OptIn(UnstableApi::class)
    fun setResolution(resolution: String) {
        val player = _uiState.value?.player ?: return
        val trackSelector = _uiState.value?.trackSelector as? DefaultTrackSelector ?: return

        if (resolution == "Auto") {
            trackSelector.setParameters(
                trackSelector.buildUponParameters()
                    .clearOverrides()
                    .build()
            )
        } else {
            val dimensions = resolution.split("x")
            val targetHeight = dimensions[0].toInt()
            val targetWidth = dimensions[1].toInt()

            for (group in player.currentTracks.groups) {
                if (group.type == C.TRACK_TYPE_VIDEO) {
                    for (i in 0 until group.length) {
                        val format = group.getTrackFormat(i)
                        if (format.height == targetHeight && format.width == targetWidth) {
                            trackSelector.setParameters(
                                trackSelector.buildUponParameters()
                                    .setOverrideForType(
                                        androidx.media3.common.TrackSelectionOverride(
                                            group.mediaTrackGroup,
                                            i
                                        )
                                    )
                                    .build()
                            )
                            break
                        }


                    }
                }
            }
        }
        _uiState.value = _uiState.value?.copy(currentVideoTrack = resolution)
    }

    @OptIn(UnstableApi::class)
    fun initPlayer(url:String) {
        if (_uiState.value?.player != null) return

        val trackSelector = DefaultTrackSelector(context, AdaptiveTrackSelection.Factory())
        val defaultParameters = trackSelector.buildUponParameters()
            .setAllowVideoMixedMimeTypeAdaptiveness(true) // allow change in codec info
            .build()

        trackSelector.setParameters(defaultParameters)


        val bufferController = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                20_000,  // minBufferMs - prevents drip-feeding
                60_000,  // maxBufferMs - higher for stable connections
                2_000,   // bufferForPlaybackMs - start quickly
                5_000    // bufferForPlaybackAfterRebufferMs
            )
            .setTargetBufferBytes(DefaultLoadControl.DEFAULT_TARGET_BUFFER_BYTES)
            .setPrioritizeTimeOverSizeThresholds(true) // Better for live streams
            .build()


        val okHttpClient = OkHttpClient()
        val dataSourceFactory = DefaultDataSourceFactory(
            context,
            OkHttpDataSource.Factory(okHttpClient)
        )

        // Step 2.6: Create media source factory with DASH, HLS, and SmoothStreaming support
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)


        val player = ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .setLoadControl(bufferController)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onTracksChanged(tracks: Tracks) {
                        super.onTracksChanged(tracks)
                        // this is called after the tracks are fetched
                        getAllTracks()

                    }

                    override fun onIsLoadingChanged(isLoading: Boolean) {
                        super.onIsLoadingChanged(isLoading)
                        _uiState.value = _uiState.value?.copy(isBuffering = isLoading)
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        super.onIsPlayingChanged(isPlaying)
                        _uiState.value = _uiState.value?.copy(isPlaying = isPlaying)
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        super.onPlaybackStateChanged(playbackState)
                        // this is called whenever internal status are changed

                        when (playbackState) {
                            Player.STATE_IDLE -> {
                                // Player is stopped or empty
                            }

                            Player.STATE_BUFFERING -> {
                                _uiState.value = _uiState.value?.copy(isBuffering = true)
                            }

                            Player.STATE_READY -> {
                                _uiState.value = _uiState.value?.copy(
                                    isPlaying = playWhenReady,
                                    isBuffering = false,
                                    isReady = true
                                )

                                if (_uiState.value!!.player!!.duration != C.TIME_UNSET) {// not a live stream

                                    val duration = _uiState.value!!.player!!.duration
                                    _uiState.value = _uiState.value!!.copy(
                                        currentTimeStamp = convertTime(0),
                                        finalTimeStamp = convertTime(duration),
                                        videoEndTimeMs = duration
                                    )

                                }
                            }

                            Player.STATE_ENDED -> {
                                // The video finished. Show a replay button!
                            }

                        }
                    }
                })
                // Use the live stream URL provided by the user
                val streamUrl = url.replace("localhost", "10.0.2.2")
                val mediaItem =
                    MediaItem.fromUri(streamUrl)
                setMediaItem(mediaItem)
                prepare()
            }
        observerRunTime()

        _uiState.value = _uiState.value?.copy(player = player, trackSelector = trackSelector)
    }

    fun togglePlayPause() {
        val player = _uiState.value?.player ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun toggleFullScreen() {
        _uiState.value = _uiState.value?.copy(isFullScreen = !(_uiState.value?.isFullScreen ?: false))
    }

    fun observerRunTime() {
        viewModelScope.launch {
            while (isActive) {
                _uiState.value?.player?.let { player ->
                    val currentPos = player.currentPosition
                    val duration = if (player.duration != C.TIME_UNSET) player.duration else null

                    val progress = if (duration != null && duration > 0) {
                        currentPos.toFloat() / duration.toFloat()
                    } else {
                        0f
                    }

                    _uiState.postValue(
                        _uiState.value?.copy(
                        currentTimeStamp = convertTime(currentPos),
                        seekPointer = progress,
                        videoEndTimeMs = duration,
                        finalTimeStamp = duration?.let { convertTime(it) }
                    ))
                }
                delay(1000)
            }
        }
    }

    fun seekBarLoad(target: Float) {
        val player = _uiState.value?.player ?: return
        val duration = _uiState.value?.videoEndTimeMs

        if(duration == null){
            // For live streams, we generally don't seek via a 0-1 slider unless it's a window
            _uiState.value = _uiState.value?.copy(seekPointer = 1f)
        }
        else {
            player.pause()
            val seekPos = (target * duration).toLong()
            _uiState.value = _uiState.value?.copy(
                seekPointer = target,
                currentTimeStamp = convertTime(seekPos)
            )
            player.seekTo(seekPos)
            player.play()
        }


    }

    fun convertTime(timeMs: Long): String {
        val safeTimeMs = timeMs.coerceAtLeast(0)

        val hours = TimeUnit.MILLISECONDS.toHours(safeTimeMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(safeTimeMs) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(safeTimeMs) % 60

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }


    override fun onCleared() {
        super.onCleared()
        _uiState.value?.player?.release()
    }

}

@OptIn(UnstableApi::class)
data class PlayerUiState(
    var isPlaying: Boolean,
    val player: ExoPlayer?,
    val trackSelector: TrackSelector?,
    var seekPointer: Float,
    var isFullScreen: Boolean,
    var isBuffering: Boolean,
    var isError: Boolean,
    var errorMessage: String,
    val currentVideoTrack: String,
    val videoTracks: List<String>,
    var isReady: Boolean = false,
    var currentTimeStamp: String?,
    var finalTimeStamp: String?,
    var videoEndTimeMs: Long?
)
