package dev.zwander.cellreader.data.wrappers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.telephony.CellInfo
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.util.Base64
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.allAccessRulesCompat
import dev.zwander.cellreader.data.cardIdCompat
import dev.zwander.cellreader.data.util.onAvail
import dev.zwander.cellreader.data.util.withMinApi
import dev.zwander.cellreader.data.util.withTryCatch
import java.io.ByteArrayOutputStream

data class SubscriptionInfoWrapper(
    val id: Int,
    val iccId: String?,
    val simSlotIndex: Int,
    val displayName: String?,
    val carrierName: String?,
    val carrierId: Int,
    val nameSource: Int,
    val iconTint: Int,
    val number: String?,
    val dataRoaming: Int,
    val iconBitmap: String?,
    val mcc: String?,
    val mnc: String?,
    val ehplmns: List<String>?,
    val hplmns: List<String>?,
    val countryIso: String?,
    val embedded: Boolean?,
    val accessRules: List<UiccAccessRuleWrapper>?,
    val cardString: String?,
    val cardId: String?,
    val opportunistic: Boolean?,
    val groupUuid: String?,
    val groupOwner: String?,
    val groupDisabled: Boolean?,
    val profileClass: Int,
    val subscriptionType: Int,
    val uiccApplicationsEnabled: Boolean?,
    val serviceCapabilities: Set<Int>?,
    val isOnlyNonTerrestrialNetwork: Boolean?,
    val usageSetting: Int?,
    val transferStatus: Int?,
    val satelliteESOSSupported: Boolean?,
) {
    @SuppressLint("MissingPermission", "InlinedApi")
    constructor(info: SubscriptionInfo, context: Context) : this(
        id = info.subscriptionId,
        iccId = info.iccId,
        simSlotIndex = info.simSlotIndex,
        displayName = info.displayName?.toString(),
        carrierName = info.carrierName?.toString(),
        carrierId = withMinApi(Build.VERSION_CODES.Q, CellInfo.UNAVAILABLE) {
            info.carrierId
        },
        nameSource = try {
            SubscriptionInfo::class.java
                .getMethod("getNameSource").invoke(info)
                ?.toString()?.toIntOrNull() ?: CellInfo.UNAVAILABLE
        } catch (_: NoSuchMethodException) {
            SubscriptionInfo::class.java
                .getDeclaredMethod("getDisplayNameSource").invoke(info)
                ?.toString()?.toIntOrNull() ?: CellInfo.UNAVAILABLE
        },
        iconTint = info.iconTint,
        number = @Suppress("Deprecation")
                withMinApi(Build.VERSION_CODES.TIRAMISU, info.number) {
                (context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager).getPhoneNumber(
                    info.subscriptionId
                )
            },
        dataRoaming = info.dataRoaming,
        iconBitmap = ByteArrayOutputStream().use { output ->
            val bmp = info.createIconBitmap(context)
            bmp.compress(Bitmap.CompressFormat.PNG, 100, output)
            bmp.recycle()
            Base64.encodeToString(output.toByteArray(), Base64.DEFAULT)
        },
        mcc = @Suppress("DEPRECATION")
        withMinApi(Build.VERSION_CODES.Q, info.mcc.onAvail { it.toString() }) {
            info.mccString
        },
        mnc = @Suppress("DEPRECATION")
        withMinApi(Build.VERSION_CODES.Q, info.mnc.onAvail { it.toString() }) {
            info.mncString
        },
        ehplmns = withMinApi(Build.VERSION_CODES.Q, listOf()) {
            info.ehplmns
        },
        hplmns = withMinApi(Build.VERSION_CODES.Q, listOf()) {
            info.hplmns
        },
        countryIso = info.countryIso,
        embedded = withMinApi(Build.VERSION_CODES.O_MR1) {
            info.isEmbedded
        },
        accessRules = info.allAccessRulesCompat.map { UiccAccessRuleWrapper(it) },
        cardString = withMinApi(Build.VERSION_CODES.Q) {
            info.cardString
        },
        cardId = info.cardIdCompat,
        opportunistic = withMinApi(Build.VERSION_CODES.Q) {
            info.isOpportunistic
        },
        groupUuid = withMinApi(Build.VERSION_CODES.Q) {
            info.groupUuid?.toString()
        },
        groupOwner = withMinApi(Build.VERSION_CODES.Q) {
            info.groupOwner
        },
        groupDisabled = withMinApi(Build.VERSION_CODES.Q) {
            info.isGroupDisabled
        },
        profileClass = withMinApi(Build.VERSION_CODES.Q, CellInfo.UNAVAILABLE) {
            info.profileClass
        },
        subscriptionType = withMinApi(Build.VERSION_CODES.Q, CellInfo.UNAVAILABLE) {
            info.subscriptionType
        },
        uiccApplicationsEnabled = withMinApi(Build.VERSION_CODES.R) {
            info.areUiccApplicationsEnabled()
        },
        serviceCapabilities = withMinApi(Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            info.serviceCapabilities
        },
        isOnlyNonTerrestrialNetwork = withMinApi(Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            info.isOnlyNonTerrestrialNetwork
        },
        usageSetting = withMinApi(Build.VERSION_CODES.TIRAMISU) {
            info.usageSetting
        },
        transferStatus = withMinApi(Build.VERSION_CODES.TIRAMISU) {
            withTryCatch {
                info.transferStatus
            }
        },
        satelliteESOSSupported = withMinApi(Build.VERSION_CODES.BAKLAVA) {
            info.isSatelliteESOSSupported
        },
    )

    val iconBitmapBmp: Bitmap?
        get() = iconBitmap?.let {
            Base64.decode(it, Base64.DEFAULT).run {
                BitmapFactory.decodeByteArray(this, 0, size)
            }
        }

    companion object {
        fun transferStatusToStringRes(status: Int): Int {
            return when (status) {
                SubscriptionManager.TRANSFER_STATUS_CONVERTED -> R.string.transfer_status_converted
                SubscriptionManager.TRANSFER_STATUS_NONE -> R.string.transfer_status_none
                SubscriptionManager.TRANSFER_STATUS_TRANSFERRED_OUT -> R.string.transfer_status_transferred_out
                else -> R.string.unknown
            }
        }
    }
}