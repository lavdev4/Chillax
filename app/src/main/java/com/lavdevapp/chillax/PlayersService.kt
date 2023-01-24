package com.lavdevapp.chillax

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.TimeUnit

class PlayersService : Service(), AudioManager.OnAudioFocusChangeListener {
    private var currentTrackList: List<Track>? = null
    private val activePlayers = mutableMapOf<String, LoopPlayer>()
    private val audioManager by lazy { getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false
    private var playbackDelayed = false
    private var timer: CountDownTimer? = null
    private val _timerStatus = MutableLiveData<TimerStatus>()
    val timerStatus: LiveData<TimerStatus>
        get() = _timerStatus
    private val playersActive: Boolean
        get() = activePlayers.isNotEmpty()
    private val timerActive: Boolean
        get() = timer != null
    val isWorking: Boolean
        get() = playersActive || timerActive

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopPlayers()
        stopTimer()
        super.onTaskRemoved(rootIntent)
    }

    override fun onBind(intent: Intent): IBinder {
        return PlayersServiceBinder()
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (playbackDelayed) {
                    initiatePlaylist(currentTrackList!!)
                    playbackDelayed = false
                } else {
                    resumePause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                sendStopPlaybackBroadcastAction()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                pausePlayers()
            }
        }
    }

    private fun sendStopPlaybackBroadcastAction() {
        if (playersActive) {
            Intent().also {
                it.action = BROADCAST_ACTION_STOP_PLAYERS
                sendBroadcast(it)
            }
        }
    }

    fun initiatePlaylist(trackList: List<Track>) {
        currentTrackList = trackList
        when (analyzeNewTrackList(trackList)) {
            CASE_FILL_TRACK_LIST -> startPlayers(trackList)
            CASE_UPDATE_TRACK_LIST -> updatePlayers(trackList)
            CASE_PURGE_TRACK_LIST -> stopPlayers()
        }
    }

    private fun analyzeNewTrackList(trackList: List<Track>): Int {
        return if (trackList.none { it.isEnabled } && !playersActive) CASE_NO_ACTION_NEEDED
        else if (trackList.none { it.isPlaying } && !playersActive) CASE_NO_ACTION_NEEDED
        else if (trackList.none { it.isEnabled }) CASE_PURGE_TRACK_LIST
        else if (trackList.none { it.isPlaying }) CASE_PURGE_TRACK_LIST
        else if (trackList.any { it.isPlaying } && !playersActive) CASE_FILL_TRACK_LIST
        else if (trackList.any { it.isPlaying } && playersActive) CASE_UPDATE_TRACK_LIST
        else throw RuntimeException("Unknown case")
    }

    private fun startPlayers(trackList: List<Track>) {
        hasAudioFocus = requestAudioFocus()
        if (hasAudioFocus) {
            startForeground()
            trackList.forEach { track ->
                if (track.isEnabled && track.isPlaying) {
                    activePlayers[track.trackName] =
                        LoopPlayer.create(this, track.parsedUri, track.volume)
                }
            }
            updateNotification()
        }
    }

    private fun updatePlayers(trackList: List<Track>) {
        trackList.forEach { track ->
            if (!(track.isPlaying && track.isEnabled) && activePlayers.containsKey(track.trackName)) {
                activePlayers[track.trackName]?.stop()
                activePlayers.remove(track.trackName)
            }
            if (track.isEnabled && track.isPlaying && !activePlayers.containsKey(track.trackName)) {
                activePlayers[track.trackName] =
                    LoopPlayer.create(this, track.parsedUri, track.volume)
            }
        }
    }

    fun stopPlayers() {
        if (playersActive) activePlayers.forEach { it.value.stop() }
        activePlayers.clear()
        abandonAudioFocus()
        if (!timerActive) stopForeground()
    }

    private fun resumePause() {
        if (playersActive) activePlayers.forEach {
            it.value.start()
        }
    }

    private fun pausePlayers() {
        if (playersActive) activePlayers.forEach {
            it.value.pause()
        }
//        stopPlayersAfterDelay()
    }

/*    private fun stopPlayersAfterDelay() {
        Handler(Looper.getMainLooper()).postDelayed(
            ::stopPlayers,
            TimeUnit.MINUTES.toMillis(10)
        )
    }*/

    fun startTimer(hour: Int, minute: Int) {
        startForeground()
        timer = object : CountDownTimer(timeToMillis(hour, minute), 1000) {
            override fun onTick(remainigMillis: Long) {
                _timerStatus.value = TimerStatus(millisToTime(remainigMillis), timerActive)
                if (remainigMillis / 1000 % 60 == 0.toLong()) updateNotification()
            }

            override fun onFinish() {
                stopTimer(true)
            }
        }.start()
        Handler(Looper.getMainLooper()).postDelayed({ updateNotification() }, 1000)
    }

    fun stopTimer(isFinished: Boolean = false) {
        if (isFinished) {
            timer = null
            stopPlayers()
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
        return TimeUnit.HOURS.toMillis(hour.toLong()) + TimeUnit.MINUTES.toMillis(minute.toLong())
    }

    private fun millisToTime(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(hours)
        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(
                minutes
            )
        return resources.getString(R.string.timer_text_format, hours, minutes, seconds)
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
                        Intent().apply { action = BROADCAST_ACTION_STOP_PLAYERS },
                        pendingIntentImmutableFlag or PendingIntent.FLAG_ONE_SHOT
                    ).also {
                        addAction(
                            0,
                            getString(R.string.notification_action_stop_playback),
                            it
                        )
                    }
                }

                //set action to stop timer
                if (timerActive) {
                    PendingIntent.getBroadcast(
                        this@PlayersService,
                        0,
                        Intent().apply { action = BROADCAST_ACTION_STOP_TIMER },
                        pendingIntentImmutableFlag or PendingIntent.FLAG_ONE_SHOT
                    ).also {
                        addAction(0, getString(R.string.notification_action_stop_timer), it)
                        val time = _timerStatus.value?.currentTime?.let { timeString ->
                            timeString.split(":")
                                .run {
                                    resources.getString(
                                        R.string.timer_text_format_short,
                                        get(0).toInt(),
                                        get(1).toInt() + 1
                                    )
                                }
                        }
                        setContentText("Timer: $time")
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

    private fun requestAudioFocus(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                setAudioAttributes(AudioAttributes.Builder().run {
                    setUsage(AudioAttributes.USAGE_MEDIA)
                    setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    build()
                })
                setAcceptsDelayedFocusGain(true)
                setOnAudioFocusChangeListener(this@PlayersService)
                build()
            }
            val result = audioManager.requestAudioFocus(audioFocusRequest!!)
            synchronized(this@PlayersService) {
                return when (result) {
                    AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> true
                    AudioManager.AUDIOFOCUS_REQUEST_FAILED -> false
                    AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                        playbackDelayed = true
                        false
                    }
                    else -> false
                }
            }
        } else {
            //Required for api level < 26
            @Suppress("DEPRECATION")
            val result = audioManager.requestAudioFocus(
                this@PlayersService, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN
            )
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            //Required for api level < 26
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(this@PlayersService)
        }
        hasAudioFocus = false
    }

    inner class PlayersServiceBinder : Binder() {
        val service: PlayersService
            get() = this@PlayersService
    }

    data class TimerStatus(
        //Expected to have hh:mm:ss format
        val currentTime: String,
        val isActive: Boolean,
        val isFinished: Boolean = false,
    )

    companion object {
        private const val NOTIFICATION_CHANNEL_ID =
            "players_service_notification_channel_id"
        private const val NOTIFICATION_CHANNEL_NAME =
            "players_service_notification_channel_name"
        private const val NOTIFICATION_ID = 1
        const val BROADCAST_ACTION_STOP_PLAYERS = "com.lavdevapp.chillax.STOP_PLAYERS"
        const val BROADCAST_ACTION_STOP_TIMER = "com.lavdevapp.chillax.STOP_TIMER"
        private const val CASE_FILL_TRACK_LIST = 2
        private const val CASE_UPDATE_TRACK_LIST = 3
        private const val CASE_PURGE_TRACK_LIST = 4
        private const val CASE_NO_ACTION_NEEDED = 5
    }
}