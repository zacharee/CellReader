package dev.zwander.cellreader

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.*
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.glance.appwidget.updateAll
import dev.zwander.cellreader.data.*
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.data.*
import dev.zwander.cellreader.data.util.CellUtils
import dev.zwander.cellreader.data.util.update
import dev.zwander.cellreader.data.wrappers.*
import dev.zwander.cellreader.widget.SignalWidget
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.collections.HashMap

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
            primaryCell.value = SubscriptionManager.getDefaultDataSubscriptionId()

            telephonies.putAll(
                subscriptions.mapNotNull { subId ->
                    subInfos.update {
                        it!![subId] = subs.getActiveSubscriptionInfo(subId)?.let { subInfo -> SubscriptionInfoWrapper(subInfo, this@UpdaterService) }
                        HashMap(it)
                    }

                    if (subInfos.value!![subId] != null) {
                        cellInfos.update {
                            it!![subId] = listOf()
                            HashMap(it)
                        }
                        strengthInfos.update {
                            it!![subId] = listOf()
                            HashMap(it)
                        }

                        subIds.update {
                            it!!.add(subId)
                            TreeSet(SubsComparator(primaryCell.value!!)).also { s-> s.addAll(it) }
                        }

                        launch(Dispatchers.IO) {
                            betweenUtils.queueSubscriptionInfo(subInfos.value!!)
                                betweenUtils.queueNewSubId(subIds.value!!)
                        }

                        subId to telephony.createForSubscriptionId(subId).also { telephony ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val callback = telephonyCallbacks[subId] ?: TelephonyListener(subId, this@UpdaterService).apply {
                                    telephonyCallbacks[subId] = this
                                }
                                val privileged = privilegedCallbacks[subId] ?: PhysicalChannelConfigListener(this@UpdaterService).apply {
                                    privilegedCallbacks[subId] = this
                                }

                                telephony.registerTelephonyCallback(Dispatchers.IO.asExecutor(), callback)
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
                                            PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED
                                )
                            }

                            updateCellInfo(subId, telephony.allCellInfo ?: mutableListOf())
                            updateSignal(subId, telephony.signalStrength)
                            updateServiceState(subId, telephony.serviceState)
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
            val sorted = infos.map { CellInfoWrapper.newInstance(it) }.sortedWith(CellUtils.CellInfoComparator)

            val foundIDs = mutableListOf<String>()
            val newInfo = sorted.filterNot { foundIDs.contains(it.cellIdentity.toString()).also { result -> if (!result) foundIDs.add(it.cellIdentity.toString()) } }

            launch(Dispatchers.Main) {
                cellInfos.update {
                    it!![subId] = newInfo
                    HashMap(it)
                }

                launch(Dispatchers.IO) {
                    betweenUtils.queueCellInfos(cellInfos.value!!)
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
                signalStrengths.update {
                    it!![subId] = strength
                    HashMap(it)
                }
                strengthInfos.update {
                    it!![subId] = newInfo
                    HashMap(it)
                }

                launch(Dispatchers.IO) {
                    betweenUtils.queueSignalStrengths(strengthInfos.value!!)
                }

                updateWidgets()
            }
        }
    }

    override fun updateServiceState(subId: Int, serviceState: ServiceState?) {
        with (CellModel) {
            val wrapped = serviceState?.run { ServiceStateWrapper(this) }

            launch(Dispatchers.Main) {
                serviceStates.update {
                    it!![subId] = wrapped
                    HashMap(it)
                }

                launch(Dispatchers.IO) {
                    betweenUtils.queueServiceState(serviceStates.value!!)
                }

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
        launch(Dispatchers.Main) {
            CellModel.displayInfos.update {
                it!![subId] = telephonyDisplayInfo?.let { info -> TelephonyDisplayInfoWrapper(info) }
                it
            }
        }
    }

    private var lastUpdate = AtomicLong(0L)

    private suspend fun updateWidgets() {
        withContext(Dispatchers.Main) {
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastUpdate.get() >= 1000) {
                lastUpdate.set(currentTime)

//                val subIds = CellModel.subIds.value!!.map(Any::toString).toSet()
//                val cellInfos = betweenUtils.cellInfoGson.toJson(CellModel.cellInfos.value)
//                val subInfos = betweenUtils.otherGson.toJson(CellModel.subInfos.value)
//                val serviceStates = betweenUtils.serviceStateGson.toJson(CellModel.serviceStates.value)
//                val strengthInfos = betweenUtils.cellSignalStrengthGson.toJson(CellModel.strengthInfos.value)
//
//                GlanceState.updateValue(
//                    this@UpdaterService,
//                    SignalWidget().stateDefinition,
//                    "WIDGET_OPTIONS"
//                ) {
//                    it.toMutablePreferences().also {
//                        it[SignalWidget.SUB_IDS_KEY] = subIds
//                        it[SignalWidget.CELL_INFOS_KEY] = cellInfos
//                        it[SignalWidget.SUB_INFOS_KEY] = subInfos
//                        it[SignalWidget.SERVICE_STATES_KEY] = serviceStates
//                        it[SignalWidget.STRENGTH_INFOS_KEY] = strengthInfos
//                    }
//                }
                SignalWidget().updateAll(this@UpdaterService)
            }
        }
    }

    private inner class SubscriptionListener(private val currentList: MutableList<SubscriptionInfo>) : SubscriptionManager.OnSubscriptionsChangedListener() {
        override fun onSubscriptionsChanged() {
            launch {
                val newList = subs.allSubscriptionInfoList
                val newIds = newList.map { it.subscriptionId }
                val currentIds = currentList.map { it.subscriptionId }

                val defaultId = SubscriptionManager.getDefaultDataSubscriptionId()

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
                            CellModel.subInfos.update {
                                it!![subInfo.subscriptionId] = SubscriptionInfoWrapper(subInfo, this@UpdaterService)
                                HashMap(it)
                            }

                            withContext(Dispatchers.IO) {
                                betweenUtils.queueSubscriptionInfo(CellModel.subInfos.value!!)
                            }

                            updateWidgets()
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    CellModel.primaryCell.value = defaultId
                    CellModel.subIds.update {
                        TreeSet(SubsComparator(defaultId)).also { s -> s.addAll(it!!) }
                    }
                    updateWidgets()
                }

                withContext(Dispatchers.IO) {
                    betweenUtils.queuePrimaryCell(defaultId)
                }
            }
        }
    }
}