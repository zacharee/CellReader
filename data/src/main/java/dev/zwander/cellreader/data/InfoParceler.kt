package dev.zwander.cellreader.data

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.telephony.*
import com.google.android.gms.wearable.DataMap
import io.gsonfire.GsonFireBuilder

object InfoParceler {


    fun DataMap.putParcelable(key: String, item: Parcelable) {
        val parcel = Parcel.obtain()
        item.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        putByteArray(key, parcel.marshall())
        parcel.recycle()
    }

    fun <T> DataMap.getParcelable(key: String, creator: Parcelable.Creator<T>): T? {
        val byteArray = getByteArray(key) ?: return null
        val parcel = Parcel.obtain()
        parcel.unmarshall(byteArray, 0, byteArray.size)
        parcel.setDataPosition(0)
        val obj = creator.createFromParcel(parcel) as T
        parcel.recycle()
        return obj
    }
}