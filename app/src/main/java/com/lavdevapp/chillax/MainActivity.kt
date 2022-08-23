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
    private var serviceBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("app_log", "--------------------------------")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupAdapter()
        setupMainSwitch()
        restoreMainSwitchState()
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
        with(binding){
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
        broadcastReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    PlayersService.NOTIFICATION_ACTION_STOP_TIMER -> stopTimer()
                    PlayersService.NOTIFICATION_ACTION_STOP_PLAYERS -> setMainSwitchState(false)
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
                    setMainSwitchState(false)
                }
            }
            Log.d("app_timer", "timer text set: ${it.currentTime}")
        }
    }

    private fun restoreMainSwitchState() {
        binding.mainSwitch.isChecked = viewModel.mainSwitchState.value ?: false
        Log.d("app_log", "main switch restored")
    }

    // TODO:
    private fun stopTimer() {
        if (serviceBound) playersService.stopTimer(false)
    }

    private fun setMainSwitchState(state: Boolean) {
        binding.mainSwitch.isChecked = state
        // TODO:
        viewModel.setMainSwitchState(state)
    }

    private fun setupServiceConnection() {
        serviceConnection = object : ServiceConnection {
            private val observer = Observer<PlayersService.TimerStatus> {
                viewModel.setTimerStatus(it)
            }

            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                playersService = (binder as PlayersServiceBinder).service
                playersService.timerStatus.observe(this@MainActivity, observer)
                serviceBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                playersService.timerStatus.removeObserver(observer)
                serviceBound = false
            }
        }
    }
}