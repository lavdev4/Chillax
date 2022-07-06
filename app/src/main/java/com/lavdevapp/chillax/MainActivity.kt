package com.lavdevapp.chillax

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import android.os.Bundle
import android.widget.CompoundButton

class MainActivity : AppCompatActivity() {
    private val players = mutableListOf<Player>()
    private val switchesWithPlayers = mutableMapOf<SwitchMaterial, Player>()
    private val preferences by lazy { getPreferences(MODE_PRIVATE) }
    private lateinit var mainSwitch: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rainSound = Player(R.raw.rain_sound, "rain_sound")
        val birdsSound = Player(R.raw.birds_sound, "birds_sound")

        players.add(rainSound)
        players.add(birdsSound)

        players[0].switchState = preferences.getBoolean("rain_sound", false)
        players[1].switchState = preferences.getBoolean("birds_sound", false)

        val firstSwitch = findViewById<SwitchMaterial>(R.id.first_switch)
        switchesWithPlayers[firstSwitch] = players[0]
        val secondSwitch = findViewById<SwitchMaterial>(R.id.second_switch)
        switchesWithPlayers[secondSwitch] = players[1]

        for ((key, value) in switchesWithPlayers) {
            if (value.switchState) {
                key.isChecked = true
            }
        }
        mainSwitch = findViewById(R.id.main_switch)
        mainSwitch.setOnCheckedChangeListener { compoundButton, isChecked ->
            for (player in players) {
                if (player.switchState) {
                    togglePlayer(player)
                }
                toggleSwitchesEnabled()
            }
        }
        firstSwitch.setOnCheckedChangeListener { compoundButton, isChecked ->
            val player = switchesWithPlayers[firstSwitch]
            if (player != null) {
                togglePlayer(player)
                player.switchState = isChecked
            }
        }
        secondSwitch.setOnCheckedChangeListener { compoundButton, isChecked ->
            val player = switchesWithPlayers[secondSwitch]
            if (player != null) {
                togglePlayer(player)
                player.switchState = isChecked
            }
        }
    }

    override fun onStop() {
        val prefsEditor = preferences!!.edit()
        for (player in players) {
            prefsEditor.putBoolean(player.playerTag, player.switchState)
        }
        prefsEditor.apply()
        super.onStop()
    }

    private fun togglePlayer(player: Player) {
        if (player.isPlaying) {
            player.stopTrack()
        } else {
            player.startTrack(this)
        }
    }

    private fun toggleSwitchesEnabled() {
        for ((key) in switchesWithPlayers) {
            key.isEnabled = mainSwitch.isChecked
        }
    }
}