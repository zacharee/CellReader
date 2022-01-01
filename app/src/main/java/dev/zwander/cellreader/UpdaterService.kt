package dev.zwander.cellreader

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.telephony.*
import androidx.collection.ArraySet
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.glance.appwidget.updateAll
import dev.zwander.cellreader.data.CellModel
import dev.zwander.cellreader.utils.CellUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.collections.HashMap

class UpdaterService : Service(), CoroutineScope by MainScope() {
    companion object {
        const val ACTION_EXIT = "${BuildConfig.APPLICATION_ID}.EXIT"
    }

    private val telephony by lazy { getSystemService(TELEPHONY_SERVICE) as TelephonyManager }
    private val subs by lazy { getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager }

    private val telephonies = arrayListOf<TelephonyManager>()
    private val callbacks = HashMap<Int, TelephonyCallback>()
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
            .setContentTitle(resources.getString(R.string.app_name))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                NotificationCompat.Action.Builder(
                    null, "Exit", PendingIntent.getService(
                        this,
                        100,
                        Intent(this, UpdaterService::class.java).apply { action = ACTION_EXIT },
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                ).build()
            )
            .build()

        startForeground(100, n)

        subs.addOnSubscriptionsChangedListener(Dispatchers.Main.asExecutor(), subsListener)
//        init()
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
        telephonies.forEach {
            it.unregisterTelephonyCallback(callbacks[it.subscriptionId])
        }

        telephonies.clear()
        callbacks.clear()
    }

    @SuppressLint("MissingPermission")
    private fun init() {
        cellInfos.clear()

        telephonies.addAll(
            subs.allSubscriptionInfoList.map { telephony.createForSubscriptionId(it.subscriptionId) }
        )

        telephonies.forEach { telephony ->
            cellInfos[telephony.subscriptionId] = CellModel()

            val callback = callbacks[telephony.subscriptionId] ?: TelephonyListener(telephony.subscriptionId).apply {
                callbacks[telephony.subscriptionId] = this
            }

//            updateSignal(telephony.subscriptionId, telephony.signalStrength)
//            update(telephony.subscriptionId, telephony.allCellInfo)
            telephony.registerTelephonyCallback(Dispatchers.Main.asExecutor(), callback)
        }
    }

    private fun update(subId: Int, infos: MutableList<CellInfo>) {
        launch(Dispatchers.IO) {
            primaryCell = subs.defaultDataSubscriptionInfo?.subscriptionId ?: 0
            infos.sortWith(CellUtils.CellInfoComparator)

            val foundIDs = mutableListOf<String>()
            val model = cellInfos[subId]!!
            val newInfo = infos.filterNot { foundIDs.contains(it.cellIdentity.toString()).also { result -> if (!result) foundIDs.add(it.cellIdentity.toString()) } }

            launch(Dispatchers.Main) {
                model.cellInfos.clear()
                model.cellInfos.addAll(newInfo)
                SignalWidget().updateAll(this@UpdaterService)
            }
        }
    }

    private fun updateSignal(subId: Int, strength: SignalStrength?) {
        launch(Dispatchers.IO) {
            val model = cellInfos[subId]!!
            val newInfo = (strength?.cellSignalStrengths?.sortedWith(CellUtils.CellSignalStrengthComparator) ?: listOf())

            launch(Dispatchers.Main) {
                model.strengths.clear()
                model.strengths.addAll(newInfo)
                SignalWidget().updateAll(this@UpdaterService)
            }
        }
    }

    private inner class TelephonyListener(private val subId: Int) : TelephonyCallback(),
        TelephonyCallback.CellInfoListener, TelephonyCallback.SignalStrengthsListener {
        @SuppressLint("MissingPermission")
        override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>?) {
            update(subId, cellInfo ?: mutableListOf())
        }

        override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
            updateSignal(subId, signalStrength)
        }
    }

    private inner class SubscriptionListener(private val currentList: MutableList<SubscriptionInfo>) :  SubscriptionManager.OnSubscriptionsChangedListener() {
        override fun onSubscriptionsChanged() {
            val newList = subs.allSubscriptionInfoList
            val newIds = newList.map { it.subscriptionId }
            val currentIds = currentList.map { it.subscriptionId }

            if (newList.size != currentList.size || !newIds.containsAll(currentIds) || !currentIds.containsAll(newIds)) {
                currentList.clear()
                currentList.addAll(newList)

                deinit()
                init()
            }
        }
    }
}