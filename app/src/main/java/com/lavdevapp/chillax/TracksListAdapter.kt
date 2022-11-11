package com.lavdevapp.chillax

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lavdevapp.chillax.databinding.AdapterPlayersListBinding


class TracksListAdapter(
    private val onItemChangedCallback: (
        track: Track,
        switchChecked: Boolean,
        favouritesChecked: Boolean
    ) -> Unit
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
            playerSwitch.isChecked = track.isPlaying
            playerSwitch.isEnabled = track.isEnabled
            playerFavourites.isChecked = track.isFavourite
        }
    }

    override fun onBindViewHolder(
        holder: TracksListViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        if (payloads.isNotEmpty()) {
            val bundle = (payloads[0] as Bundle)
            with(holder.binding) {
                playerSwitch.isChecked = bundle.getBoolean(PAYLOAD_SWITCH_STATE_KEY)
                playerSwitch.isEnabled = bundle.getBoolean(PAYLOAD_SWITCH_ENABLED_STATE_KEY)
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    inner class TracksListViewHolder(val binding: AdapterPlayersListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            with(binding.playerSwitch) {
                setOnClickListener {
                    val actualItem = getItem(adapterPosition)
                    onItemChangedCallback(actualItem, isChecked, actualItem.isFavourite)
                }
            }
            with(binding.playerFavourites) {
                setOnClickListener {
                    val actualItem = getItem(adapterPosition)
                    onItemChangedCallback(actualItem, actualItem.isPlaying, isChecked)
                }
            }
        }
    }

    private class TracksListDiffUtil : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem.trackUri == newItem.trackUri
        }

        override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
            return (oldItem.isPlaying == newItem.isPlaying) &&
                    (oldItem.isEnabled == newItem.isEnabled)
        }

        override fun getChangePayload(oldItem: Track, newItem: Track): Any {
            return Bundle().apply {
                putBoolean(PAYLOAD_SWITCH_STATE_KEY, newItem.isPlaying)
                putBoolean(PAYLOAD_SWITCH_ENABLED_STATE_KEY, newItem.isEnabled)
            }
        }
    }

    companion object {
        const val PAYLOAD_SWITCH_STATE_KEY = "checkbox_state"
        const val PAYLOAD_SWITCH_ENABLED_STATE_KEY = "card_enabled_state"
    }
}