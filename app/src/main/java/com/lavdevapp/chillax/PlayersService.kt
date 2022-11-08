package com.lavdevapp.chillax

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlin.math.roundToInt

class PlayersService : Service() {
    private val activeMediaPlayers = mutableListOf<MediaPlayer>()
    private var timer: CountDownTimer? = null
    private val _timerStatus = MutableLiveData<TimerStatus>()
    val timerStatus: LiveData<TimerStatus>
        get() = _timerStatus
    private val playersActive: Boolean
        get() {
            return activeMediaPlayers.isNotEmpty()
        }
    private val timerActive: Boolean
        get() {
            return timer != null
        }
    val isWorking: Boolean
        get() {
            return playersActive || timerActive
        }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopPlayersIfActive()
        stopTimer()
        super.onTaskRemoved(rootIntent)
    }

    override fun onBind(intent: Intent): IBinder {
        return PlayersServiceBinder()
    }

    fun initiatePlaylist(trackList: List<Track>) {
        stopPlayersIfActive()
        prepareAndStartPlayers(trackList)
    }

    fun stopPlayersIfActive() {
        if (activeMediaPlayers.isNotEmpty()) activeMediaPlayers.forEach {
            it.stop()
            it.release()
        }
        activeMediaPlayers.clear()
        if (!timerActive) stopForeground()
    }

    fun startTimer(hour: Int, minute: Int) {
        startForeground()
        timer = object : CountDownTimer(timeToMillis(hour, minute), 1000) {
            override fun onTick(remainigMillis: Long) {
                _timerStatus.value = TimerStatus(millisToTime(remainigMillis), timerActive)
                updateNotification()
            }

            override fun onFinish() {
                stopTimer(true)
            }
        }.start()
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
            _timerStatus.value =
                TimerStatus(resources.getString(R.string.timer_default_text), false)
            if (!playersActive) stopForeground() else updateNotification()
        }
    }

    private fun timeToMillis(hour: Int, minute: Int): Long {
        return hour * 3600000L + minute * 60000L
    }

    private fun millisToTime(millis: Long): String {
        val totalMinutes = millis / 60000
        val hour = totalMinutes / 60
        val minute = ((totalMinutes / 60.0 - hour) * 60.0).roundToInt()
        return resources.getString(R.string.timer_text_format, hour, minute)
    }

    private fun prepareAndStartPlayers(trackList: List<Track>) {
        trackList.forEach {
            if (it.switchEnabled && it.switchState) {
                val mediaPlayer = initMediaPlayer(it.parsedUri, it.volume)
                activeMediaPlayers.add(mediaPlayer)
            }
        }
        startPlayers()
    }

    private fun startPlayers() {
        if (activeMediaPlayers.isNotEmpty()) {
            startForeground()
            activeMediaPlayers.forEach {
                //start players from callback when prepared
                it.prepareAsync()
            }
            updateNotification()
        }
    }

    private fun initMediaPlayer(trackUri: Uri, volume: Float): MediaPlayer {
        return MediaPlayer().apply {
            setDataSource(this@PlayersService, trackUri)
            setVolume(volume, volume)
            setWakeMode(this@PlayersService, PowerManager.PARTIAL_WAKE_LOCK)
            setOnPreparedListener { it.start() }
        }.also {
            it.isLooping = true
        }
    }

    private fun startForeground() {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        updateNotification()
    }

    private fun stopForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            //Required for api level < 24
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntentImmutableFlag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.foreground_service_notification_text))
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            //setting notification actions
            .apply {
                //set action to open app
                PendingIntent.getActivity(
                    this@PlayersService,
                    0,
                    Intent(this@PlayersService, MainActivity::class.java),
                    pendingIntentImmutableFlag or PendingIntent.FLAG_CANCEL_CURRENT
                ).also { setContentIntent(it) }

                //set action to stop playlist
                if (playersActive) {
                    PendingIntent.getBroadcast(
                        this@PlayersService,
                        0,
                        Intent().apply { action = NOTIFICATION_ACTION_STOP_PLAYERS },
                        pendingIntentImmutableFlag or PendingIntent.FLAG_ONE_SHOT
                    ).also { addAction(0, getString(R.string.notification_action_stop_playback), it) }
                }

                //set action to stop timer
                if (timerActive) {
                    PendingIntent.getBroadcast(
                        this@PlayersService,
                        0,
                        Intent().apply { action = NOTIFICATION_ACTION_STOP_TIMER },
                        pendingIntentImmutableFlag or PendingIntent.FLAG_ONE_SHOT
                    ).also {
                        addAction(0, getString(R.string.notification_action_stop_timer), it)
                        setContentText("Timer: ${_timerStatus.value?.currentTime}")
                    }
                }
            }
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notification = createNotification()
        notificationManager.notify(NOTIFICATION_ID, notification)
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
        private const val NOTIFICATION_CHANNEL_ID =
            "players_service_notification_channel_id"
        private const val NOTIFICATION_CHANNEL_NAME =
            "players_service_notification_channel_name"
        private const val NOTIFICATION_ID = 1
        const val NOTIFICATION_ACTION_STOP_PLAYERS = "com.lavdevapp.chillax.STOP_PLAYERS"
        const val NOTIFICATION_ACTION_STOP_TIMER = "com.lavdevapp.chillax.STOP_TIMER"
    }
}