package dev.zwander.cellreader

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemProperties
import android.telephony.*
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.glance.appwidget.updateAll
import dev.zwander.cellreader.data.*
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.data.*
import dev.zwander.cellreader.data.util.CellUtils
import dev.zwander.cellreader.data.util.asExecutor
import dev.zwander.cellreader.data.util.update
import dev.zwander.cellreader.data.wrappers.*
import dev.zwander.cellreader.widget.SignalWidget
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class UpdaterService : Service(), CoroutineScope by MainScope(), TelephonyListenerCallback {
    companion object {
        const val ACTION_EXIT = "${BuildConfig.APPLICATION_ID}.EXIT"
        const val ACTION_REFRESH = "${BuildConfig.APPLICATION_ID}.REFRESH"

        fun refresh(context: Context) {
            start(context, true)
        }

        fun start(context: Context, refresh: Boolean) {
            val intent = Intent(context, UpdaterService::class.java)
            if (refresh) intent.action = ACTION_REFRESH

            ContextCompat.startForegroundService(context, intent)
        }
    }

    private var isStarted = false
    private var stalledAfterSecurityException = AtomicBoolean(false)

    private val telephony by lazy { getSystemService(TELEPHONY_SERVICE) as TelephonyManager }
    private val subs by lazy { getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager }

    private val subsListener by lazy { SubscriptionListener(mutableListOf()) }
    private val opportunisticSubsListener by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            OpportunisticSubsListener()
        } else {
            null
        }
    }
    private val betweenUtils by lazy { BetweenUtils.getInstance(this) }

    private val callbackExecutor = coroutineContext.asExecutor()
    private val cellModel = CellModel.getInstance()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_EXIT) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (intent?.action == ACTION_REFRESH && isStarted) {
            refresh()
        }

        if (isStarted && stalledAfterSecurityException.get()) {
            refresh()
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
            .setSmallIcon(R.drawable.cell_3)
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

        refresh()

        isStarted = true

//        PermissionUtils.checkShizukuPermission {
//            if (it == PackageManager.PERMISSION_GRANTED) {
//                Shizuku.bindUserService(
//                    Shizuku.UserServiceArgs(ComponentName(this, ShizukuUserService::class.java))
//                        .processNameSuffix("privileged")
//                        .version(BuildConfig.VERSION_CODE)
//                        .debuggable(true)
//                        .daemon(false)
//                        .tag("CellReader"),
//                    object : ServiceConnection {
//                        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//                            CellModel.service = IShizukuUserService.Stub.asInterface(service)
//
//                            CellModel.privilegedCallbacks.forEach { (subId, callback) ->
//                                CellModel.service!!.registerPrivilegedListener(subId, callback)
//                            }
//
//                            CellModel.subIds.value?.forEach { subId ->
//                                CellModel.service!!.requestNetworkScan(
//                                    subId,
//                                    NetworkScanRequest(
//                                        NetworkScanRequest.SCAN_TYPE_ONE_SHOT,
//                                        arrayOf(
//                                            AccessNetworkConstants.AccessNetworkType.GERAN,
//                                            AccessNetworkConstants.AccessNetworkType.UTRAN,
//                                            AccessNetworkConstants.AccessNetworkType.EUTRAN,
//                                            AccessNetworkConstants.AccessNetworkType.CDMA2000,
//                                            AccessNetworkConstants.AccessNetworkType.IWLAN,
//                                            AccessNetworkConstants.AccessNetworkType.NGRAN
//                                        ).map {
//                                            RadioAccessSpecifier(
//                                                it,
//                                                null,
//                                                null
//                                            )
//                                        }.toTypedArray(),
//                                        0,
//                                        10000,
//                                        true,
//                                        1,
//                                        null
//                                    ),
//                                    object : INetworkScanCallback.Stub() {
//                                        override fun onComplete() {
//                                            Log.e("CellReader", "Scan complete for $subId")
//                                        }
//
//                                        override fun onError(error: Int) {
//                                            Log.e("CellReader", "Scan error for $subId $error")
//                                        }
//
//                                        override fun onResults(results: MutableList<Any?>?) {
//                                            val actualResults = results as MutableList<CellInfo>
//
//                                            Log.e("CellReader", "New results $actualResults")
//                                        }
//                                    }
//                                )
//                            }
//                        }
//
//                        override fun onServiceDisconnected(name: ComponentName?) {
//                            CellModel.service = null
//                        }
//                    }
//                )
//            }
//        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()

        isStarted = false

        runBlocking {
            betweenUtils.queueClear()
        }
        cellModel.destroy()
        subs.removeOnSubscriptionsChangedListener(subsListener)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            opportunisticSubsListener?.let {
                subs.removeOnOpportunisticSubscriptionsChangedListener(it)
            }
        }
        cancel()
    }

    @SuppressLint("InlinedApi")
    private fun refresh(newIds: List<Int> = emptyList()) {
        stalledAfterSecurityException.set(false)
        cellModel.isRefreshing.value = true
        val isStarted = isStarted

        launch {
            if (isStarted) {
                delay(100)
            }

            subs.removeOnSubscriptionsChangedListener(subsListener)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                opportunisticSubsListener?.let {
                    subs.removeOnOpportunisticSubscriptionsChangedListener(it)
                }
            }
            subsListener.clear()

            betweenUtils.queueClear()
            cellModel.destroy()

            if (newIds.isEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    subs.addOnSubscriptionsChangedListener(callbackExecutor, subsListener)
                } else {
                    @Suppress("DEPRECATION")
                    subs.addOnSubscriptionsChangedListener(subsListener)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    opportunisticSubsListener?.let {
                        subs.addOnOpportunisticSubscriptionsChangedListener(callbackExecutor, it)
                    }
                }

                try {
                    if (subs.allSubscriptionInfoList.isEmpty()) {
                        init(listOf(SubscriptionManager.DEFAULT_SUBSCRIPTION_ID))
                    }
                } catch (ignored: SecurityException) {
                    stalledAfterSecurityException.set(true)
                }
            } else {
                init(newIds)
            }

            cellModel.isRefreshing.value = false
        }
    }

    @SuppressLint("InlinedApi", "MissingPermission")
    private fun init(subscriptions: List<Int>) {
        try {
            val isEmpty = (subs.activeSubscriptionInfoList ?: listOf()).isEmpty()

            with (cellModel) {
                primaryCell.value = SubscriptionManager.getDefaultDataSubscriptionId()

                telephonies.putAll(
                    subscriptions.mapNotNull { subId ->
                        subInfos.update {
                            it[subId] = (if (isEmpty) {
                                subs.allSubscriptionInfoList.find { info -> info.subscriptionId == subId }
                            } else {
                                subs.getActiveSubscriptionInfo(subId)
                            })?.let { subInfo ->
                                SubscriptionInfoWrapper(
                                    subInfo,
                                    this@UpdaterService
                                )
                            }
                        }

                        if (subInfos.value[subId] != null || isEmpty) {
                            cellInfos.update {
                                it[subId] = listOf()
                            }
                            strengthInfos.update {
                                it[subId] = listOf()
                            }

                            subIds.update {
                                it.add(subId)
                                it.updateComparator(SubsComparator(primaryCell.value))
                            }

                            launch(Dispatchers.IO) {
                                betweenUtils.queueSubscriptionInfo(subInfos.value)
                                betweenUtils.queueNewSubId(subIds.value)
                            }

                            subId to telephony.createForSubscriptionId(subId).also { telephony ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    val callback = telephonyCallbacks[subId] ?: TelephonyListener(subId, this@UpdaterService).apply {
                                        telephonyCallbacks[subId] = this
                                    }
                                    val privileged = privilegedCallbacks[subId] ?: PhysicalChannelConfigListener(this@UpdaterService).apply {
                                        privilegedCallbacks[subId] = this
                                    }

                                    telephony.registerTelephonyCallback(callbackExecutor, callback)
                                    service?.registerPrivilegedListener(subId, privileged)
                                } else {
                                    val listener = telephonyListeners[subId] ?: StateListener(subId, this@UpdaterService).apply {
                                        telephonyListeners[subId] = this
                                    }

                                    @Suppress("DEPRECATION")
                                    telephony.listen(
                                        listener,
                                        PhoneStateListener.LISTEN_SERVICE_STATE or
                                                PhoneStateListener.LISTEN_CELL_INFO or
                                                PhoneStateListener.LISTEN_SIGNAL_STRENGTHS or
                                                PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED or
                                                PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                                    )
                                }

                                updateCellInfo(subId, telephony.allCellInfo ?: mutableListOf())
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                                    updateSignal(subId, telephony.signalStrength)
                                }

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    updateServiceState(subId, telephony.serviceState)
                                } else {
                                    updateServiceState(subId, telephony.getServiceStateForSubscriber(subId))
                                }
                            }
                        } else {
                            null
                        }
                    }
                )
            }
        } catch (e: SecurityException) {
            stalledAfterSecurityException.set(true)
        }
    }

    override fun updateCellInfo(subId: Int, infos: MutableList<CellInfo>) {
        with (cellModel) {
            val sorted = infos.map { CellInfoWrapper.newInstance(it) }.sortedWith(CellUtils.CellInfoComparator)

            val bands = SystemProperties.get("ril.lteband").split(",")

            if (infos.isEmpty() && strengthInfos.value[subId]?.isNotEmpty() == true) {
                return
            }

            val foundIDs = mutableListOf<String>()
            val newInfo = sorted.filterNot { info ->
                foundIDs.contains(info.cellIdentity.toString()).also { result ->
                    if (!result) foundIDs.add(info.cellIdentity.toString())
                }
            }.toMutableList()

            val realBand = subInfos.value[subId]?.simSlotIndex?.let { bands.getOrNull(it) }
            var firstInfo = sorted.firstOrNull()

            if (!realBand.isNullOrBlank() && firstInfo != null) {
                val firstInfoBand = firstInfo.cellIdentity.realBands.firstOrNull()

                if (firstInfoBand != realBand) {
                    if (firstInfo is CellInfoLteWrapper) {
                        firstInfo = firstInfo.copy(
                            cellIdentity = firstInfo.cellIdentity.copy(
                                realBands = listOf(realBand),
                            ),
                        )
                    }

                    if (firstInfo is CellInfoNrWrapper) {
                        firstInfo = firstInfo.copy(
                            cellIdentity = firstInfo.cellIdentity.copy(
                                realBands = listOf(realBand),
                            ),
                        )
                    }
                }
            }

            if (firstInfo != null) {
                newInfo[0] = firstInfo
            }

            cellInfos.update {
                it[subId] = newInfo
            }

            launch(Dispatchers.IO) {
                betweenUtils.queueCellInfos(cellInfos.value)
                updateWidgets()
            }
        }
    }

    override fun updateSignal(subId: Int, strength: SignalStrength?) {
        @Suppress("DEPRECATION")
        val newInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            (strength?.cellSignalStrengths?.mapNotNull { CellSignalStrengthWrapper.newInstance(it) }?.sortedWith(CellUtils.CellSignalStrengthComparator) ?: listOf())
        } else {
            mutableListOf<CellSignalStrengthWrapper>().apply {
                strength?.let { strength ->
                    if (strength.gsmSignalStrength != Int.MAX_VALUE) {
                        add(
                            CellSignalStrengthWrapper.newInstance<CellSignalStrengthGsmWrapper>(
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    CellSignalStrengthGsm(strength.gsmSignalStrength, strength.gsmBitErrorRate, Int.MAX_VALUE)
                                } else {
                                    CellSignalStrengthGsm::class.java.getConstructor(Int::class.java, Int::class.java)
                                        .newInstance(strength.gsmSignalStrength, strength.gsmBitErrorRate)
                                }
                            )
                        )
                    }
                    if (strength.cdmaDbm != Int.MAX_VALUE) {
                        add(CellSignalStrengthWrapper.newInstance(CellSignalStrengthCdma(-strength.cdmaDbm, -strength.cdmaEcio, -strength.evdoDbm, -strength.evdoEcio, -strength.evdoSnr)))
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        if (strength.wcdmaAsuLevel != 255) {
                            add(CellSignalStrengthWrapper.newInstance(CellSignalStrengthWcdma::class.java.getConstructor(Int::class.java, Int::class.java).newInstance(strength.wcdmaAsuLevel, Int.MAX_VALUE)))
                        }
                    }
                    if (strength.lteSignalStrength != Int.MAX_VALUE) {
                        add(CellSignalStrengthWrapper.newInstance(CellSignalStrengthLte::class.java.getConstructor(Int::class.java, Int::class.java, Int::class.java, Int::class.java, Int::class.java, Int::class.java)
                            .newInstance(strength.lteSignalStrength, strength.lteRsrp, strength.lteRsrq, strength.lteRssnr, strength.lteCqi, Int.MAX_VALUE)))
                    }
                }
            }
        }

        with (cellModel) {
            signalStrengths.update {
                it[subId] = strength
            }
            strengthInfos.update {
                it[subId] = newInfo
            }

            launch(Dispatchers.IO) {
                betweenUtils.queueSignalStrengths(strengthInfos.value)
                updateWidgets()
            }
        }
    }

    override fun updateServiceState(subId: Int, serviceState: ServiceState?) {
        with (cellModel) {
            val wrapped = serviceState?.run { ServiceStateWrapper(this) }

            serviceStates.update {
                it[subId] = wrapped
            }

            launch(Dispatchers.IO) {
                betweenUtils.queueServiceState(serviceStates.value)
                updateWidgets()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun updatePhysicalChannelConfigs(
        subId: Int,
        configs: List<PhysicalChannelConfig>
    ) {}

    override fun updateDisplayInfo(subId: Int, telephonyDisplayInfo: TelephonyDisplayInfo?) {
        cellModel.displayInfos.update {
            it[subId] = telephonyDisplayInfo?.let { info -> TelephonyDisplayInfoWrapper(info) }
        }

        launch(Dispatchers.IO) {
            betweenUtils.queueDisplayInfos(cellModel.displayInfos.value)
            updateWidgets()
        }
    }

    override fun updateDataConnectionState(subId: Int, state: Int, networkType: Int) {
        cellModel.dataConnectionStates.update {
            it[subId] = DataConnectionState(subId, state, networkType)
        }

        launch(Dispatchers.IO) {
            betweenUtils.queueDataConnectionStates(cellModel.dataConnectionStates.value)
            updateWidgets()
        }
    }

    private var lastUpdate = AtomicLong(0L)

    private suspend fun updateWidgets() {
        withContext(Dispatchers.Main) {
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastUpdate.get() >= 1000) {
                lastUpdate.set(currentTime)

                SignalWidget().updateAll(this@UpdaterService)
            }
        }
    }

    private inner class SubscriptionListener(private val currentList: MutableList<SubscriptionInfo>) : SubscriptionManager.OnSubscriptionsChangedListener() {
        fun clear() {
            currentList.clear()
        }

        @SuppressLint("NewApi")
        override fun onSubscriptionsChanged() {
            try {
                val newList = subs.allSubscriptionInfoList
                val newIds = newList.map { it.subscriptionId }
                val currentIds = currentList.map { it.subscriptionId }

                val defaultId = SubscriptionManager.getDefaultDataSubscriptionId()

                if (newList.size != currentList.size || !(newIds.containsAll(currentIds) && currentIds.containsAll(newIds))) {
                    clear()
                    currentList.addAll(newList)

                    refresh(newIds)
                } else {
                    launch {
                        newList.forEach { subInfo ->
                            cellModel.subInfos.update {
                                it[subInfo.subscriptionId] = SubscriptionInfoWrapper(subInfo, this@UpdaterService)
                            }

                            withContext(Dispatchers.IO) {
                                betweenUtils.queueSubscriptionInfo(cellModel.subInfos.value)
                            }

                            updateWidgets()
                        }
                    }
                }

                cellModel.primaryCell.value = defaultId
                cellModel.subIds.update {
                    it.updateComparator(SubsComparator(defaultId))
                }

                launch {
                    updateWidgets()

                    withContext(Dispatchers.IO) {
                        betweenUtils.queuePrimaryCell(defaultId)
                    }
                }
            } catch (ignored: SecurityException) {
                stalledAfterSecurityException.set(true)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private inner class OpportunisticSubsListener : SubscriptionManager.OnOpportunisticSubscriptionsChangedListener() {
        override fun onOpportunisticSubscriptionsChanged() {
            subsListener.onSubscriptionsChanged()
        }
    }
}