package dev.zwander.cellreader.data

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.telephony.*
import com.google.android.gms.wearable.DataMap
import io.gsonfire.GsonFireBuilder

object InfoParceler {
    private val gson = GsonFireBuilder()
        .wrap(CellInfoGsm::class.java, CellInfoGsm::class.java.canonicalName)
        .wrap(CellIdentityGsm::class.java, CellIdentityGsm::class.java.canonicalName)
        .wrap(CellSignalStrengthGsm::class.java, CellSignalStrengthGsm::class.java.canonicalName)
        .wrap(CellInfoCdma::class.java, CellInfoCdma::class.java.canonicalName)
        .wrap(CellIdentityCdma::class.java, CellIdentityCdma::class.java.canonicalName)
        .wrap(CellSignalStrengthCdma::class.java, CellSignalStrengthCdma::class.java.canonicalName)
        .wrap(CellInfoLte::class.java, CellInfoLte::class.java.canonicalName)
        .wrap(CellIdentityLte::class.java, CellIdentityLte::class.java.canonicalName)
        .wrap(CellSignalStrengthLte::class.java, CellSignalStrengthLte::class.java.canonicalName)
        .wrap(CellIdentityTdscdma::class.java, CellIdentityTdscdma::class.java.canonicalName)
        .wrap(CellInfoWcdma::class.java, CellInfoWcdma::class.java.canonicalName)
        .wrap(CellIdentityWcdma::class.java, CellIdentityWcdma::class.java.canonicalName)
        .wrap(CellSignalStrengthWcdma::class.java, CellSignalStrengthWcdma::class.java.canonicalName)
        .apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                wrap(CellInfoTdscdma::class.java, CellInfoTdscdma::class.java.canonicalName)
                wrap(CellSignalStrengthTdscdma::class.java, CellSignalStrengthTdscdma::class.java.canonicalName)
                wrap(CellInfoNr::class.java, CellInfoNr::class.java.canonicalName)
                wrap(CellIdentityNr::class.java, CellIdentityNr::class.java.canonicalName)
                wrap(CellSignalStrengthNr::class.java, CellSignalStrengthNr::class.java.canonicalName)
            }
        }
        .registerTypeSelector(CellInfo::class.java) { element ->
            val name = element.asJsonObject.keySet().first()

            try {
                @Suppress("UNCHECKED_CAST")
                CellInfo::class.java.classLoader?.loadClass(name) as Class<out CellInfo>?
            } catch (e: Exception) {
                null
            }
        }
        .createGsonBuilder()
        .create()

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