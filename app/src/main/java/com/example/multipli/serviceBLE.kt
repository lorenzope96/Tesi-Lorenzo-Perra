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
import android.bluetooth.le.ScanResult
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat


class serviceBLE: Service() {
     private val binder = LocalBinder()
     private val beaconBL : ArrayList<String> = ArrayList()
     private lateinit var bluetoothManager: BluetoothManager
     private lateinit var bluetoothAdapter: BluetoothAdapter
     private lateinit var bluetoothLeScanner: BluetoothLeScanner
     lateinit var  deviceName: String
     private  var  start : Boolean = false
     var  allert : Boolean = false
     var alreadyConnected : String = ""
     var main = MainActivity()
    private lateinit var mServiceAudio: activityRecordAudio
    private var mBoundAudio: Boolean = false

    private val connectionAudio = object : ServiceConnection {   //activityRecordAudio

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            val binderAudio=service as activityRecordAudio.LocalBinderAudio
            mServiceAudio = binderAudio.getService()
            mBoundAudio = true
        }
        override fun onServiceDisconnected(arg0: ComponentName) {
            mBoundAudio = false
        }
    }

     override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
         bluetoothManager = getSystemService(BluetoothManager::class.java)
         bluetoothAdapter = bluetoothManager.adapter
         bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
         beaconBL.add("testLab")
         startScan(bluetoothLeScanner)
         val pendingIntent: PendingIntent =
             Intent(this, serviceBLE::class.java).let { notificationIntent ->
                 PendingIntent.getActivity(
                     this, 0, notificationIntent,
                     PendingIntent.FLAG_MUTABLE
                 )
             }
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             val manager: NotificationManager =
                 getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
             val chan = NotificationChannel(
                 "Bluetooth",
                 "Bluetooth", manager.importance
             )
             manager.createNotificationChannel(chan)


           val notification: Notification = Notification.Builder(this, "Bluetooth")
                 .setOngoing(true)
                 .setContentTitle("Ricerca dispositivi")
                 .setContentText("Ricerca dei dispositivi per potersi collegare al beacon")
                 .setSmallIcon(R.drawable.ic_launcher_foreground)
                 .setContentIntent(pendingIntent)

                 .build()
             startForeground(startId,notification)
         }

         Intent(this, activityRecordAudio::class.java).also { intent ->
             bindService(intent, connectionAudio, Context.BIND_AUTO_CREATE)}
         return START_STICKY
     }

     override fun onBind(intent: Intent?): IBinder? {
         return binder
     }
     private fun startScan(bluetoothLeScanner: BluetoothLeScanner?) {
         Thread(Runnable {
             while (true) {
                 if (ActivityCompat.checkSelfPermission(
                         this,
                         Manifest.permission.BLUETOOTH_SCAN
                     ) == PackageManager.PERMISSION_GRANTED
                 ) {
                     bluetoothLeScanner?.startScan(leScanCallback)
                     Thread.sleep(5000)
                     bluetoothLeScanner?.stopScan(leScanCallback)
                 }

             }}).start()

     }

     val leScanCallback: ScanCallback = object : ScanCallback() {
         override fun onScanResult(callbackType: Int, result: ScanResult?) {
             super.onScanResult(callbackType, result)
             LeDeviceListAdapter(result)
         }

     }

     private fun LeDeviceListAdapter(result: ScanResult?) {
         val device: BluetoothDevice? = result?.device
         if (ActivityCompat.checkSelfPermission(
                 this,
                 Manifest.permission.BLUETOOTH_CONNECT
             ) == PackageManager.PERMISSION_GRANTED
         ) {
             deviceName = device?.name ?: "Unknown Device"
         }
         val deviceRSSI : Int? = result?.rssi
         val deviceAddress = device?.address ?: "Unknown Address"
         if(beaconBL.contains(deviceName)){
             if (deviceRSSI != null) {
                 if (deviceName != alreadyConnected   ){
                    if(deviceRSSI > -90){
                     alreadyConnected = deviceName
                     start= true
                     allert = true
                     roomNotification()
                 }
                 else{
                     alreadyConnected = ""

                 }
                 }

             }
         }
     }

         fun startAudio() {

        mServiceAudio.audioOptionsFun(true,this)
    }

    fun stopAudio(){
        mServiceAudio.audioStop()
    }

    fun allertNotification() {
            allert = false
            start = false
    }


    fun dbIsReady(): Int {
            val prova : Int = mServiceAudio.startRecordingAudio()
            return prova


    }

    fun allertNotificationRestart(){
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
                 "Room", manager.importance
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

     fun beaconFind(): Boolean {
         return start
     }
     fun deviceRoom(): String{
         val prova : String = deviceName
         return deviceName
     }
     inner  class LocalBinder: Binder(){
         fun getService(): serviceBLE = this@serviceBLE

     }
    fun stoScan(){

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
}

     override fun onDestroy() {
         super.onDestroy()
         stopForeground(STOP_FOREGROUND_REMOVE)
         stopSelf()
     }

 }











