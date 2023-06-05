package com.example.multipli

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var permissionLauncher : ActivityResultLauncher<Array<String>>
    private lateinit var roomText:TextView
    private lateinit var roomTextdb:TextView
    private lateinit var stopButton:Button
    private lateinit var contextprova : Context
    private lateinit var mService: serviceBLE
    private var isBluetoothAdvertiseGranted = false
    private var isBluetoothConnectGranted = false
    private var isBluetoothScanGranted = false
    private var isRecordAudioGranted = false
    private var isCorseLocationGranted = false
    private var isFineLocationGranted = false
    private var isNotificationGranted = false
    private var mBound: Boolean = false
    private var visible : Boolean = true
    private  var updatedb : Boolean = true


    private val connection = object : ServiceConnection {    // serviceBLE

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
           val binder=service as serviceBLE.LocalBinder
            mService = binder.getService()
            mBound = true
        }
        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        permissionLauncher = registerForActivityResult(         // si sta controllando quali permessi si hanno
            ActivityResultContracts.RequestMultiplePermissions()) { permission ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                isBluetoothAdvertiseGranted = permission[Manifest.permission.BLUETOOTH_ADVERTISE]
                    ?: isBluetoothAdvertiseGranted
            isBluetoothConnectGranted =
                permission[Manifest.permission.BLUETOOTH_CONNECT] ?: isBluetoothConnectGranted
            isBluetoothScanGranted =
                permission[Manifest.permission.BLUETOOTH_SCAN] ?: isBluetoothScanGranted
        }

            isRecordAudioGranted = permission[Manifest.permission.RECORD_AUDIO] ?: isRecordAudioGranted
            isCorseLocationGranted = permission[Manifest.permission.ACCESS_COARSE_LOCATION] ?: isCorseLocationGranted
            isFineLocationGranted = permission[Manifest.permission.ACCESS_FINE_LOCATION] ?: isFineLocationGranted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                isNotificationGranted =
                    permission[Manifest.permission.POST_NOTIFICATIONS] ?: isNotificationGranted
            }
            }
        requestPermissions()    // fa partire la funzione per richiedere i permessi
        // serve per accendere il bluethooth nel caso fosse spento
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        if (bluetoothAdapter != null){
        if (!bluetoothAdapter.isEnabled){
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)  // nel caso il Bluetooth fosse of chiedere all'utente di metterlo On

            if (ActivityCompat.checkSelfPermission(   // si sta controllando se si hanno i permessi Bluetooth
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startActivity(enableBtIntent)

            }
            else{
                requestPermissions() // nel caso non si abbiano i permessi viene fatta nuovamente una richiesta
            }

        }}


        setContentView(R.layout.activity_main)
            val textView:TextView= findViewById(R.id.room)
            roomText= textView
            roomTextdb = findViewById(R.id.textdb)
            stopButton = findViewById(R.id.stopbutton)
            val intentscan = Intent(this, serviceBLE::class.java)
            val audioService = Intent(this, activityRecordAudio::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                uiUpdate()
                stopButton.setOnClickListener{
                    mService.stopAudio()
                    updatedb = false
                    stopButton.visibility = View.INVISIBLE

                }
                //applicationContext.startForegroundService(audioService)
                Intent(this, serviceBLE::class.java).also { intent ->
                    bindService(intent, connection, Context.BIND_AUTO_CREATE)}
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S){
                    applicationContext.startForegroundService(intentscan)
                    applicationContext.startForegroundService(audioService)

                }
                else {

                    applicationContext.startService(intentscan)
                    applicationContext.startService(audioService)
                }




            }catch (e:Exception){
                e.printStackTrace()
            }





        }




    }
    private fun requestPermissions() {     // in base alle autorizzazioni che non si hanno vengono messe dentro un vettore per poter essere richieste
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            isBluetoothAdvertiseGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ) == PackageManager.PERMISSION_GRANTED

            isBluetoothConnectGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

            isBluetoothScanGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        }

        isRecordAudioGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        isCorseLocationGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        isFineLocationGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        isNotificationGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

        val permissionRequest : MutableList<String> = ArrayList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){

        if (! isBluetoothAdvertiseGranted){
            permissionRequest.add( Manifest.permission.BLUETOOTH_ADVERTISE)
        }
        if (! isBluetoothConnectGranted){
            permissionRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (!isBluetoothScanGranted) {
            permissionRequest.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        }
        if (! isRecordAudioGranted){
            permissionRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        if (! isCorseLocationGranted) {
            permissionRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
            if (! isFineLocationGranted){
                permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
        if (! isNotificationGranted) {
            permissionRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        }
        if(permissionRequest.isNotEmpty()){
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }
    }
   private fun uiUpdate (){

            contextprova= this
            Thread {
                while (true) {
                    //if (visible) {
                        if(mBound){
                        if (mService.beaconFind()) {
                            if (mService.allert){
                                mService.allertNotification()
                                allertDialog()
                            }

                            if (mService.deviceRoom() == "testLab")
                                roomText.text = "Stanza : Studio"

                        }

                        }// qua
                   // }//questo
                }
            }.start()


        }




    override fun onPause() {
        super.onPause()
        activityPause()
    }

    private fun activityPause() {
        visible = false
    }



    override fun onRestart() {
        super.onRestart()
        Thread {
            activityResume()
        }.start()
    }

    private fun activityResume() {
        visible = true
    }
    private fun audiostop(){
        mService.stopAudio()
    }

     fun allertDialog(){
        runOnUiThread {
            try {
                val builder: AlertDialog? = AlertDialog.Builder(this)
                    .setTitle("Stanza")
                    .setMessage("Ti trovi all'interno di questa stanza?"+ roomText.text)
                    .setPositiveButton("Si") { dialog, which ->
                        mService.startAudio()
                        stopButton.visibility = View.VISIBLE
                        mService.stoScan()
                        dbAudioUpdate()
                    }
                    .setNegativeButton("No") { dialog, which ->
                        roomText.text = "Stanza : "
                        mService.allertNotificationRestart()
                    }
                    .show()
            }catch (e:Exception){
                e.printStackTrace()
            }

        }

    }

    private fun dbAudioUpdate() {
            Thread {
                while (updatedb) {

                    val audioTry: Int = mService.dbIsReady()
                    val dB: Double = 20 * Math.log10(audioTry.toDouble())
                    val dbInt: Int = dB.toInt()
                    roomTextdb.text = dbInt.toString() + "dB"
                    Thread.sleep(500)
                }
                roomTextdb.text = "0"+"dB"
            }.start()

    }


}