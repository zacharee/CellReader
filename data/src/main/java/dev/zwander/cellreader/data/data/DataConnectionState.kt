package dev.zwander.cellreader.data.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DataConnectionState(
    val subId: Int,
    val state: Int,
    val networkType: Int
) : Parcelable
