package com.lavdevapp.chillax

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lavdevapp.chillax.databinding.AdapterPlayersListBinding


class PlayersListAdapter(
    private val onCheckedChangeCallback: (player: Player, isChecked: Boolean) -> Unit,
    private val onEnabledChangeCallback: (areEnabled: Boolean) -> Unit
) : ListAdapter<Player, PlayersListAdapter.PlayersListViewHolder>(PlayersListDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayersListViewHolder {
        val binding = AdapterPlayersListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlayersListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayersListViewHolder, position: Int) {
        val player = getItem(position)
        with(holder.binding) {
            playerName.text = player.playerName
            playerSwitch.isChecked = player.switchState
            playerSwitch.isEnabled = player.switchEnabled
        }
        Log.d("app_log", "onBindViewHolder: ${player.playerName}")
    }

    fun setItemsEnabled(areEnabled: Boolean) {
        onEnabledChangeCallback(areEnabled)
    }

    inner class PlayersListViewHolder(val binding: AdapterPlayersListBinding) :
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

    class PlayersListDiffUtil : DiffUtil.ItemCallback<Player>() {

        override fun areItemsTheSame(oldItem: Player, newItem: Player): Boolean {
            return oldItem.trackId == newItem.trackId
        }

        override fun areContentsTheSame(oldItem: Player, newItem: Player): Boolean {
            Log.d("app_log", "areContentsTheSame: " +
                    "${oldItem.playerName} - " +
                    "${(oldItem.switchState == newItem.switchState) &&
                            (oldItem.switchEnabled == newItem.switchEnabled)}")
            return (oldItem.switchState == newItem.switchState) &&
                    (oldItem.switchEnabled == newItem.switchEnabled)
        }
    }
}