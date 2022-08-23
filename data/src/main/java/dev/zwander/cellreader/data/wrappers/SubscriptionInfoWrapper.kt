package dev.zwander.cellreader.data.wrappers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.telephony.CellInfo
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import dev.zwander.cellreader.data.allAccessRulesCompat
import dev.zwander.cellreader.data.cardIdCompat
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
    val iconBitmap: ByteArray?,
    val mcc: String?,
    val mnc: String?,
    val ehplmns: ArrayList<String>?,
    val hplmns: ArrayList<String>?,
    val countryIso: String?,
    val embedded: Boolean,
    val accessRules: ArrayList<UiccAccessRuleWrapper>?,
    val cardString: String?,
    val cardId: String?,
    val opportunistic: Boolean,
    val groupUuid: String?,
    val groupOwner: String?,
    val groupDisabled: Boolean,
    val profileClass: Int,
    val subscriptionType: Int,
    val uiccApplicationsEnabled: Boolean
) {
    @SuppressLint("MissingPermission", "InlinedApi")
    constructor(info: SubscriptionInfo, context: Context) : this(
        info.subscriptionId,
        info.iccId,
        info.simSlotIndex,
        info.displayName?.toString(),
        info.carrierName?.toString(),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) info.carrierId else CellInfo.UNAVAILABLE,
        info.nameSource,
        info.iconTint,
        if (Build.VERSION.SDK_INT >= 33) {
            (context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager).getPhoneNumber(info.subscriptionId)
        } else {
            info.number
        },
        info.dataRoaming,
        ByteArrayOutputStream().use { output ->
            val bmp = info.createIconBitmap(context)
            bmp.compress(Bitmap.CompressFormat.PNG, 100, output)
            bmp.recycle()
            output.toByteArray()
        },
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) info.mccString else info.mcc.toString(),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) info.mncString else info.mnc.toString(),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ArrayList(info.ehplmns) else arrayListOf(),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ArrayList(info.hplmns) else arrayListOf(),
        info.countryIso,
        info.isEmbedded,
        ArrayList(info.allAccessRulesCompat.map { UiccAccessRuleWrapper(it) }),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) info.cardString else null,
        info.cardIdCompat,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) info.isOpportunistic else false,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) info.groupUuid?.toString() else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) info.groupOwner else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) info.isGroupDisabled else false,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) info.profileClass else CellInfo.UNAVAILABLE,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) info.subscriptionType else CellInfo.UNAVAILABLE,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) info.areUiccApplicationsEnabled() else false
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SubscriptionInfoWrapper

        if (id != other.id) return false
        if (iccId != other.iccId) return false
        if (simSlotIndex != other.simSlotIndex) return false
        if (displayName != other.displayName) return false
        if (carrierName != other.carrierName) return false
        if (carrierId != other.carrierId) return false
        if (nameSource != other.nameSource) return false
        if (iconTint != other.iconTint) return false
        if (number != other.number) return false
        if (dataRoaming != other.dataRoaming) return false
        if (iconBitmap != null) {
            if (other.iconBitmap == null) return false
            if (!iconBitmap.contentEquals(other.iconBitmap)) return false
        } else if (other.iconBitmap != null) return false
        if (mcc != other.mcc) return false
        if (mnc != other.mnc) return false
        if (ehplmns != other.ehplmns) return false
        if (hplmns != other.hplmns) return false
        if (countryIso != other.countryIso) return false
        if (embedded != other.embedded) return false
        if (accessRules != other.accessRules) return false
        if (cardString != other.cardString) return false
        if (cardId != other.cardId) return false
        if (opportunistic != other.opportunistic) return false
        if (groupUuid != other.groupUuid) return false
        if (groupOwner != other.groupOwner) return false
        if (groupDisabled != other.groupDisabled) return false
        if (profileClass != other.profileClass) return false
        if (subscriptionType != other.subscriptionType) return false
        if (uiccApplicationsEnabled != other.uiccApplicationsEnabled) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (iccId?.hashCode() ?: 0)
        result = 31 * result + simSlotIndex
        result = 31 * result + (displayName?.hashCode() ?: 0)
        result = 31 * result + (carrierName?.hashCode() ?: 0)
        result = 31 * result + carrierId
        result = 31 * result + nameSource
        result = 31 * result + iconTint
        result = 31 * result + (number?.hashCode() ?: 0)
        result = 31 * result + dataRoaming
        result = 31 * result + (iconBitmap?.contentHashCode() ?: 0)
        result = 31 * result + (mcc?.hashCode() ?: 0)
        result = 31 * result + (mnc?.hashCode() ?: 0)
        result = 31 * result + (ehplmns?.hashCode() ?: 0)
        result = 31 * result + (hplmns?.hashCode() ?: 0)
        result = 31 * result + (countryIso?.hashCode() ?: 0)
        result = 31 * result + embedded.hashCode()
        result = 31 * result + (accessRules?.hashCode() ?: 0)
        result = 31 * result + (cardString?.hashCode() ?: 0)
        result = 31 * result + (cardId?.hashCode() ?: 0)
        result = 31 * result + opportunistic.hashCode()
        result = 31 * result + (groupUuid?.hashCode() ?: 0)
        result = 31 * result + (groupOwner?.hashCode() ?: 0)
        result = 31 * result + groupDisabled.hashCode()
        result = 31 * result + profileClass
        result = 31 * result + subscriptionType
        result = 31 * result + uiccApplicationsEnabled.hashCode()
        return result
    }
}