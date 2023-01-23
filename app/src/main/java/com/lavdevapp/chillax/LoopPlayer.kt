package com.lavdevapp.chillax

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.PowerManager

class LoopPlayer(
    private val context: Context,
    private val dataSource: Uri,
    private val volume: Float
) {
    private var currentPlayer: MediaPlayer = MediaPlayer()
    private lateinit var nextPlayer: MediaPlayer
    private var isPaused = false

    init {
        with(currentPlayer) {
            setDataSource(context, dataSource)
            setVolume(volume, volume)
            setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
            setOnPreparedListener {
                it.start()
                prepareNextPlayer()
            }
        }
    }

    private fun prepareNextPlayer() {
        nextPlayer = MediaPlayer().apply {
            setDataSource(context, dataSource)
            setVolume(volume, volume)
            setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
        }
        nextPlayer.setOnPreparedListener {
            currentPlayer.setNextMediaPlayer(nextPlayer)
            currentPlayer.setOnCompletionListener {
                nextPlayer.start()
                it.release()
                currentPlayer = nextPlayer
                prepareNextPlayer()
            }
        }
        nextPlayer.prepareAsync()
    }

    fun start() {
        if (isPaused) {
            currentPlayer.start()
            isPaused = false
        } else {
            //start players from callback when prepared
            currentPlayer.prepareAsync()
        }
    }

    fun stop() {
        currentPlayer.stop()
        release()
    }

    fun pause() {
        currentPlayer.pause()
        isPaused = true
    }

    private fun release() {
        currentPlayer.release()
        nextPlayer.release()
    }

//    fun isPlaying() = currentPlayer.isPlaying

    companion object {
        fun create(context: Context, dataSource: Uri, volume: Float) =
            LoopPlayer(context, dataSource, volume).apply { start() }
    }
}