package dev.zwander.cellreader.data

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.wearable.DataMap

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