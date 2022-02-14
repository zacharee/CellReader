package dev.zwander.cellreader.data.layouts

import android.os.Build
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.flowlayout.SizeMode
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.components.PaddedDivider
import dev.zwander.cellreader.data.components.WearSafeText
import dev.zwander.cellreader.data.util.*
import dev.zwander.cellreader.data.wrappers.SubscriptionInfoWrapper

@Composable
fun SubInfo(
    subscriptionInfo: SubscriptionInfoWrapper
) {
    val context = LocalContext.current

    with (subscriptionInfo) {
        PaddedDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
        )

        WearSafeText(
            text = stringResource(id = R.string.subscription_info),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        FlowRow(
            mainAxisSpacing = 16.dp,
            mainAxisAlignment = MainAxisAlignment.SpaceEvenly,
            mainAxisSize = SizeMode.Expand,
            modifier = Modifier.padding(8.dp)
        ) {
            FormatText(R.string.sim_slot_format, "$simSlotIndex")
            FormatText(R.string.number_format, number ?: "")
            FormatText(R.string.display_name_format, "$displayName")
            FormatText(R.string.carrier_name_format, "$carrierName")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FormatText(R.string.carrier_id_format, "$carrierId")
                FormatText(
                    R.string.subscription_type_format,
                    subscriptionTypeToString(context, subscriptionType)
                )
            }
            FormatText(R.string.subscription_id_format, "$id")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FormatText(
                    R.string.profile_class_format,
                    profileClassToString(context, profileClass)
                )
            }
            FormatText(R.string.name_source_format, nameSourceToString(context, nameSource))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FormatText(R.string.opportunistic_format, "$opportunistic")
            }
            FormatText(R.string.embedded_format, "$embedded")
            if (iccId?.isNotBlank() == true) {
                FormatText(R.string.icc_id_format, iccId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (hplmns?.isNotEmpty() == true) {
                    FormatText(R.string.hplmns_format, hplmns.joinToString(", ") { it.asMccMnc })
                }
                if (ehplmns?.isNotEmpty() == true) {
                    FormatText(R.string.ehplmns_format, ehplmns.joinToString(", ") { it.asMccMnc })
                }
                FormatText(R.string.group_disabled_format, "$groupDisabled")
                this.groupUuid?.apply {
                    FormatText(R.string.group_uuid_format, this)
                }
                this.groupOwner?.apply {
                    FormatText(R.string.group_owner_format, this)
                }
            }
            FormatText(R.string.country_iso_format, countryIso ?: "")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (cardString?.isNotBlank() == true) {
                    FormatText(R.string.card_string_format, cardString)
                }
            }
            FormatText(R.string.card_id_format, cardId ?: "")
            FormatText(R.string.data_roaming_format, "$dataRoaming")
            FormatText(R.string.plmn_format, "$mcc-$mnc")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                FormatText(R.string.uicc_apps_format, "$uiccApplicationsEnabled")
            }

            if (accessRules?.isNotEmpty() == true) {
                AccessRules(accessRules = accessRules)
            }
        }
    }
}