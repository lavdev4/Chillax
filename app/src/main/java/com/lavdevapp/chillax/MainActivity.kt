package com.lavdevapp.chillax

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.lavdevapp.chillax.PlayersService.PlayersServiceBinder
import com.lavdevapp.chillax.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
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
    }

    override fun onStart() {
        super.onStart()
        setupServiceConnection()
        playersServiceIntent = Intent(this, PlayersService::class.java)
        applicationContext.bindService(playersServiceIntent, serviceConnection, 0)
        applicationContext.startService(playersServiceIntent)
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