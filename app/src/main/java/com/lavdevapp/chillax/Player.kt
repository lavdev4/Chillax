package com.lavdevapp.chillax

import android.content.Context
import android.media.MediaPlayer

class Player(private val track: Int, val playerTag: String) {
    private var mediaPlayer: MediaPlayer? = null
    var switchState = false
    val isPlaying: Boolean
        get() = if (mediaPlayer == null) {
            false
        } else {
            mediaPlayer!!.isPlaying
        }

    fun startTrack(context: Context?) {
        mediaPlayer?.run {
            MediaPlayer.create(context, track)
            isLooping = true
            start()
        }
    }

    fun stopTrack() {
        mediaPlayer?.let {
            it.stop()
            it.release()
        }
        mediaPlayer = null
    }
}