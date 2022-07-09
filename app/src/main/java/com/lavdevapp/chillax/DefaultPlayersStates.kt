package com.lavdevapp.chillax

object DefaultPlayersStates {
    val players: List<Player>
        get() {
            return listOf(
                Player(R.raw.rain_sound, "rain_sound"),
                Player(R.raw.birds_sound, "birds_sound")
            )
        }
}