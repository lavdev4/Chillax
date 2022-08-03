package com.lavdevapp.chillax

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class PlayersService : Service() {
    private val activeMediaPlayers = mutableListOf<MediaPlayer>()
    val isActive = activeMediaPlayers.isNotEmpty()

    override fun onCreate() {
        Log.d("app_log", "service started")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("app_log", "service start command")
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Log.d("app_log", "service destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d("app_log", "service bound")
        return PlayersServiceBinder()
    }

    fun initiatePlaylist(trackList: List<Track>) {
        stopPlayersIfActive()
        initPlayers(trackList)
        startPlayersIfActive()
        Log.d("app_log", "playlist activated in service")
    }

    private fun stopPlayersIfActive() {
        if (activeMediaPlayers.isNotEmpty()) {
            activeMediaPlayers.forEach {
                it.stop()
                it.release()
            }
            activeMediaPlayers.clear()
        }
        Log.d("app_log", "playlist cleared in service")
    }

    private fun initPlayers(trackList: List<Track>) {
        trackList.forEach { track ->
            if (track.switchEnabled && track.switchState) {
                val mediaPlayer = initMediaPlayer(track.parsedUri)
                activeMediaPlayers.add(mediaPlayer)
                Log.d("app_log", "player init with track name: ${track.trackName}")
            }
        }
    }

    private fun startPlayersIfActive() {
        if (activeMediaPlayers.isNotEmpty()) {
            createNotificationChannel()
            startForeground(SERVICE_NOTIFICATION_ID, createNotification())
            //needed to set notification title, text, icon etc.
            updateNotification()
            Log.d("app_log", "foreground started")
            activeMediaPlayers.forEach {
                //starts players on callback
                it.prepareAsync()
                Log.d("app_log", "player $it started")
            }
        } else {
            stopForeground(true)
            Log.d("app_log", "foreground stopped")
        }
    }

    private fun initMediaPlayer(trackUri: Uri): MediaPlayer {
        return MediaPlayer().apply {
            setDataSource(this@PlayersService, trackUri)
            setOnPreparedListener { it.start() }
        }.also {
            it.isLooping = true
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                SERVICE_NOTIFICATION_CHANNEL_ID,
                SERVICE_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, SERVICE_NOTIFICATION_CHANNEL_ID)
            .setContentText(getString(R.string.foreground_service_notification_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(SERVICE_NOTIFICATION_ID, createNotification())
    }

    inner class PlayersServiceBinder : Binder() {
        val service: PlayersService
            get() = this@PlayersService
    }

    companion object {
        private const val SERVICE_NOTIFICATION_CHANNEL_ID = "players_service_notification_channel_id"
        private const val SERVICE_NOTIFICATION_CHANNEL_NAME = "players_service_notification_channel_name"
        private const val SERVICE_NOTIFICATION_ID = 1
    }
}