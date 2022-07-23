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

        val adapter = PlayersListAdapter(
            { player, isChecked -> viewModel.setItemChecked(player, isChecked) },
            { areEnabled -> viewModel.setItemsEnabled(areEnabled) }
        )
        binding.playersRecyclerView.adapter = adapter

        viewModel.playersList.observe(this) {
            adapter.submitList(it)
            Log.d("app_log", "list submitted")
        }

        binding.mainSwitch.setOnCheckedChangeListener { _, isChecked ->
            adapter.setItemsEnabled(isChecked)
        }
    }

    override fun onPause() {
        if (!isChangingConfigurations) {
            viewModel.saveData()
        }
        super.onPause()
    }
}