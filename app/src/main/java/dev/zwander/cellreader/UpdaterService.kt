package dev.zwander.cellreader

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.*
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.glance.appwidget.updateAll
import dev.zwander.cellreader.data.BetweenUtils
import dev.zwander.cellreader.data.data.CellModel
import dev.zwander.cellreader.data.data.TelephonyListenerCallback
import dev.zwander.cellreader.data.wrappers.CellInfoWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthWrapper
import dev.zwander.cellreader.data.wrappers.ServiceStateWrapper
import dev.zwander.cellreader.data.wrappers.SubscriptionInfoWrapper
import dev.zwander.cellreader.utils.CellUtils
import dev.zwander.cellreader.utils.cellIdentityCompat
import kotlinx.coroutines.*

class UpdaterService : Service(), CoroutineScope by MainScope(), TelephonyListenerCallback {
    companion object {
        const val ACTION_EXIT = "${BuildConfig.APPLICATION_ID}.EXIT"
    }

    private val telephony by lazy { getSystemService(TELEPHONY_SERVICE) as TelephonyManager }
    private val subs by lazy { getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager }

    private val subsListener by lazy { SubscriptionListener(mutableListOf()) }
    private val betweenUtils by lazy { BetweenUtils.getInstance(this) }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_EXIT) {
            stopSelf()
            return START_NOT_STICKY
        }

        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("InlinedApi")
    override fun onCreate() {
        super.onCreate()

        val nm = NotificationManagerCompat.from(this)

        nm.createNotificationChannel(
            NotificationChannelCompat.Builder("main", NotificationManagerCompat.IMPORTANCE_LOW)
                .setName(resources.getString(R.string.app_name))
                .build()
        )

        val n = NotificationCompat.Builder(this, "main")
            .setContentTitle(resources.getString(R.string.running_notification_title))
            .setContentText(resources.getString(R.string.running_notification_desc))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    101,
                    Intent(
                        this,
                        MainActivity::class.java
                    ),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
            )
            .addAction(
                NotificationCompat.Action.Builder(
                    null, resources.getString(R.string.exit), PendingIntent.getService(
                        this,
                        100,
                        Intent(this, UpdaterService::class.java).apply { action = ACTION_EXIT },
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                ).build()
            )
            .build()

        startForeground(100, n)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            subs.addOnSubscriptionsChangedListener(Dispatchers.IO.asExecutor(), subsListener)
        } else {
            @Suppress("DEPRECATION")
            subs.addOnSubscriptionsChangedListener(subsListener)
        }

        if (subs.allSubscriptionInfoList.isEmpty()) {
            init(listOf(SubscriptionManager.DEFAULT_SUBSCRIPTION_ID))
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()

        cancel()
        CellModel.destroy()
        subs.removeOnSubscriptionsChangedListener(subsListener)
    }

    @SuppressLint("MissingPermission")
    private fun init(subscriptions: List<Int>) {
        CellModel.create(
            telephony,
            subs,
            subscriptions,
            this
        )
    }

    @SuppressLint("MissingPermission")
    override fun updateCellInfo(subId: Int, infos: MutableList<CellInfo>) {
        with (CellModel) {
            if (infos.isEmpty()) {
                infos.addAll(telephonies[subId]!!.allCellInfo)
            }

            infos.sortWith(CellUtils.CellInfoComparator)

            val foundIDs = mutableListOf<String>()
            val newInfo = infos.filterNot { foundIDs.contains(it.cellIdentityCompat.toString()).also { result -> if (!result) foundIDs.add(it.cellIdentityCompat.toString()) } }

            val wrapped = newInfo.map { CellInfoWrapper.newInstance(it) }

            launch(Dispatchers.Main) {
                cellInfos[subId] = newInfo

                SignalWidget().updateAll(this@UpdaterService)
            }

            launch(Dispatchers.IO) {
                betweenUtils.sendCellInfos(subId, wrapped)
            }
        }
    }

    override fun updateSignal(subId: Int, strength: SignalStrength?) {
        @Suppress("DEPRECATION")
        val newInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            (strength?.cellSignalStrengths?.sortedWith(CellUtils.CellSignalStrengthComparator) ?: listOf())
        } else {
            mutableListOf<CellSignalStrength>().apply {
                strength?.let { strength ->
                    if (strength.gsmSignalStrength != Int.MAX_VALUE) {
                        add(CellSignalStrengthGsm(strength.gsmSignalStrength, strength.gsmBitErrorRate, Int.MAX_VALUE))
                    }
                    if (strength.cdmaDbm != Int.MAX_VALUE) {
                        add(CellSignalStrengthCdma(-strength.cdmaDbm, -strength.cdmaEcio, -strength.evdoDbm, -strength.evdoEcio, -strength.evdoSnr))
                    }
                    if (strength.wcdmaAsuLevel != 255) {
                        add(CellSignalStrengthWcdma::class.java.getConstructor(Int::class.java, Int::class.java).newInstance(strength.wcdmaAsuLevel, Int.MAX_VALUE))
                    }
                    if (strength.lteSignalStrength != Int.MAX_VALUE) {
                        add(CellSignalStrengthLte::class.java.getConstructor(Int::class.java, Int::class.java, Int::class.java, Int::class.java, Int::class.java, Int::class.java)
                            .newInstance(strength.lteSignalStrength, strength.lteRsrp, strength.lteRsrq, strength.lteRssnr, strength.lteCqi, Int.MAX_VALUE))
                    }
                }
            }
        }

        val wrapped = newInfo.map { CellSignalStrengthWrapper.newInstance(it) }

        launch(Dispatchers.Main) {
            with (CellModel) {
                signalStrengths[subId] = strength
                strengthInfos[subId] = newInfo

                SignalWidget().updateAll(this@UpdaterService)
            }
        }

        launch(Dispatchers.IO) {
            betweenUtils.sendSignalStrengths(subId, wrapped)
        }
    }

    override fun updateServiceState(subId: Int, serviceState: ServiceState?) {
        with (CellModel) {
            val wrapped = serviceState?.run { ServiceStateWrapper(this) }

            launch(Dispatchers.Main) {
                serviceStates[subId] = serviceState
            }

            launch(Dispatchers.IO) {
                betweenUtils.sendServiceState(subId, wrapped)
            }
        }
    }

    private inner class SubscriptionListener(private val currentList: MutableList<SubscriptionInfo>) : SubscriptionManager.OnSubscriptionsChangedListener() {
        override fun onSubscriptionsChanged() {
            val newList = subs.allSubscriptionInfoList
            val newIds = newList.map { it.subscriptionId }
            val currentIds = currentList.map { it.subscriptionId }

            val defaultId = SubscriptionManager.getDefaultSubscriptionId()

            launch(Dispatchers.Main) {
                CellModel.primaryCell = defaultId
            }

            if (newList.size != currentList.size || !(newIds.containsAll(currentIds) && currentIds.containsAll(newIds))) {
                currentList.clear()
                currentList.addAll(newList)

                launch(Dispatchers.Main) {
                    CellModel.destroy()
                    init(newIds)
                }
            } else {
                newList.forEach { subInfo ->
                    launch(Dispatchers.Main) {
                        CellModel.subInfos[subInfo.subscriptionId] = subInfo
                    }

                    launch(Dispatchers.IO) {
                        betweenUtils.sendSubscriptionInfo(subInfo.subscriptionId, SubscriptionInfoWrapper(subInfo, this@UpdaterService))
                    }
                }
            }
        }
    }
}