package com.lavdevapp.chillax

object DefaultTracksStates {
    val tracks: List<Track>
        get() {
            return listOf(
                //Must have unique name
                //Clean app cache after change
                Track("Birds 1", getUri(R.raw.birds_1), getFloat(50)),
                Track("Birds 2", getUri(R.raw.birds_2), getFloat(60)),
                Track("Farm", getUri(R.raw.farm), getFloat(60)),
                Track("Forest 1", getUri(R.raw.forest_1), getFloat(20)),
                Track("Forest 2", getUri(R.raw.forest_2), getFloat(70)),
                Track("Forest 3", getUri(R.raw.forest_3), getFloat(40)),
                Track("Forest 4", getUri(R.raw.forest_4), getFloat(50)),
                Track("Light rain thunder", getUri(R.raw.light_rain_thunder), getFloat(100)),
                Track("Ocean 1", getUri(R.raw.ocean_1), getFloat(30)),
                Track("Ocean 2", getUri(R.raw.ocean_2), getFloat(40)),
                Track("Ocean seagulls", getUri(R.raw.ocean_seagulls), getFloat(20)),
                Track("Rain 1", getUri(R.raw.rain_1), getFloat(30)),
                Track("Rain 2", getUri(R.raw.rain_2), getFloat(30)),
                Track("Rain 3", getUri(R.raw.rain_3), getFloat(40)),
                Track("Rain 4", getUri(R.raw.rain_4), getFloat(70)),
                Track("Rain 5", getUri(R.raw.rain_5), getFloat(80)),
                Track("Rain 6", getUri(R.raw.rain_6), getFloat(60)),
                Track("Rain 7", getUri(R.raw.rain_7), getFloat(50)),
                Track("Rain thunder", getUri(R.raw.rain_thunder), getFloat(50)),
                Track("Stream", getUri(R.raw.stream), getFloat(10)),
                Track("Waves 1", getUri(R.raw.waves_1), getFloat(60)),
                Track("Waves 2", getUri(R.raw.waves_2), getFloat(40)),
                Track("Waves 3", getUri(R.raw.waves_3), getFloat(70)))
        }

    private fun getUri(resId: Int) = "android.resource://com.lavdevapp.chillax/raw/$resId"

    private fun getFloat(percent: Byte) = percent / 100.0f
}