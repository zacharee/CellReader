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
import dev.zwander.cellreader.data.BetweenUtils
import dev.zwander.cellreader.data.data.CellModel
import dev.zwander.cellreader.data.data.StateListener
import dev.zwander.cellreader.data.data.TelephonyListener
import dev.zwander.cellreader.data.data.TelephonyListenerCallback
import dev.zwander.cellreader.data.util.CellUtils
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.wrappers.*
import dev.zwander.cellreader.widget.SignalWidgetReceiver
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicLong

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

        runBlocking {
            betweenUtils.queueClear()
        }
        cancel()
        CellModel.destroy()
        subs.removeOnSubscriptionsChangedListener(subsListener)
    }

    @SuppressLint("MissingPermission")
    private fun init(subscriptions: List<Int>) {
        with (CellModel) {
            telephonies.putAll(
                subscriptions.mapNotNull {
                    subInfos[it] = subs.getActiveSubscriptionInfo(it)?.let { subInfo -> SubscriptionInfoWrapper(subInfo, this@UpdaterService) }

                    if (subInfos[it] != null) {
                        cellInfos[it] = listOf()
                        strengthInfos[it] = listOf()

                        subIds.add(it)

                        launch(Dispatchers.IO) {
                            betweenUtils.queueSubscriptionInfo(subInfos)
                                betweenUtils.queueNewSubId(subIds)
                        }

                        it to telephony.createForSubscriptionId(it).also { telephony ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val callback = telephonyCallbacks[it] ?: TelephonyListener(it, this@UpdaterService).apply {
                                    telephonyCallbacks[it] = this
                                }

                                telephony.registerTelephonyCallback(Dispatchers.IO.asExecutor(), callback)
                            } else {
                                val listener = telephonyListeners[it] ?: StateListener(it, this@UpdaterService).apply {
                                    telephonyListeners[it] = this
                                }

                                @Suppress("DEPRECATION")
                                telephony.listen(
                                    listener,
                                    PhoneStateListener.LISTEN_SERVICE_STATE or
                                            PhoneStateListener.LISTEN_CELL_INFO or
                                            PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                                )
                            }

                            updateCellInfo(it, telephony.allCellInfo ?: mutableListOf())
                            updateSignal(it, telephony.signalStrength)
                            updateServiceState(it, telephony.serviceState)
                        }
                    } else {
                        null
                    }
                }
            )
        }
    }

    @SuppressLint("MissingPermission")
    override fun updateCellInfo(subId: Int, infos: MutableList<CellInfo>) {
        with (CellModel) {
            if (infos.isEmpty()) {
                infos.addAll(telephonies[subId]!!.allCellInfo)
            }

            val sorted = infos.map { CellInfoWrapper.newInstance(it) }.sortedWith(CellUtils.CellInfoComparator)

            val foundIDs = mutableListOf<String>()
            val newInfo = sorted.filterNot { foundIDs.contains(it.cellIdentity.toString()).also { result -> if (!result) foundIDs.add(it.cellIdentity.toString()) } }

            launch(Dispatchers.Main) {
                cellInfos[subId] = newInfo

                launch(Dispatchers.IO) {
                    betweenUtils.queueCellInfos(cellInfos)
                }

                updateWidgets()
            }
        }
    }

    override fun updateSignal(subId: Int, strength: SignalStrength?) {
        @Suppress("DEPRECATION")
        val newInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            (strength?.cellSignalStrengths?.map { CellSignalStrengthWrapper.newInstance(it) }?.sortedWith(CellUtils.CellSignalStrengthComparator) ?: listOf())
        } else {
            mutableListOf<CellSignalStrengthWrapper>().apply {
                strength?.let { strength ->
                    if (strength.gsmSignalStrength != Int.MAX_VALUE) {
                        add(CellSignalStrengthGsmWrapper(CellSignalStrengthGsm(strength.gsmSignalStrength, strength.gsmBitErrorRate, Int.MAX_VALUE)))
                    }
                    if (strength.cdmaDbm != Int.MAX_VALUE) {
                        add(CellSignalStrengthCdmaWrapper(CellSignalStrengthCdma(-strength.cdmaDbm, -strength.cdmaEcio, -strength.evdoDbm, -strength.evdoEcio, -strength.evdoSnr)))
                    }
                    if (strength.wcdmaAsuLevel != 255) {
                        add(CellSignalStrengthWcdmaWrapper(CellSignalStrengthWcdma::class.java.getConstructor(Int::class.java, Int::class.java).newInstance(strength.wcdmaAsuLevel, Int.MAX_VALUE)))
                    }
                    if (strength.lteSignalStrength != Int.MAX_VALUE) {
                        add(CellSignalStrengthLteWrapper(CellSignalStrengthLte::class.java.getConstructor(Int::class.java, Int::class.java, Int::class.java, Int::class.java, Int::class.java, Int::class.java)
                            .newInstance(strength.lteSignalStrength, strength.lteRsrp, strength.lteRsrq, strength.lteRssnr, strength.lteCqi, Int.MAX_VALUE)))
                    }
                }
            }
        }

        launch(Dispatchers.Main) {
            with (CellModel) {
                signalStrengths[subId] = strength
                strengthInfos[subId] = newInfo

                launch(Dispatchers.IO) {
                    betweenUtils.queueSignalStrengths(strengthInfos)
                }

                updateWidgets()
            }
        }
    }

    override fun updateServiceState(subId: Int, serviceState: ServiceState?) {
        with (CellModel) {
            val wrapped = serviceState?.run { ServiceStateWrapper(this) }

            launch(Dispatchers.Main) {
                serviceStates[subId] = wrapped

                launch(Dispatchers.IO) {
                    betweenUtils.queueServiceState(serviceStates)
                }

                updateWidgets()
            }
        }
    }

    private var lastUpdate = AtomicLong(0L)

    private fun updateWidgets() {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastUpdate.get() >= 1000) {
            lastUpdate.set(currentTime)

            sendBroadcast(Intent(this, SignalWidgetReceiver::class.java).setAction(SignalWidgetReceiver.ACTION_REFRESH))
        }
    }

    private inner class SubscriptionListener(private val currentList: MutableList<SubscriptionInfo>) : SubscriptionManager.OnSubscriptionsChangedListener() {
        override fun onSubscriptionsChanged() {
            launch {
                val newList = subs.allSubscriptionInfoList
                val newIds = newList.map { it.subscriptionId }
                val currentIds = currentList.map { it.subscriptionId }

                val defaultId = SubscriptionManager.getDefaultSubscriptionId()

                if (newList.size != currentList.size || !(newIds.containsAll(currentIds) && currentIds.containsAll(newIds))) {
                    currentList.clear()
                    currentList.addAll(newList)

                    withContext(Dispatchers.Main) {
                        betweenUtils.queueClear()
                        CellModel.destroy()
                        init(newIds)
                    }
                } else {
                    newList.forEach { subInfo ->
                        withContext(Dispatchers.Main) {
                            CellModel.subInfos[subInfo.subscriptionId] = SubscriptionInfoWrapper(subInfo, this@UpdaterService)

                            withContext(Dispatchers.IO) {
                                betweenUtils.queueSubscriptionInfo(CellModel.subInfos)
                            }

                            updateWidgets()
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    CellModel.primaryCell = defaultId
                    updateWidgets()
                }

                withContext(Dispatchers.IO) {
                    betweenUtils.queuePrimaryCell(defaultId)
                }
            }
        }
    }
}