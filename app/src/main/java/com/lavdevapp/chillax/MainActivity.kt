package com.lavdevapp.chillax

import android.app.TimePickerDialog
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.TimePicker
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.lavdevapp.chillax.PlayersService.PlayersServiceBinder
import com.lavdevapp.chillax.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<AppViewModel>()
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
        tracksListAdapter = TracksListAdapter { track, isChecked ->
            viewModel.setItemChecked(track, isChecked)
        }
        binding.playersRecyclerView.adapter = tracksListAdapter
        observeTrackListState()
    }

    private fun setupMainSwitch() {
        with(binding.mainSwitch) {
            setOnClickListener { viewModel.setMainSwitchState(isChecked) }
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
                    PlayersService.NOTIFICATION_ACTION_STOP_TIMER -> {
                        stopTimer()
                    }
                    PlayersService.NOTIFICATION_ACTION_STOP_PLAYERS -> {
                        stopPlayers()
                        viewModel.setMainSwitchState(false)
                    }
                }
            }
        }
        val intentFilter = IntentFilter().apply {
            addAction(PlayersService.NOTIFICATION_ACTION_STOP_TIMER)
            addAction(PlayersService.NOTIFICATION_ACTION_STOP_PLAYERS)
        }
        registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun observeTrackListState() {
        viewModel.tracksListState.observe(this) {
            tracksListAdapter.submitList(it)
            if (serviceBound) playersService.initiatePlaylist(it)
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
        if (serviceBound) playersService.stopPlayersIfActive()
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
}