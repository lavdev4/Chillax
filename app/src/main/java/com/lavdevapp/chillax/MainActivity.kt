package com.lavdevapp.chillax

import android.app.TimePickerDialog
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.TimePicker
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import com.lavdevapp.chillax.PlayersService.PlayersServiceBinder
import com.lavdevapp.chillax.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<AppViewModel>()
    private lateinit var playersService: PlayersService
    private lateinit var playersServiceIntent: Intent
    private lateinit var serviceConnection: ServiceConnection
    private lateinit var tracksListAdapter: TracksListAdapter
    private var serviceBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("app_log", "--------------------------------")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupAdapter()
        observeTrackList()
        setupMainSwitchListener()
        setupTimerClickListener()
    }

    override fun onStart() {
        super.onStart()
        setupServiceConnection()
        playersServiceIntent = Intent(this, PlayersService::class.java)
        applicationContext.startService(playersServiceIntent)
        applicationContext.bindService(playersServiceIntent, serviceConnection, 0)
    }

    override fun onPause() {
        if (!isChangingConfigurations) {
            viewModel.saveData()
        }
        super.onPause()
    }

    override fun onStop() {
        if (serviceBound && !playersService.isActive) {
            applicationContext.stopService(intent)
        }
        applicationContext.unbindService(serviceConnection)
        super.onStop()
    }

    override fun onTimeSet(view: TimePicker?, hour: Int, minute: Int) {
        if (serviceBound) {
            playersService.startTimer(hour, minute)
            playersService.countDownTimeLeft.observe(this) {
                binding.timerView.text = it
                Log.d("app_timer", "timer text set: $it")
            }
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
            if (serviceBound) {
                playersService.initiatePlaylist(it)
            }
            Log.d("app_log", "list submitted")
        }
    }

    private fun setupMainSwitchListener() {
        binding.mainSwitch.setOnCheckedChangeListener { _, isChecked ->
            tracksListAdapter.setItemsEnabled(isChecked)
        }
    }

    private fun setupTimerClickListener() {
        binding.timerView.setOnClickListener {
            // TODO: new instance every call ??
            TimePickerDialogFragment().show(supportFragmentManager, "timePicker")
        }
    }

    private fun setupServiceConnection() {
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                this@MainActivity.playersService = (service as PlayersServiceBinder).service
                serviceBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                serviceBound = false
            }
        }
    }
}