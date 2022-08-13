package com.lavdevapp.chillax

import android.app.TimePickerDialog
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.TimePicker
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.lavdevapp.chillax.PlayersService.PlayersServiceBinder
import com.lavdevapp.chillax.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<AppViewModel>()
    private lateinit var tracksListAdapter: TracksListAdapter
    private lateinit var playersService: PlayersService
    private lateinit var serviceConnection: ServiceConnection
    private var serviceBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("app_log", "--------------------------------")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupAdapter()
        observeTrackList()
        observeTimer()
        setupMainSwitchListener()
        setupTimerClickListener()
        setupTimerRefreshButtonClickListener()
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
        applicationContext.unbindService(serviceConnection)
        if (serviceBound && !playersService.isWorking) applicationContext.stopService(intent)
        super.onStop()
    }

    override fun onTimeSet(view: TimePicker?, hour: Int, minute: Int) {
        if (serviceBound && (hour != 0 || minute != 0)) {
            playersService.startTimer(hour, minute)
        }
    }

    private fun setupAdapter() {
        tracksListAdapter = TracksListAdapter(
            { track, isChecked -> viewModel.setItemChecked(track, isChecked) },
            { areEnabled -> viewModel.setItemsEnabled(areEnabled) }
        )
        binding.playersRecyclerView.adapter = tracksListAdapter
    }

    private fun observeTrackList() {
        viewModel.tracksList.observe(this) {
            tracksListAdapter.submitList(it)
            if (serviceBound) playersService.initiatePlaylist(it)
            Log.d("app_log", "list submitted")
        }
    }

    private fun observeTimer() {
        viewModel.timerStatus.observe(this) {
            with(binding) {
                timerView.text = it.currentTime
                timerRefreshButton.visibility = if (it.isActive) View.VISIBLE else View.GONE
                if (it.isFinished) mainSwitch.isChecked = false
            }
            Log.d("app_timer", "timer text set: ${it.currentTime}")
        }
    }

    private fun setupMainSwitchListener() {
        with(binding.mainSwitch) {
            setOnCheckedChangeListener { _, isChecked ->
                tracksListAdapter.setItemsEnabled(isChecked)
            }
            // TODO: during to the isSaveEnabled false is loosing state on rotating
            isSaveEnabled = false
        }
    }

    private fun setupTimerClickListener() {
        binding.timerView.setOnClickListener {
            TimePickerDialogFragment().show(supportFragmentManager, "timePicker")
        }
    }

    private fun setupTimerRefreshButtonClickListener() {
        binding.timerRefreshButton.setOnClickListener {
            if (serviceBound) playersService.stopTimer()
        }
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