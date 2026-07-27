package com.example.echojournal.data.audio

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class AudioPlayerManager(context: Context) {
    private val player = ExoPlayer.Builder(context).build()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    fun play(file: File) {
        val mediaItem = MediaItem.fromUri(file.absolutePath)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        _isPlaying.value = true
    }

    fun stop() {
        player.stop()
        _isPlaying.value = false
    }

    fun release() {
        player.release()
    }
}
