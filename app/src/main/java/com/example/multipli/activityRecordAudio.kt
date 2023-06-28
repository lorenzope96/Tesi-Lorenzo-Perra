package com.example.multipli


import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.microsoft.azure.sdk.iot.device.ClientOptions
import com.microsoft.azure.sdk.iot.device.DeviceClient
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode
import com.microsoft.azure.sdk.iot.device.Message
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubException
import java.io.File

 class activityRecordAudio: Service() {
     lateinit var audio : MediaRecorder
     private lateinit var filetoDelate: File
     private lateinit var  audioDirectory: File
     val samples : ArrayList<Int> = ArrayList()
     private val binderAudio = LocalBinderAudio()
      var  amplitude : Int = 0
     var ok : Boolean = false
     private var stop: Boolean = true
     var media : Double = 0.0
     var mediadb : Double = 0.0


     override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

         val pendingIntent: PendingIntent =
             Intent(this, activityRecordAudio::class.java).let { notificationIntent ->
                 PendingIntent.getActivity(
                     this, 0, notificationIntent,
                     PendingIntent.FLAG_MUTABLE
                 )
             }
         if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
             val manager: NotificationManager =
                 getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
             val chan = NotificationChannel(
                 "Audio",
                 "Audio", NotificationManager.IMPORTANCE_HIGH
             )
            manager.createNotificationChannel(chan)


             val notification: Notification = Notification.Builder(this, "Audio")
                 .setOngoing(true)
                 .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
                 .setContentTitle("Registrazione audio")
                 .setContentText("sta registrando l'audio per poter campionare i valori di rumore")
                 .setSmallIcon(R.drawable.ic_launcher_foreground)
                 .setContentIntent(pendingIntent)

                 .build()

             startForeground(startId, notification, FOREGROUND_SERVICE_TYPE_MICROPHONE)
         }
         else{
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                 val manager: NotificationManager =
                     getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                 val chan = NotificationChannel(
                     "Audio",
                     "Audio", NotificationManager.IMPORTANCE_HIGH
                 )
                 manager.createNotificationChannel(chan)


                 val notification: Notification = Notification.Builder(this, "Audio")
                     .setOngoing(true)
                     .setContentTitle("Registrazione audio")
                     .setContentText("sta registrando l'audio per poter campionare i valori di rumore")
                     .setSmallIcon(R.drawable.ic_launcher_foreground)


                     .build()
                 if (ActivityCompat.checkSelfPermission(
                         this,
                         Manifest.permission.POST_NOTIFICATIONS
                     ) == PackageManager.PERMISSION_GRANTED
                 ) {
                     with(NotificationManagerCompat.from(this)) {
                         // notificationId is a unique int for each notification that you must define
                         notify(13, notification)
                     }
                 }


             }
         }

         return START_STICKY

     }
     override fun onBind(intent: Intent?): IBinder {
         return binderAudio
     }

     inner  class LocalBinderAudio: Binder(){
         fun getService(): activityRecordAudio = this@activityRecordAudio

     }











      fun audioOptionsFun(start:Boolean, context: Context)  {
         if (start){

         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

             audioDirectory = context.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS)!!
         val file = File(audioDirectory, "test" + ".mp3")
         filetoDelate = file





                  audio= MediaRecorder(context).apply {
                     setAudioSource(MediaRecorder.AudioSource.MIC)
                     AudioManager.PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED
                     setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                     setOutputFile(file)
                     setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                     prepare()
                 }
                    ok= true
                    stop= true

                 audio.start()



     }
             else{
             val audioDirect : String = Environment.getExternalStorageDirectory().path
             val file = File(context.filesDir,"")
             val file2 = File(file, "recording.mp3")
             filetoDelate = file2
                 audio = MediaRecorder()
             audio.setAudioSource(MediaRecorder.AudioSource.MIC)
             audio.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
             audio.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
             AudioManager.PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED
             if (Build.VERSION.SDK_INT> Build.VERSION_CODES.Q){
                 audio.setOutputFile(file2)
             }
             else {
                 audio.setOutputFile("/dev/null")
             }

             audio.prepare()
             ok= true
             stop= true
             audio.start()


                 }


             }
     }

     private fun sendDataTest() {
         val connString : String = "HostName=raccoltadatiTesi.azure-devices.net;DeviceId=Tesi:Oneplus8;SharedAccessKey=pgM6QSl7a4qLF/1CszYXLhW6fHm6FgMIaIiApdoBa68="
         val protocol : IotHubClientProtocol = IotHubClientProtocol.HTTPS
         val client : DeviceClient = DeviceClient(connString,protocol)
         client.open()
         try {
             val dB: Double = 20 * Math.log10(amplitude.toDouble())
             val message = Message(dB.toString())
             client.sendEventAsync(message, iotHubEventCallback,this)
         }catch (e : IotHubException){
             println(e)
         }

     }

     val iotHubEventCallback : IotHubEventCallback = object : IotHubEventCallback{
         override fun execute(status: IotHubStatusCode, context: Any) {
             if (status== IotHubStatusCode.OK){
                 println("ha inviato")
                 
             }
             else{
                 println("non ha inviato")
             }

         }
     }


     fun audioStop(){
         audio.stop()
         if (Build.VERSION.SDK_INT> Build.VERSION_CODES.Q) {
             filetoDelate.delete()
         }
         stop = false
     }
     fun startRecordingAudio(): Int {
           while (true){
         amplitude = audio.maxAmplitude

               if (samples.size >= 30){
                   mediaSamples()
               }
               else
                   samples.add(amplitude)

               val soundPressure = amplitude.toDouble() / Short.MAX_VALUE
               val prova = soundPressure * soundPressure
               val intensita = 10* Math.log10(prova)
               // prova a calcolare la potenza
               val valoreDiRiferimento = Short.MAX_VALUE
               val potenza = (amplitude * amplitude).toDouble() / (2 * valoreDiRiferimento * valoreDiRiferimento).toDouble()
               //sendDataTest()

         //stopForeground(STOP_FOREGROUND_REMOVE)
         //stopSelf()
         return amplitude
     }
         return 0
     }
     fun mediaSamples(){
        media =  samples.average()
         media = 20* Math.log10(media)
     }
     fun clear(){
         samples.clear()
     }


     override fun onDestroy() {
         super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
         stopSelf()//
     }
     }











