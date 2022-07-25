package com.lavdevapp.chillax

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.lavdevapp.chillax.PlayersService.PlayersServiceBinder
import com.lavdevapp.chillax.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<AppViewModel>()
    private lateinit var service: PlayersService
    private var bound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("app_log", "--------------------------------")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = TracksListAdapter(
            { track, isChecked -> viewModel.setItemChecked(track, isChecked) },
            { areEnabled -> viewModel.setItemsEnabled(areEnabled) }
        )
        binding.playersRecyclerView.adapter = adapter

        binding.mainSwitch.setOnCheckedChangeListener { _, isChecked ->
            adapter.setItemsEnabled(isChecked)
        }
        // TODO: fix
        //binding.mainSwitch.isChecked = false

        //observe was here

        //------------------------------------------------------------

        val connection = object : ServiceConnection {

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                this@MainActivity.service = (service as PlayersServiceBinder).service
                bound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                bound = false
            }
        }

        val intent = Intent(this, PlayersService::class.java)
        ContextCompat.startForegroundService(this, intent)
        applicationContext.bindService(intent, connection, 0)

        viewModel.tracksList.observe(this) {
            adapter.submitList(it)
            if (bound) {
                service.initiatePlaylist(it)
            }
            Log.d("app_log", "list submitted")
        }
    }

    override fun onPause() {
        if (!isChangingConfigurations) {
            viewModel.saveData()
        }
        super.onPause()
    }
}