package com.lavdevapp.chillax

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.TimePickerDialog
import android.content.*
import android.graphics.Color
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.CompoundButton
import android.widget.TimePicker
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.lavdevapp.chillax.PlayersService.PlayersServiceBinder
import com.lavdevapp.chillax.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener {
    private val viewModel by viewModels<AppViewModel>()
    private lateinit var binding: ActivityMainBinding
    private lateinit var tracksListAdapter: TracksListAdapter
    private lateinit var playersService: PlayersService
    private lateinit var serviceConnection: ServiceConnection
    private lateinit var broadcastReceiver: BroadcastReceiver
    private var serviceTimerObserver: Observer<PlayersService.TimerStatus>? = null
    private var serviceBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupOnBackPressedCallback()
        setupAdapter()
        setupMainSwitch()
        setupTimer()
        setupBroadcastReceiver()
    }

    override fun onStart() {
        super.onStart()
        setupServiceConnection()
        Intent(this, PlayersService::class.java).also {
            applicationContext.startService(it)
            applicationContext.bindService(it, serviceConnection, 0)
        }
    }

    override fun onPause() {
        if (!isChangingConfigurations) viewModel.saveData()
        super.onPause()
    }

    override fun onStop() {
        if (serviceBound && !playersService.isWorking) applicationContext.stopService(intent)
        applicationContext.unbindService(serviceConnection)
        super.onStop()
    }

    override fun onDestroy() {
        unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }

    override fun onTimeSet(view: TimePicker?, hour: Int, minute: Int) {
        if (serviceBound && (hour != 0 || minute != 0)) {
            playersService.startTimer(hour, minute)
        }
        observeServiceTimer()
    }

    private fun setupOnBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this) {
            if (serviceBound && playersService.isWorking) {
                moveTaskToBack(false)
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun setupAdapter() {
        tracksListAdapter = TracksListAdapter { track, isPlaying, isFavourite ->
            viewModel.updateItem(track, isPlaying, isFavourite)
        }.also {
            it.registerAdapterDataObserver(object : AdapterDataObserver() {
                override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                    binding.playersRecyclerView.scrollToPosition(fromPosition)
                }
            })
        }
        binding.playersRecyclerView.adapter = tracksListAdapter
        observeTrackListState()
    }

    private fun setupMainSwitch() {
        with(binding.mainSwitch) {
            setOnClickListener { viewModel.setMainSwitchState(isChecked) }
            setOnCheckedChangeListener { compoundButton, checked ->
                initMainSwitchBackgroundTransitions(compoundButton, checked)
            }
            isSaveEnabled = false
        }
        observeMainSwitchState()
    }

    private fun setupTimer() {
        with(binding) {
            timerStartButton.setOnClickListener {
                if (!timerView.isVisible) {
                    TimePickerDialogFragment().show(supportFragmentManager, "timePicker")
                }
            }
            timerRefreshButton.setOnClickListener {
                if (serviceBound) {
                    playersService.stopTimer()
                }
            }
        }
        observeTimerStatus()
    }

    private fun setupBroadcastReceiver() {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    PlayersService.BROADCAST_ACTION_STOP_TIMER -> {
                        stopTimer()
                    }
                    PlayersService.BROADCAST_ACTION_STOP_PLAYERS -> {
                        stopPlayers()
                        viewModel.setMainSwitchState(false)
                    }
                }
            }
        }
        val intentFilter = IntentFilter().apply {
            addAction(PlayersService.BROADCAST_ACTION_STOP_TIMER)
            addAction(PlayersService.BROADCAST_ACTION_STOP_PLAYERS)
        }
        registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun observeTrackListState() {
        viewModel.tracksListState.observe(this) { tracks ->
            val sortedList =
                tracks.sortedWith(compareBy<Track> { !it.isFavourite }.thenBy { it.trackName })
            tracksListAdapter.submitList(sortedList)
            if (serviceBound) playersService.initiatePlaylist(tracks)
        }
    }

    private fun observeMainSwitchState() {
        viewModel.mainSwitchState.observe(this) { binding.mainSwitch.isChecked = it }
    }

    private fun observeTimerStatus() {
        viewModel.timerStatus.observe(this) { timerStatus ->
            with(binding) {
                with(timerStatus) {
                    timerView.text = currentTime
                    timerRefreshButton.visibility = if (isActive) View.VISIBLE else View.GONE
                    timerView.visibility = if (isActive) View.VISIBLE else View.GONE
                    if (isActive) timerStartButton.hide() else timerStartButton.show()
                    if (isFinished) viewModel.setMainSwitchState(false)
                }
            }
        }
    }

    private fun observeServiceTimer() {
        if (serviceBound && serviceTimerObserver == null) {
            serviceTimerObserver = Observer<PlayersService.TimerStatus> {
                viewModel.setTimerStatus(it)
            }.also { playersService.timerStatus.observe(this, it) }
        }
    }

    private fun stopPlayers() {
        if (serviceBound) playersService.stopPlayers()
    }

    private fun stopTimer() {
        if (serviceBound) playersService.stopTimer(false)
    }

    private fun setupServiceConnection() {
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                playersService = (binder as PlayersServiceBinder).service
                serviceBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                serviceBound = false
            }
        }
    }

    private fun initMainSwitchBackgroundTransitions(
        compoundButton: CompoundButton,
        checked: Boolean,
    ) {
        val animationsDuration = 300L
        val colorAccent: Int
        val colorAccentDark: Int
        val colorTransparentBlack: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            colorAccent = resources.getColor(R.color.accent, theme)
            colorAccentDark = resources.getColor(R.color.accentDark, theme)
            colorTransparentBlack = resources.getColor(R.color.transparentBlack, theme)
        } else {
            colorAccent = resources.getColor(R.color.accent)
            colorAccentDark = resources.getColor(R.color.accentDark)
            colorTransparentBlack = resources.getColor(R.color.transparentBlack)
        }
        val backgroundTransition = (compoundButton.background as TransitionDrawable).apply {
            isCrossFadeEnabled = true
        }
        val textColorAnimator = ObjectAnimator
            .ofArgb(compoundButton, "textColor", colorTransparentBlack, colorAccent)
            .setDuration(animationsDuration)
        val textShadowColorAnimator =
            ValueAnimator.ofArgb(Color.TRANSPARENT, colorAccentDark).apply {
                duration = animationsDuration
                addUpdateListener {
                    compoundButton
                        .setShadowLayer(8.0f, 0.0f, 0.0f, it.animatedValue as Int)
                }
            }
        if (checked) {
            backgroundTransition.startTransition(animationsDuration.toInt())
            textColorAnimator.start()
            textShadowColorAnimator.start()
        } else {
            backgroundTransition.reverseTransition(animationsDuration.toInt())
            textColorAnimator.reverse()
            textShadowColorAnimator.reverse()
        }
    }
}