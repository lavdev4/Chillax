package com.lavdevapp.chillax

object DefaultTracksStates {
    val tracks: List<Track>
        get() {
            return listOf(
                //Must have unique names
                Track("rain_sound", getUri(R.raw.rain_sound)),
                Track("birds_sound", getUri(R.raw.birds_sound))
            )
        }

    private fun getUri(resId: Int) = "android.resource://com.lavdevapp.chillax/raw/$resId"
}