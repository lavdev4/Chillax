package com.lavdevapp.chillax

import android.app.TimePickerDialog
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.TimePicker
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
        Log.d("app_log", "--------------------------------")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupAdapter()
        setupMainSwitch()
        setupTimer()
        setupBroadcastReceiver()
        restoreMainSwitchState()
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

    private fun setupAdapter() {
        tracksListAdapter = TracksListAdapter { track, isChecked ->
            viewModel.setItemChecked(track, isChecked)
        }
        binding.playersRecyclerView.adapter = tracksListAdapter
        observeTrackListState()
    }

    private fun setupMainSwitch() {
        with(binding.mainSwitch) {
            setOnCheckedChangeListener { _, isChecked ->
                viewModel.setMainSwitchState(isChecked)
                Log.d("app_log", "main switch checked $isChecked")
            }
            isSaveEnabled = false
        }
    }

    private fun setupTimer() {
        with(binding) {
            timerStartButton.setOnClickListener {
                if (!timerView.isVisible) {
                    TimePickerDialogFragment().show(supportFragmentManager, "timePicker")
                }
            }
            timerRefreshButton.setOnClickListener {
                if (serviceBound) playersService.stopTimer()
            }
        }
        observeTimerStatus()
    }

    private fun setupBroadcastReceiver() {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    PlayersService.NOTIFICATION_ACTION_STOP_TIMER -> stopTimer()
                    PlayersService.NOTIFICATION_ACTION_STOP_PLAYERS -> {
                        stopPlayers()
                        binding.mainSwitch.isChecked = false
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
            Log.d("app_log", "list submitted $it")
        }
    }

    private fun observeTimerStatus() {
        viewModel.timerStatus.observe(this) {
            with(binding) {
                timerView.text = it.currentTime
                with(it.isActive) {
                    timerRefreshButton.visibility = if (this) View.VISIBLE else View.GONE
                    timerView.visibility = if (this) View.VISIBLE else View.GONE
                }
                if (it.isFinished) {
                    binding.mainSwitch.isChecked = false
                }
            }
            Log.d("app_timer", "timer text set: ${it.currentTime}")
        }
    }

    private fun observeServiceTimer() {
        if (serviceBound && serviceTimerObserver == null) {
            serviceTimerObserver = Observer<PlayersService.TimerStatus> {
                viewModel.setTimerStatus(it)
                Log.d("app_log", "service timer status emit ${it.currentTime}")
            }.also { playersService.timerStatus.observe(this, it) }
        }
    }

    private fun restoreMainSwitchState() {
        binding.mainSwitch.isChecked = viewModel.mainSwitchState.value ?: false
        Log.d("app_log", "main switch restored")
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