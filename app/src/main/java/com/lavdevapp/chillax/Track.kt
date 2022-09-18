package com.lavdevapp.chillax

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Track(
    val trackName: String,
    val trackUri: String,
    val switchState: Boolean = false,
    @Json(ignore = true) val switchEnabled: Boolean = false,
) : Parcelable {

    @Json(ignore = true)
    val parsedUri: Uri = Uri.parse(trackUri)

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flag: Int) {
        with(parcel) {
            writeString(trackName)
            writeString(trackUri)
            writeByte(if (switchState) 1 else 0)
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