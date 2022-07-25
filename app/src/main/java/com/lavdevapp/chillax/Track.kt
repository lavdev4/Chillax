package com.lavdevapp.chillax

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.net.URI

@JsonClass(generateAdapter = true)
data class Track(
    val trackName: String,
    val trackUri: String,
    val switchState: Boolean = false,
    val switchEnabled: Boolean = false
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel?, flag: Int) {
        parcel?.let {
            with(it) {
                writeString(trackName)
                writeString(trackUri)
                writeByte(if (switchState) 1 else 0)
                writeByte(if (switchEnabled) 1 else 0)
            }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Track> {
        override fun createFromParcel(parcel: Parcel): Track {
            return Track(parcel)
        }

        override fun newArray(size: Int): Array<Track?> {
            return arrayOfNulls(size)
        }
    }
}