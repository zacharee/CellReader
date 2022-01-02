package dev.zwander.cellreader

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.glance.appwidget.updateAll
import dev.zwander.cellreader.utils.CellUtils
import kotlinx.coroutines.*
import kotlin.collections.HashMap

class UpdaterService : Service(), CoroutineScope by MainScope() {
    companion object {
        const val ACTION_EXIT = "${BuildConfig.APPLICATION_ID}.EXIT"
    }

    private val telephony by lazy { getSystemService(TELEPHONY_SERVICE) as TelephonyManager }
    private val subs by lazy { getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager }

    private val callbacks by lazy { HashMap<Int, TelephonyCallback>() }
    private val listeners = HashMap<Int, PhoneStateListener>()
    private val subsListener by lazy { SubscriptionListener(mutableListOf()) }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_EXIT) {
            stopSelf()
            return START_NOT_STICKY
        }

        return super.onStartCommand(intent, flags, startId)
    }

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
        deinit()
        subs.removeOnSubscriptionsChangedListener(subsListener)
    }

    private fun deinit() {
        telephonies.forEach { (subId, telephony) ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                telephony.unregisterTelephonyCallback(callbacks[subId])
            } else {
                telephony.listen(listeners[subId], PhoneStateListener.LISTEN_NONE)
            }
        }

        subIds.clear()
        cellInfos.clear()
        strengthInfos.clear()
        subInfos.clear()
        serviceStates.clear()
        telephonies.clear()
    }

    @SuppressLint("MissingPermission")
    private fun init(subscriptions: List<Int>) {
        telephonies.putAll(
            subscriptions.map {
                cellInfos[it] = listOf()
                strengthInfos[it] = listOf()
                subInfos[it] = subs.getActiveSubscriptionInfo(it)
                subIds.add(it)

                it to telephony.createForSubscriptionId(it).also { telephony ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val callback = callbacks[it] ?: TelephonyListener(it).apply {
                            callbacks[it] = this
                        }

                        telephony.registerTelephonyCallback(Dispatchers.IO.asExecutor(), callback)
                    } else {
                        val listener = listeners[it] ?: StateListener(it).apply {
                            listeners[it] = this
                        }

                        telephony.listen(
                            listener,
                            PhoneStateListener.LISTEN_SERVICE_STATE or
                                    PhoneStateListener.LISTEN_CELL_INFO or
                                    PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                        )
                    }

                    update(it, telephony.allCellInfo)
                    updateSignal(it, telephony.signalStrength)
                    updateServiceState(it, telephony.serviceState)
                }
            }
        )
    }

    @SuppressLint("MissingPermission")
    private fun update(subId: Int, infos: MutableList<CellInfo>) {
        if (infos.isEmpty()) {
            infos.addAll(telephonies[subId]!!.allCellInfo)
        }

        infos.sortWith(CellUtils.CellInfoComparator)

        val foundIDs = mutableListOf<String>()
        val newInfo = infos.filterNot { foundIDs.contains(it.cellIdentity.toString()).also { result -> if (!result) foundIDs.add(it.cellIdentity.toString()) } }

        launch(Dispatchers.Main) {
            cellInfos[subId] = newInfo

            SignalWidget().updateAll(this@UpdaterService)
        }
    }

    private fun updateSignal(subId: Int, strength: SignalStrength?) {
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

        launch(Dispatchers.Main) {
            signalStrengths[subId] = strength
            strengthInfos[subId] = newInfo

            SignalWidget().updateAll(this@UpdaterService)
        }
    }

    private fun updateServiceState(subId: Int, serviceState: ServiceState?) {
        launch(Dispatchers.Main) {
            serviceStates[subId] = serviceState
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private inner class TelephonyListener(private val subId: Int) : TelephonyCallback(),
        TelephonyCallback.CellInfoListener, TelephonyCallback.SignalStrengthsListener,
        TelephonyCallback.ServiceStateListener {
        @SuppressLint("MissingPermission")
        override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>?) {
            update(subId, cellInfo ?: mutableListOf())
        }

        override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
            updateSignal(subId, signalStrength)
        }

        override fun onServiceStateChanged(serviceState: ServiceState?) {
            updateServiceState(subId, serviceState)
        }
    }

    private inner class StateListener(private val subId: Int) : PhoneStateListener() {
        override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>?) {
            update(subId, cellInfo ?: mutableListOf())
        }

        override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
            updateSignal(subId, signalStrength)
        }

        override fun onServiceStateChanged(serviceState: ServiceState?) {
            updateServiceState(subId, serviceState)
        }
    }

    private inner class SubscriptionListener(private val currentList: MutableList<SubscriptionInfo>) : SubscriptionManager.OnSubscriptionsChangedListener() {
        override fun onSubscriptionsChanged() {
            val newList = subs.allSubscriptionInfoList
            val newIds = newList.map { it.subscriptionId }
            val currentIds = currentList.map { it.subscriptionId }

            val defaultId = SubscriptionManager.getDefaultSubscriptionId()

            launch(Dispatchers.Main) {
                primaryCell = defaultId
            }

            if (newList.size != currentList.size || !(newIds.containsAll(currentIds) && currentIds.containsAll(newIds))) {
                currentList.clear()
                currentList.addAll(newList)

                launch(Dispatchers.Main) {
                    deinit()
                    init(newIds)
                }
            }
        }
    }
}