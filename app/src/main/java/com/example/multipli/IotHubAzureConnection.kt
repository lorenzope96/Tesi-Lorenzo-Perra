package com.example.multipli

import android.os.Bundle
import androidx.activity.ComponentActivity

import com.microsoft.azure.sdk.iot.device.*
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol
import com.microsoft.azure.sdk.iot.device.exceptions.*
import com.microsoft.azure.sdk.iot.device.ClientOptions




import com.microsoft.azure.sdk.iot.device.ProxySettings

import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Proxy.Type
import java.net.URISyntaxException

class IotHubAzureConnection : ComponentActivity() {
    private val D2C_MESSAGE_TIMEOUT_MILLISECONDS : Int = 10000 // tempo massimo di attesa di un messaggio per essere inviato, Tipicamente questa operazione finisce nel giro di qualche secondo
    // qui c'è la parte che non ho capito benissimo. Mi sa che sta facendo un controllo dello stato

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // nell'esempio di microsoft fa un primo controllo se gli sono stati inviati tutti i dati necessari per la connessione
        // per adesso a me non interessa, vorrei prima provare a farlo collegare dando io in ingresso le variabili necessarie
        val connString : String = "HostName=raccoltadatiTesi.azure-devices.net;DeviceId=Tesi:Oneplus8;SharedAccessKey=pgM6QSl7a4qLF/1CszYXLhW6fHm6FgMIaIiApdoBa68="//HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key> or HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key>;GatewayHostName=<gateway>
        val protocol : IotHubClientProtocol = IotHubClientProtocol.HTTPS // ho scleto questo
        val numRequest : Int = 4  //numero di richeste da inviare, qua ho messo 0 perchè non so cosa sia
        // nell'esempio fa un controllo sul tipo di protocollo da inserire
        val proxyHostname : String = "proxy hostname (ie: '127.0.0.1', 'localhost', etc.)" // non so cosa andrà messo
        val proxyPort : Int = 0 //proxy port number
        val proxyUsername : String = "questo campo è opzionale"
        val proxyPassword : CharArray = charArrayOf()

        // qua dovrebbero iniziare le operazione per collegarsi
        //val inetSocketAddress : InetSocketAddress = InetSocketAddress(proxyHostname, proxyPort)
       // val proxy : Proxy = Proxy(Type.HTTP,inetSocketAddress)
       // val httpProxySettings : ProxySettings = ProxySettings(proxy,proxyUsername,proxyPassword)
        val clientOptions : ClientOptions = ClientOptions()
        clientOptions.modelId
        val client : DeviceClient = DeviceClient(connString,protocol,clientOptions)
        client.open()
        try {
            val message : Message= Message("prova")
            client.sendEventAsync(message, object : IotHubEventCallback {
                override fun execute(status: IotHubStatusCode, context: Any) {
                    if (status == IotHubStatusCode.OK) {
                        println("è riuscito a inviare")

                    } else {
                        println("non ha inviato")

                    }
                }
            }, null)
        }catch (e : IotHubException){
            println(e)
        }





    }

}

