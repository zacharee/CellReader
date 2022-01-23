package dev.zwander.cellreader.ui.layouts

import android.os.Build
import android.telephony.SubscriptionInfo
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.flowlayout.SizeMode
import dev.zwander.cellreader.R
import dev.zwander.cellreader.data.allAccessRulesCompat
import dev.zwander.cellreader.data.cardIdCompat
import dev.zwander.cellreader.ui.components.PaddedDivider
import dev.zwander.cellreader.utils.*

@Composable
fun SubInfo(
    subscriptionInfo: SubscriptionInfo
) {
    with (subscriptionInfo) {
        PaddedDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
        )

        Text(
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
            FormatText(R.string.number_format, number)
            FormatText(R.string.display_name_format, "$displayName")
            FormatText(R.string.carrier_name_format, "$carrierName")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FormatText(R.string.carrier_id_format, "$carrierId")
                FormatText(
                    R.string.subscription_type_format,
                    subscriptionTypeToString(subscriptionType)
                )
            }
            FormatText(R.string.subscription_id_format, "$subscriptionId")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FormatText(
                    R.string.profile_class_format,
                    profileClassToString(profileClass)
                )
            }
            FormatText(R.string.name_source_format, nameSourceToString(nameSource))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FormatText(R.string.opportunistic_format, "$isOpportunistic")
            }
            FormatText(R.string.embedded_format, "$isEmbedded")
            if (iccId.isNotBlank()) {
                FormatText(R.string.icc_id_format, iccId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (hplmns.isNotEmpty()) {
                    FormatText(R.string.hplmns_format, hplmns.joinToString(", ") { it.asMccMnc })
                }
                if (ehplmns.isNotEmpty()) {
                    FormatText(R.string.ehplmns_format, ehplmns.joinToString(", ") { it.asMccMnc })
                }
                FormatText(R.string.group_disabled_format, "$isGroupDisabled")
                this.groupUuid?.apply {
                    FormatText(R.string.group_uuid_format, "$this")
                }
                this.groupOwner?.apply {
                    FormatText(R.string.group_owner_format, this)
                }
            }
            FormatText(R.string.country_iso_format, countryIso)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (cardString.isNotBlank()) {
                    FormatText(R.string.card_string_format, cardString)
                }
            }
            FormatText(R.string.card_id_format, cardIdCompat)
            FormatText(R.string.data_roaming_format, "$dataRoaming")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FormatText(R.string.mcc_mnc_format, "$mccString-$mncString")
            } else {
                FormatText(R.string.mcc_mnc_format, "$mcc-$mnc")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                FormatText(R.string.uicc_apps_format, "${areUiccApplicationsEnabled()}")
            }

            if (allAccessRulesCompat.isNotEmpty()) {
                AccessRules(accessRules = allAccessRulesCompat)
            }
        }
    }
}