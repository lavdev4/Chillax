package com.lavdevapp.chillax

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import com.lavdevapp.chillax.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: AppViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AppViewModel::class.java]

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