package com.lavdevapp.chillax

object DefaultTracksStates {
    val tracks: List<Track>
        get() {
            return listOf(
                //Must have unique names
                //Clean app cache after change
                Track("rain_sound", getUri(R.raw.rain_sound), getFloat(10)),
                Track("birds_sound", getUri(R.raw.birds_sound), getFloat(100))
            )
        }

    private fun getUri(resId: Int) = "android.resource://com.lavdevapp.chillax/raw/$resId"

    private fun getFloat(percent: Byte) = percent / 100.0f
}