package com.lavdevapp.chillax

import android.net.Uri

object DefaultTracksStates {
    val tracks: List<Track>
        get() {
            return listOf(
                // TODO: refactor needed
                //Must have unique names
                Track("rain_sound", getUri(R.raw.rain_sound)),
                Track("birds_sound", getUri(R.raw.birds_sound))
            )
        }

    private fun getUri(resId: Int) = "android.resource://com.lavdevapp.chillax/raw/$resId"
}