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

// TODO: add in manifest
class PlayersService : Service() {
    private val activeMediaPlayers = mutableListOf<MediaPlayer>()

    override fun onCreate() {
        startForeground(SERVICE_NOTIFICATION_ID, createNotification())
        Log.d("app_log", "service started")
        super.onCreate()
    }

    override fun onBind(intent: Intent): IBinder {
        return PlayersServiceBinder()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("app_log", "service start command")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("app_log", "service destroyed")
    }

    fun initiatePlaylist(trackList: List<Track>) {
        deactivateCurrentPlaylist()
        trackList.forEach { track ->
            if (track.switchEnabled && track.switchState) {
                val mediaPlayer = MediaPlayer().apply {
                    setDataSource(this@PlayersService, track.parsedUri)
                    setOnPreparedListener {
                        it.isLooping = true
                        it.start()
                    }
                    prepareAsync()
                }
                Log.d("app_log", "player $mediaPlayer, with track name ${track.trackName} started")
                activeMediaPlayers.add(mediaPlayer)
            }
        }
        Log.d("app_log", "playlist activated in service")
    }

    private fun deactivateCurrentPlaylist() {
        activeMediaPlayers.forEach {
            it.stop()
            it.release()
        }
        activeMediaPlayers.clear()
        Log.d("app_log", "active media players - $activeMediaPlayers")
        Log.d("app_log", "playlist cleared in service")
    }

    private fun createNotification(): Notification {
        // TODO: test lower notification importance/priority
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                SERVICE_NOTIFICATION_CHANNEL_ID,
                SERVICE_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
        return NotificationCompat.Builder(this, SERVICE_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(resources.getString(R.string.app_name))
            .setContentText(getString(R.string.foreground_service_notification_text))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
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