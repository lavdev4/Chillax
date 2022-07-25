package com.lavdevapp.chillax

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.lavdevapp.chillax.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        viewModel.tracksList.observe(this) {
            adapter.submitList(it)
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