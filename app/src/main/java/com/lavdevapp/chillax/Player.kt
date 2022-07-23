package com.lavdevapp.chillax

import android.content.Context
import android.media.MediaPlayer
import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Player(
    val trackId: Int,
    val playerName: String,
    val switchState: Boolean = false,
    val switchEnabled: Boolean = false
) : Parcelable {

    @Json(ignore = true)
    private var mediaPlayer: MediaPlayer? = null
    @Json(ignore = true)
    private val isPlaying = if (mediaPlayer == null) {
        false
    } else {
        mediaPlayer!!.isPlaying
    }

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    )

    private fun startTrack(context: Context) {
        mediaPlayer?.run {
            MediaPlayer.create(context, trackId)
            isLooping = true
            start()
        }
    }

    private fun stopTrack() {
        mediaPlayer?.let {
            it.stop()
            it.release()
        }
        mediaPlayer = null
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel?, flag: Int) {
        parcel?.let {
            with(it) {
                writeInt(trackId)
                writeString(playerName)
                writeByte(if (switchState) 1 else 0)
                writeByte(if (switchEnabled) 1 else 0)
            }
        }
    }

    companion object CREATOR : Parcelable.Creator<Player> {
        override fun createFromParcel(parcel: Parcel): Player {
            return Player(parcel)
        }

        override fun newArray(size: Int): Array<Player?> {
            return arrayOfNulls(size)
        }
    }
}