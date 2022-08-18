package com.lavdevapp.chillax

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import kotlin.math.roundToInt

class PlayersService : Service() {
    private val activeMediaPlayers = mutableListOf<MediaPlayer>()
    private var timer: CountDownTimer? = null
    private val _timerStatus = MutableLiveData<TimerStatus>()
    val timerStatus: LiveData<TimerStatus>
        get() = _timerStatus
    private val playersActive: Boolean
        get() { return activeMediaPlayers.isNotEmpty() }
    private val timerActive: Boolean
        get() { return timer != null }
    val isWorking: Boolean
        get() { return playersActive || timerActive }

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
        prepareAndStartPlayers(trackList)
        Log.d("app_log", "playlist activated in service")
    }

    fun startTimer(hour: Int, minute: Int) {
        startForeground()
        timer = object : CountDownTimer(timeToMillis(hour, minute), 1000) {
            override fun onTick(remainigMillis: Long) {
                _timerStatus.value = TimerStatus(millisToTime(remainigMillis), timerActive)
                Log.d("app_timer", "on tick")
            }

            override fun onFinish() {
                stopTimer(true)
                Log.d("app_timer", "on finish")
            }
        }.start()
        Log.d("app_timer", "timer started")
    }

    fun stopTimer(isFinished: Boolean = false) {
        if (isFinished) {
            timer = null
            stopPlayersIfActive()
            _timerStatus.value = TimerStatus(
                resources.getString(R.string.timer_default_text),
                isActive = false,
                isFinished = true
            )
            stopForeground()
        } else {
            timer?.cancel()
            timer = null
            _timerStatus.value = TimerStatus(resources.getString(R.string.timer_default_text), false)
            if (!playersActive) stopForeground()
        }
    }

    private fun timeToMillis(hour: Int, minute: Int): Long {
//        val millis = hour * 3600000L + minute * 60000L
        val millis = hour * 60000L + minute * 1000L
        Log.d("app_timer", "millis: $millis")
        return millis
    }

    private fun millisToTime(millis: Long): String {
//        val totalMinutes = millis / 60000
//        val hour = totalMinutes / 60
//        val minute = ((totalMinutes / 60.0 - hour) * 60.0).roundToInt() + 1
        val totalMinutes = millis / 1000
        val hour = totalMinutes / 60
        val minute = ((totalMinutes / 60.0 - hour) * 60.0).roundToInt() + 1

        val time = resources.getString(R.string.timer_text_format, hour, minute)
        Log.d("app_timer", "time: $time")
        return time
    }

    private fun stopPlayersIfActive() {
        if (activeMediaPlayers.isNotEmpty()) activeMediaPlayers.forEach {
            it.stop()
            it.release()
        }
        activeMediaPlayers.clear()
        Log.d("app_log", "playlist cleared in service")
    }

    private fun prepareAndStartPlayers(trackList: List<Track>) {
        trackList.forEach { track ->
            if (track.switchEnabled && track.switchState) {
                val mediaPlayer = initMediaPlayer(track.parsedUri)
                activeMediaPlayers.add(mediaPlayer)
                Log.d("app_log", "player init with track name: ${track.trackName}")
            }
        }
        startPlayers()
    }

    private fun startPlayers() {
        if (activeMediaPlayers.isNotEmpty()) {
            startForeground()
            Log.d("app_log", "foreground started")
            activeMediaPlayers.forEach {
                //starts players on prepared callback
                it.prepareAsync()
                Log.d("app_log", "player $it started")
            }
        } else {
            if (!timerActive) stopForeground()
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

    private fun startForeground() {
        createNotificationChannel()
        startForeground(SERVICE_NOTIFICATION_ID, createNotification())
        updateNotification()
    }

    private fun stopForeground() {
        stopForeground(true)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                SERVICE_NOTIFICATION_CHANNEL_ID,
                SERVICE_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, SERVICE_NOTIFICATION_CHANNEL_ID)
            .setContentText(getString(R.string.foreground_service_notification_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
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

    data class TimerStatus(
        val currentTime: String,
        val isActive: Boolean,
        val isFinished: Boolean = false
    )

    companion object {
        private const val SERVICE_NOTIFICATION_CHANNEL_ID =
            "players_service_notification_channel_id"
        private const val SERVICE_NOTIFICATION_CHANNEL_NAME =
            "players_service_notification_channel_name"
        private const val SERVICE_NOTIFICATION_ID = 1
    }
}