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
import android.util.Xml.Encoding
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.res.booleanResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.azure.core.http.ContentType
import com.azure.core.util.serializer.JsonSerializer
import com.google.android.material.tabs.TabLayout
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.microsoft.azure.sdk.iot.device.DeviceClient
import com.microsoft.azure.sdk.iot.device.DeviceClientConfig
import com.microsoft.azure.sdk.iot.device.InternalClient
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode
import com.microsoft.azure.sdk.iot.device.Message
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubException
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsTransportManager
import com.microsoft.azure.sdk.iot.device.transport.amqps.IoTHubConnectionType
import java.nio.Buffer
import java.nio.charset.Charset


class MainActivity : AppCompatActivity() {
    private var chiudi: Boolean = false
    private lateinit var permissionLauncher : ActivityResultLauncher<Array<String>>
    private lateinit var contextprova : Context
    private lateinit var mService: serviceBLE
    private lateinit var pageView : ViewPager2
    private lateinit var tab : TabLayout
    private lateinit var textViewModel: TextViewModel
    private lateinit var builder: AlertDialog
    private lateinit var intentBleDachiudere : Intent
    private lateinit var intentAudioDachiudere : Intent
    private var isBluetoothAdvertiseGranted = false
    private var isBluetoothConnectGranted = false
    private var isBluetoothScanGranted = false
    private var isRecordAudioGranted = false
    private var isCorseLocationGranted = false
    private var isFineLocationGranted = false
    private var isNotificationGranted = false
    private var mBound: Boolean = false
    private var visible : Boolean = true
    private  var updatedb : Boolean = false
    private var isUpdatingStanza = true
    var room : String =""
    var frag : stanza = stanza()
    var frag2: stanze = stanze()
    var allertDialog : Boolean = true
    private var provaprova = true
    var c = 0
    private var chiudiservice = false



    private val connection = object : ServiceConnection {    // serviceBLE

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
                val binder = service as serviceBLE.LocalBinder
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

        }
        }
        setContentView(R.layout.activity_main)
        pageView = findViewById(R.id.viewpager)
        tab=findViewById(R.id.tab)
        textViewModel = ViewModelProvider(this).get(TextViewModel::class.java)

            val intentscan = Intent(this, serviceBLE::class.java)
            val audioService = Intent(this, activityRecordAudio::class.java)
        intentBleDachiudere = intentscan
        intentAudioDachiudere = audioService
        provaprova = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                setUpTabs()
                uiUpdate()
                Intent(this, serviceBLE::class.java).also { intent ->
                    bindService(intent, connection, Context.BIND_AUTO_CREATE)}

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S){
                    if (!serviceBLE.isRunning()){
                    applicationContext.startForegroundService(intentscan)
                    applicationContext.startForegroundService(audioService)
                    }
                }
                else {
                    if (!serviceBLE.isRunning()) {
                        applicationContext.startService(intentscan)
                        applicationContext.startService(audioService)
                    }

                }


            }catch (e:Exception){
                e.printStackTrace()
            }

        }
    }
    private fun setUpTabs() {
        val adapter = Adapter(supportFragmentManager, lifecycle)
        adapter.addFragment(stanza(), "Stanza")
        adapter.addFragment(stanze(), "Stanze")
        pageView.adapter = adapter

        pageView.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                tab.setScrollPosition(position, 0f, true)
            }
        })

        tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                pageView.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
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
           val threadProva= Thread {
                try{
                while (provaprova) {

                    if (chiudi == false) {
                            if (mBound) {


                                if (mService.beaconFind()) {
                                    if (mService.allert) {
                                        mService.allertNotification()
                                        if (c >= 1)
                                            builder.cancel()
                                        if (mService.alreadyConnected == "")
                                            allertDialog()

                                    if (mService.deviceRoom() == "testLab") {
                                        room = "Stanza : Seminterrato"
                                    } else if (mService.deviceRoom() == "testLab3") {
                                        room = "Stanza : Primo Piano"
                                    } else if (mService.deviceRoom() == "testLab5") {
                                        room = "Stanza : Secondo Piano"
                                    }
                                    }
                                }
                                else{

                                    if (mService.deviceName !=""){
                                    if (mService.deviceRoom() == "testLab") {
                                        room = "Stanza : Seminterrato"
                                        textViewModel.textData.postValue(room)
                                    } else if (mService.deviceRoom() == "testLab3") {
                                        room = "Stanza : Primo Piano"
                                        textViewModel.textData.postValue(room)
                                    } else if (mService.deviceRoom() == "testLab5") {
                                        room = "Stanza : Secondo Piano"
                                        textViewModel.textData.postValue(room)
                                    }
                                    else{
                                        room = ""
                                    }}

                                }
                            }


                        if (mBound) {
                            if(mService.restart){
                                room = ""
                                textViewModel.textData.postValue(room)
                            }
                            if (c >= 1) {
                                if (mService.allertDialogok == false) {
                                    builder.dismiss()

                                }
                            }
                            updatedb= !mService.restart
                        }

                        // }// qua
                        //}//questo
                    }

                }

                }catch (e:Exception){
                    print(e)
                }
            }
       threadProva.start()


        }

    override fun onPause() {
        super.onPause()
        activityPause()
        visible= false
    }

    private fun activityPause() {
        visible = false
    }



    override fun onRestart() {
        super.onRestart()
            activityResume()
    }

    override fun onStop() {
        super.onStop()
        mService.notificaaschermospento = true
    }

    private fun activityResume() {
        visible = true
        mService.notificaaschermospento = false
    }
    private fun audiostop(){
        mService.stopAudio()
    }

     fun allertDialog(){
         c=c+1
        runOnUiThread {
            try {
                 builder = AlertDialog.Builder(this)
                    .setTitle("Stanza")
                    .setCancelable(false)
                    .setMessage("Ti trovi all'interno di questa stanza?"+ room)
                    .setPositiveButton("Si") { dialog, which ->
                        textViewModel.textData.postValue(room)
                        mService.startAudio()
                        mService.alreadyConnected = mService.deviceName
                        //stopButton.visibility = View.VISIBLE
                        mService.scanStart=10000 //mService.stoScan() //ho commentato questo, probabilmente era lui che non faceva ripartire lo scan
                        mService.scanStop = 60000
                        mService.removeNotification()
                        allertDialog = true
                        updatedb = true
                        dbAudioUpdate()
                        c=0
                    }
                    .setNegativeButton("No") { dialog, which ->
                        room = ""
                        mService.allertNotificationRestart()
                        c=0
                    }
                    .show()
            }catch (e:Exception){
                e.printStackTrace()
            }

        }

    }


    private fun dbAudioUpdate() {
        mService.dbIsReady()
            Thread {
                while (!mService.restart) {
                    val connString : String = "HostName=raccoltadatiTesi.azure-devices.net;DeviceId=Tesi:Oneplus8;SharedAccessKey=pgM6QSl7a4qLF/1CszYXLhW6fHm6FgMIaIiApdoBa68= "
                    if (mService.mediaValue().size == 120){
                       val media= mService.mediaValue().average()
                        val dbMedia : Double = 20 *Math.log10(media)
                        if(dbMedia == 0.0)
                            print("zero")
                        val mediaRSSI = mService.rssiArray().average()
                        mService.clearList()
                        mService.rssiClear()
                    val protocol : IotHubClientProtocol = IotHubClientProtocol.HTTPS
                    val client : DeviceClient = DeviceClient(connString,protocol)
                    client.open()
                    try {
                        val bodyMessage = JsonObject()
                        bodyMessage.addProperty("Noise",dbMedia)
                        bodyMessage.addProperty("RSSI",mediaRSSI)
                        bodyMessage.addProperty("Room",room)

                        val massageString= bodyMessage.toString()
                        val massegeByte = massageString.toByteArray()
                        val msgString : String = "{Value:" + dbMedia +
                                ",Room:" + room + "}"
                        val message = Message(massegeByte)
                        message.contentEncoding = "utf-8"
                        message.contentType = "application/json"


                        client.sendEventAsync(message, iotHubEventCallback,this)
                        mService.clearList()
                        client.closeNow()
                    }
                    catch (e : IotHubException){
                        println(e)
                    }
                    }
                    //roomTextdb.text = dbInt.toString() + "dB"
                    Thread.sleep(500)
                }
                val pino= Thread.interrupted()
                //roomTextdb.text = "0"+"dB"
            }.start()

    }

    val iotHubEventCallback : IotHubEventCallback = object : IotHubEventCallback {
        override fun execute(status: IotHubStatusCode, context: Any) {
            if (status== IotHubStatusCode.OK){
                println("ha inviato")

            }
            else{
                println("non ha inviato")
            }

        }
    }
    class TextViewModel : ViewModel() {
        val textData: MutableLiveData<String> = MutableLiveData()
    }

    override fun onDestroy() {
        super.onDestroy()
        chiudi = true
        chiudiservice = true
        provaprova = false


    }

}