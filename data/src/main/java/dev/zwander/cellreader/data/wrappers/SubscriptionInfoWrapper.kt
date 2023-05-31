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
import dev.zwander.cellreader.data.allAccessRulesCompat
import dev.zwander.cellreader.data.cardIdCompat
import dev.zwander.cellreader.data.util.onAvail
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
    val ehplmns: ArrayList<String>?,
    val hplmns: ArrayList<String>?,
    val countryIso: String?,
    val embedded: Boolean?,
    val accessRules: ArrayList<UiccAccessRuleWrapper>?,
    val cardString: String?,
    val cardId: String?,
    val opportunistic: Boolean?,
    val groupUuid: String?,
    val groupOwner: String?,
    val groupDisabled: Boolean?,
    val profileClass: Int,
    val subscriptionType: Int,
    val uiccApplicationsEnabled: Boolean?
) {
    @SuppressLint("MissingPermission", "InlinedApi")
    constructor(info: SubscriptionInfo, context: Context) : this(
        info.subscriptionId,
        info.iccId,
        info.simSlotIndex,
        info.displayName?.toString(),
        info.carrierName?.toString(),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) info.carrierId else CellInfo.UNAVAILABLE,
        try {
            info.nameSource
        } catch (e: NoSuchMethodError) {
            info::class.java.getDeclaredMethod("getDisplayNameSource").invoke(info)?.toString()?.toIntOrNull()
                ?: CellInfo.UNAVAILABLE
        },
        info.iconTint,
        if (Build.VERSION.SDK_INT >= 33) {
            (context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager).getPhoneNumber(
                info.subscriptionId
            )
        } else {
            @Suppress("DEPRECATION")
            info.number
        },
        info.dataRoaming,
        ByteArrayOutputStream().use { output ->
            val bmp = info.createIconBitmap(context)
            bmp.compress(Bitmap.CompressFormat.PNG, 100, output)
            bmp.recycle()
            Base64.encodeToString(output.toByteArray(), Base64.DEFAULT)
        },
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) info.mccString else info.mcc.onAvail { it.toString() },
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) info.mncString else info.mnc.onAvail { it.toString() },
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ArrayList(info.ehplmns) else arrayListOf(),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ArrayList(info.hplmns) else arrayListOf(),
        info.countryIso,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) info.isEmbedded else null,
        ArrayList(info.allAccessRulesCompat.map { UiccAccessRuleWrapper(it) }),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) info.cardString else null,
        info.cardIdCompat,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) info.isOpportunistic else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) info.groupUuid?.toString() else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) info.groupOwner else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) info.isGroupDisabled else null,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) info.profileClass else CellInfo.UNAVAILABLE,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) info.subscriptionType else CellInfo.UNAVAILABLE,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) info.areUiccApplicationsEnabled() else null
    )

    val iconBitmapBmp: Bitmap?
        get() = iconBitmap?.let {
            Base64.decode(it, Base64.DEFAULT).run {
                BitmapFactory.decodeByteArray(this, 0, size)
            }
        }
}