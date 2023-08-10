package com.example.multipli

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class serviceBLE : Service() {
    companion object {
        private var istance: serviceBLE? = null
        fun isRunning(): Boolean {
            return istance != null
        }
    }

    var restart: Boolean = false
    var f = 0
    private val binder = LocalBinder()
    private val beaconBL: ArrayList<String> = ArrayList()
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    var deviceName: String = ""
    private var start: Boolean = false
    var allert: Boolean = false
    var alreadyConnected: String = ""
    var main = MainActivity()
    var conteggio = 0
    private lateinit var mServiceAudio: activityRecordAudio
    private var mBoundAudio: Boolean = false
    var RSSI: Int = 0
    var disconnect: Boolean = false
    var RSSId: ArrayList<Int> = ArrayList()
    var scanStart: Long = 10000
    var scanStop: Long = 60000
    var notificaaschermospento: Boolean = false
    var allertDialogok = false
    var test = true


    private val connectionAudio = object : ServiceConnection {   //activityRecordAudio

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            val binderAudio = service as activityRecordAudio.LocalBinderAudio
            mServiceAudio = binderAudio.getService()
            mBoundAudio = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBoundAudio = false
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        istance = this
        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        // handler = Handler(Looper.myLooper()!!
        // f=f+1
        //if (f>1)


        beaconBL.add("testLab")
        beaconBL.add("testLab3")
        beaconBL.add("testLab5")
        //startScan(bluetoothLeScanner)
        val pendingIntent: PendingIntent =
            Intent(this, serviceBLE::class.java).let { notificationIntent ->
                PendingIntent.getActivity(
                    this, 0, notificationIntent,
                    PendingIntent.FLAG_MUTABLE
                )
            }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
            val manager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val chan = NotificationChannel(
                "Bluetooth",
                "Bluetooth", NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(chan)


            val notification: Notification = Notification.Builder(this, "Bluetooth")
                .setOngoing(true) // se messo su true non si può togliere
                .setContentTitle("Ricerca dispositivi")
                .setContentText("Ricerca dei dispositivi per potersi collegare al beacon")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)

                .build()
            startForeground(startId, notification)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val chan = NotificationChannel(
                    "Room",
                    "Room", NotificationManager.IMPORTANCE_HIGH
                )
                manager.createNotificationChannel(chan)


                val notification: Notification = Notification.Builder(this, "Bluetooth")
                    .setOngoing(true)  //true cosi non si può più togliere
                    .setContentTitle("Ricerca dispositivi")
                    .setContentText("Ricerca dei dispositivi per potersi collegare al beacon")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    //.setContentIntent(pendingIntent)

                    .build()
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    with(NotificationManagerCompat.from(this)) {
                        // notificationId is a unique int for each notification that you must define
                        notify(12, notification)
                    }
                }

            }


        }

        Intent(this, activityRecordAudio::class.java).also { intent ->
            bindService(intent, connectionAudio, Context.BIND_AUTO_CREATE)
        }
        startScan(bluetoothLeScanner)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    private fun startScan(bluetoothLeScanner: BluetoothLeScanner?) {


        val beaconMacAddress = "00:13:AA:00:7E:59"
        val beaconMacAddress2 = "88:C2:55:B0:2D:E1"
        val beaconMacAddress3 = "00:13:AA:00:86:C4"
        val scanFilterBuilder = listOf(
            ScanFilter.Builder().setDeviceAddress(beaconMacAddress).build(),
            ScanFilter.Builder().setDeviceAddress(beaconMacAddress2).build(),
            ScanFilter.Builder().setDeviceAddress(beaconMacAddress3).build()
        )

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()

        Thread(Runnable {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_SCAN
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        while (true) {
                            allertDialogok = false
                            Thread.sleep(100)
                            allertDialogok = true
                            bluetoothLeScanner?.startScan(
                                scanFilterBuilder,
                                scanSettings,
                                leScanCallback
                            )

                            allert = true
                            allertDialogok = true
                            Thread.sleep(scanStart)
                            conteggio = conteggio + 1
                            println("il conteggio:" + conteggio)
                            allert = false

                            bluetoothLeScanner?.stopScan(leScanCallback)
                            f = 0
                            if (alreadyConnected != "")
                                if (conteggio > 3) {
                                    alreadyConnected = ""
                                    deviceName = ""
                                    restart = true
                                    conteggio = 0 //
                                    stopAudio()
                                    println("chiudo")
                                }
                            Thread.sleep(scanStop)

                        }
                    }
                } else {
                    while (true) {
                        bluetoothLeScanner?.startScan(
                            scanFilterBuilder,
                            scanSettings,
                            leScanCallback
                        )
                        allert = true
                        allertDialogok = true
                        Thread.sleep(scanStart)
                        conteggio = conteggio + 1
                        println("il conteggio:" + conteggio)
                        allert = false
                        allertDialogok = false
                        bluetoothLeScanner?.stopScan(leScanCallback)
                        f = 0
                        if (alreadyConnected != "")
                            if (conteggio > 3) {
                                alreadyConnected = ""
                                deviceName = ""
                                restart = true
                                conteggio = 0 //
                                stopAudio()
                                println("chiudo")
                            }
                        Thread.sleep(scanStop)

                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()


    }

    val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            try {
                LeDeviceListAdapter(result)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }

    private fun LeDeviceListAdapter(result: ScanResult?) {
        try {
            val device: BluetoothDevice? = result?.device
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
                deviceName = device?.name ?: "Unknown Device"
            } else {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    deviceName = device?.name ?: "Unknown Device"
                }
            }

            val deviceRSSI: Int? = result?.rssi
            val deviceAddress = device?.address ?: "Unknown Address"
            if (beaconBL.contains(deviceName)) {
                conteggio = 0
                restart = false
                if (deviceRSSI != null) {
                    if (deviceName != alreadyConnected) {
                        f = f + 1
                        RSSI = deviceRSSI
                        start = true
                        disconnect = false
                        //allert = true
                        if (notificaaschermospento) {
                            roomNotification()
                        }

                    } else {
                        RSSI = deviceRSSI
                        conteggio = 0
                            RSSId.add(RSSI)
                            print("aggiunto")
                    }
                }
            } else {
                println("non ha torvato nulla")

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        // qua
    }

    fun rssiClear(){
        RSSId.clear()
    }
    fun startAudio() {

        mServiceAudio.audioOptionsFun(true, this)
    }

    fun stopAudio() {
        mServiceAudio.audioStop()
    }

    fun allertNotification() {
        allert = false
        start = false
    }


    fun dbIsReady() {
        mServiceAudio.startRecordingAudio()


    }

    fun allertNotificationRestart() {
        alreadyConnected = ""
        //allert = true
        //start = true

    }

    private fun roomNotification() {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(
                    this, 0, notificationIntent,
                    PendingIntent.FLAG_MUTABLE
                )
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val chan = NotificationChannel(
                "Room",
                "Room", NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(chan)


            val notification: Notification = Notification.Builder(this, "Room")
                .setOngoing(true)
                .setContentTitle("Controllo stanza")
                .setContentText("Sei dentro questa stanza?")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)

                .build()
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                with(NotificationManagerCompat.from(this)) {
                    // notificationId is a unique int for each notification that you must define
                    notify(10, notification)
                }
            }


        }
    }

    fun removeNotification() {
        val removeContext: String = Context.NOTIFICATION_SERVICE
        val removeNotificationManager: NotificationManager =
            this.getSystemService(removeContext) as NotificationManager
        removeNotificationManager.cancel(10)

    }

    fun beaconFind(): Boolean {
        return start
    }

    fun deviceRoom(): String {
        val prova: String = deviceName
        return deviceName
    }

    inner class LocalBinder : Binder() {
        fun getService(): serviceBLE = this@serviceBLE
    }

    fun stoScan() {

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun mediaValue(): ArrayList<Int> {
        return mServiceAudio.samples
    }
    fun rssiArray(): ArrayList<Int>{
        return RSSId
    }

    fun clearList() {
        mServiceAudio.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }


}











