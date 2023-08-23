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
     private var g=0
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
     var funzione = true


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
                 .setOngoing(false)  // se su true non si può togliere
                 .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
                 .setContentTitle("Registrazione audio")
                 .setContentText("è stato attivato il service per poter registrare l'audio, appena si farà l'accesso a una stanza l'applicazione inzierà a registrare l'audio")
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
                     .setOngoing(false) // se su true non si può togliere
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


         //audioNotificationremove()

         return START_STICKY

     }
     override fun onBind(intent: Intent?): IBinder {
         return binderAudio
     }

     inner  class LocalBinderAudio: Binder(){
         fun getService(): activityRecordAudio = this@activityRecordAudio

     }


        //fun audioNotificationremove(){
            //val removeContext : String = Context.NOTIFICATION_SERVICE
            //val removeNotificationManager : NotificationManager = this.getSystemService(removeContext) as NotificationManager
            //removeNotificationManager.cancel(13)
      //  }








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
             funzione= true

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
             funzione= true
             audio.start()


                 }


             }
     }

     fun audioStop(){
         stop = false
         funzione = false
         audio.stop()
         audio.reset()
         audio.release()
         if (Build.VERSION.SDK_INT> Build.VERSION_CODES.Q) {
             filetoDelate.delete()
         }

     }
     fun startRecordingAudio(){
         Thread {
             while (funzione) {
                 amplitude = audio.maxAmplitude

                 if (samples.size >= 120) {
                     mediaSamples()
                 } else
                     samples.add(amplitude)
                 Thread.sleep(500)
                 //sendDataTest()

                 //stopForeground(STOP_FOREGROUND_REMOVE)
                 //stopSelf()

             }
             Thread.interrupted()
         }.start()


     }
     fun mediaSamples(){
        media =  samples.average()
         media = 20* Math.log10(media)
     }
     fun clear(){
         samples.clear()
     }




     }











