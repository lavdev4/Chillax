package com.lavdevapp.chillax

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lavdevapp.chillax.databinding.AdapterPlayersListBinding


class TracksListAdapter(
    private val onCheckedChangeCallback: (track: Track, isChecked: Boolean) -> Unit,
    private val onEnabledChangeCallback: (areEnabled: Boolean) -> Unit
) : ListAdapter<Track, TracksListAdapter.TracksListViewHolder>(TracksListDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TracksListViewHolder {
        val binding = AdapterPlayersListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TracksListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TracksListViewHolder, position: Int) {
        val track = getItem(position)
        with(holder.binding) {
            playerName.text = track.trackName
            playerSwitch.isChecked = track.switchState
            playerSwitch.isEnabled = track.switchEnabled
        }
        Log.d("app_log", "onBindViewHolder: ${track.trackName}")
    }

    fun setItemsEnabled(areEnabled: Boolean) {
        onEnabledChangeCallback(areEnabled)
    }

    inner class TracksListViewHolder(val binding: AdapterPlayersListBinding) :
        RecyclerView.ViewHolder(binding.root) {
            private val switchMaterial = binding.playerSwitch

            init {
                switchMaterial.setOnClickListener {
                    val actualItem = getItem(adapterPosition)
                    onCheckedChangeCallback(actualItem, switchMaterial.isChecked)
                }
                Log.d("app_log", "view holder init")
            }
        }

    class TracksListDiffUtil : DiffUtil.ItemCallback<Track>() {

        override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem.trackUri == newItem.trackUri
        }

        override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
            Log.d("app_log", "areContentsTheSame: " +
                    "${oldItem.trackName} - " +
                    "${(oldItem.switchState == newItem.switchState) &&
                            (oldItem.switchEnabled == newItem.switchEnabled)}")
            return (oldItem.switchState == newItem.switchState) &&
                    (oldItem.switchEnabled == newItem.switchEnabled)
        }
    }
}