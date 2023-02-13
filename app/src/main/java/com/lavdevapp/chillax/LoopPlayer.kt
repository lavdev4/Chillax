package com.lavdevapp.chillax

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.PowerManager
import kotlin.random.Random

//Required for repeated playback without gaps
class LoopPlayer(
    private val context: Context,
    private val dataSource: Uri,
    private val volume: Float
) {
    private var currentPlayer: MediaPlayer = MediaPlayer()
    private var nextPlayer: MediaPlayer? = null
    private var isPaused = false

    init {
        with(currentPlayer) {
            setDataSource(context, dataSource)
            setVolume(volume, volume)
            setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
            setOnPreparedListener {
                val pos = Random.nextInt(1000, it.duration - 1000)
                it.seekTo(pos)
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
            setOnPreparedListener {
                currentPlayer.setNextMediaPlayer(nextPlayer)
                currentPlayer.setOnCompletionListener {
                    nextPlayer!!.start()
                    it.release()
                    currentPlayer = nextPlayer as MediaPlayer
                    prepareNextPlayer()
                }
            }
        }
        nextPlayer?.prepareAsync()
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
        nextPlayer?.release()
    }

//    fun isPlaying() = currentPlayer.isPlaying

    companion object {
        fun create(context: Context, dataSource: Uri, volume: Float) =
            LoopPlayer(context, dataSource, volume).apply { start() }
    }
}