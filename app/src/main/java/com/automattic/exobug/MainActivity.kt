@file:OptIn(UnstableApi::class)

package com.automattic.exobug

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.ui.PlayerView
import java.nio.ByteBuffer
import kotlin.random.Random

class MainActivity : Activity() {
    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        player = ExoPlayer.Builder(this, DummyRenderersFactory(this)).build()
        val uri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(packageName)
            .appendPath(R.raw.sample.toString())
            .build()
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()

        setContentView(R.layout.main_activity)
        findViewById<PlayerView>(R.id.playerView).player = player
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}

private class DummyProcessor : BaseAudioProcessor() {
    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat) = inputAudioFormat

    override fun queueInput(inputBuffer: ByteBuffer) {
        val array = ByteArray(inputBuffer.remaining())
        inputBuffer.get(array)
        replaceOutputBuffer(array.size).put(array).flip()
    }

    override fun onQueueEndOfStream() {
        val bytes = Random.nextBytes(256).map { (it / 16).toByte() }.toByteArray()
        replaceOutputBuffer(bytes.size).put(bytes).flip()
    }
}

private class DummyRenderersFactory(context: Context) : DefaultRenderersFactory(context) {
    override fun buildAudioSink(
        context: Context,
        enableFloatOutput: Boolean,
        enableAudioTrackPlaybackParams: Boolean,
    ) = DefaultAudioSink.Builder(context)
        .setAudioProcessors(arrayOf(DummyProcessor()))
        .setEnableFloatOutput(enableFloatOutput)
        .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
        .build()
}