package com.prateek.androidstudy.data.remote.liveStream

import com.google.gson.annotations.SerializedName

data class StreamResponse(
    val success: Boolean,
    val activeStreamCount: Int,
    val streams: List<ActiveStream>
)

data class ActiveStream(
    @SerializedName("stream_key") val streamKey: String,
    @SerializedName("streamer_id") val streamerId: String,
    val title: String,
    @SerializedName("playback_url") val playbackUrl: String,
    @SerializedName("started_at") val startedAt: String
)