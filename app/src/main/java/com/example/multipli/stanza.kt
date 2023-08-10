package com.example.multipli
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Vector

class stanza : Fragment() {
    private  var series: LineGraphSeries<DataPoint> = LineGraphSeries()
    private lateinit var graphView: GraphView
    private lateinit var urlConnectionIst: URL
    lateinit var roomText: TextView
    lateinit var dataText : TextView
    private lateinit var textViewModel: MainActivity.TextViewModel
    private   var x : Double = 0.0
    private var xD : Double = 8.0
    private var yD : Double = 0.0
    private var start : Boolean = true
    private var ok : Boolean = true
    val jsonResponse = StringBuilder()
    private var strOrarioDay = ""
    private var testUrlStringa =""
    private var startThreadData = true
    private var firstTime = true
     var isDoingUpdateDayStanza = false
    private var y :Int = 0



    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
            val view = inflater.inflate(R.layout.fragment_stanza, container, false)
        graphView = view.findViewById(R.id.graph)
        roomText = view.findViewById(R.id.room)
        dataText = view.findViewById(R.id.textDataIst)
        //graphView.gridLabelRenderer.isVerticalLabelsVisible = true
        graphView.viewport.setMinY(0.0)
        graphView.viewport.setMaxY(100.0)
        graphView.viewport.setMinX(8.0)
        graphView.viewport.setMaxX(18.0)
        graphView.viewport.isYAxisBoundsManual = true
        graphView.viewport.isXAxisBoundsManual = true

        textViewModel = ViewModelProvider(requireActivity()).get(MainActivity.TextViewModel::class.java)
        textViewModel.textData.observe(viewLifecycleOwner) { text ->
            roomText.text = text

        }

        graphView.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter(){
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                if(!isValueX){
                    return getYAxisLabel(value)
                }
                return super.formatLabel(value, isValueX)
            }
        }
        //graphView.gridLabelRenderer.verticalAxisTitleTextSize = 10f


        startData()

         // questo deve essere posizionato in modo corretto


        return view
    }


    private fun getYAxisLabel(value: Double): String {
        return when {
            value == 0.0 -> "No data"
            value > 0.0 && value <= 20.0 -> "Silenzio"
            value > 20.0 && value <= 40.0 -> "Brusio"
            value > 40.0 && value <= 60.0 -> "Riunione"
            value > 60.0 && value <= 80.0 -> "Rumore"
            value > 80.0 && value <= 100.0 -> "Frastuono"
            else -> value.toString()
        }

    }

    private fun startData() {
        Thread(Runnable {
        while(ok){
            try {
                if (roomText.text != ""){
                    ok = false
                    startThreadData = true
                    //dataUpdateIstant()
                    Thread.sleep(1000)
                    dataDayRequest()

                }


            }catch (e:Exception){
                print(e)
            }
        }
            Thread.interrupted()
    }).start()
    }

    private fun dataUpdateIstant() {
        firstTime = false
        val urlStringSeminterratoIstant = "https://tesilorenzo.azurewebsites.net/istant?Room=Semineterrato"
        val urlStringPrimoPianoIstant = "https://tesilorenzo.azurewebsites.net/istant?Room=PrimoPiano"
        val urlStringSecondoPianoIstant = "https://tesilorenzo.azurewebsites.net/istant?Room=SecondoPiano"
        Thread{
        while (startThreadData){
            if (start){
                if(roomText.text ==""){
                    series.resetData(arrayOf(DataPoint(8.0,0.0)))
                    dataDayRequest()
                }
                try {
                    val urlSeminterratoInst = URL(urlStringSeminterratoIstant)
                    val urlPrimoPianoInst = URL(urlStringPrimoPianoIstant)
                    val urlSecondoPianoInst = URL(urlStringSecondoPianoIstant)
                    if (roomText.text !=""){
                        if (roomText.text == "Stanza : Seminterrato"){
                            urlConnectionIst = urlSeminterratoInst
                        }
                        if (roomText.text == "Stanza : Primo Piano"){
                            urlConnectionIst = urlPrimoPianoInst
                        }
                        if (roomText.text == "Stanza : Secondo Piano"){
                            urlConnectionIst = urlSecondoPianoInst
                        }
                        var urlConnectionDatiIst : HttpURLConnection = urlConnectionIst.openConnection() as HttpURLConnection
                        var rd = BufferedReader(InputStreamReader(urlConnectionDatiIst.inputStream))
                        var dati : String = rd.readLine()
                        if (dati != "no" && dati != "non ancora creata"){
                            x = dati.toDouble()
                            if (x==0.0){
                                dataText.text = "No Data"
                            }
                            if (x >0.0 && x<30.0 )
                                dataText.text = "Silenzio"
                            if (x >=30 && x< 50.0)
                                dataText.text = "Brusio"
                            if (x >= 50.0 && x< 70.0)
                                dataText.text = "Riunione"
                            if (x >= 70.0 && x<90.0)
                                dataText.text = " Rumore"
                            if (x>=90.0 ){
                                dataText.text ="Frastuono"
                            }
                        }

                    }
                    Thread.sleep(30000)

                }catch (e:Exception){
                    print(e)
                }

           }
        }
            Thread.interrupted()
    }.start()
    }


    private fun dataDayRequest() {
        Thread {
            try {
                isDoingUpdateDayStanza = true
                if (roomText.text == "Stanza : Seminterrato") {
                        testUrlStringa ="https://tesilorenzo.azurewebsites.net?Room=Seminterrato"

                        val urlDay =
                            URL(testUrlStringa) // in questo modo itera da solo gli orari che vanno dalle 8 alle 18 sempre della stessa stanza
                        val urlConnectionDay: HttpURLConnection =
                            urlDay.openConnection() as HttpURLConnection // ora sto aprendo la connessione per chiedere i dati che vanno dalle 8
                        val rdDay = BufferedReader(InputStreamReader(urlConnectionDay.inputStream))
                        val responseDay: String = rdDay.readLine()
                        val jsonObject = JSONObject(responseDay)

                        urlConnectionDay.disconnect()

                        val rumoreArray = jsonObject.getJSONArray("Rumore")
                        val xArray = jsonObject.getJSONArray("X")
                        val jsonArray =
                            JSONArray(rumoreArray.toString()) // qua mi arriva il vettore sotto forma di json
                        val jsonArrayX = JSONArray(xArray.toString())
                        for (i in 0 until jsonArray.length()) {   // qui prendo tutti i dati all'interno del file json
                            val item = jsonArray.getString(i)
                            val itemx= jsonArrayX.getString(i)
                            yD = item.toDouble()
                            xD = itemx.toDouble()
                            println("asses x"+xD)
                            series.appendData(DataPoint(xD, yD), false, 100000)
                        }
                        //graphView.removeAllSeries()
                        //graphView.addSeries(series)
                        graphView.onDataChanged(true, true)


                        graphView.addSeries(series)
                }
                if (roomText.text == "Stanza : Primo Piano") {
                        testUrlStringa ="https://tesilorenzo.azurewebsites.net?Room=PrimoPiano"

                        val urlDay =
                            URL(testUrlStringa) // in questo modo itera da solo gli orari che vanno dalle 8 alle 18 sempre della stessa stanza
                        val urlConnectionDay: HttpURLConnection =
                            urlDay.openConnection() as HttpURLConnection // ora sto aprendo la connessione per chiedere i dati che vanno dalle 8
                        val rdDay = BufferedReader(InputStreamReader(urlConnectionDay.inputStream))
                        val responseDay: String = rdDay.readLine()
                        val jsonObject = JSONObject(responseDay)

                        urlConnectionDay.disconnect()

                        val rumoreArray = jsonObject.getJSONArray("Rumore")
                        val xArray = jsonObject.getJSONArray("X")
                        val jsonArray =
                            JSONArray(rumoreArray.toString()) // qua mi arriva il vettore sotto forma di json
                        val jsonArrayX = JSONArray(xArray.toString())
                        for (i in 0 until jsonArray.length()) {   // qui prendo tutti i dati all'interno del file json
                            val item = jsonArray.getString(i)
                            val itemx= jsonArrayX.getString(i)
                                yD = item.toDouble()
                                xD = itemx.toDouble()
                                println("asses x"+xD)
                            series.appendData(DataPoint(xD, yD), false, 100000)
                        }
                        //graphView.removeAllSeries()
                        //graphView.addSeries(series)
                        graphView.onDataChanged(true, true)


                    graphView.addSeries(series)

                }
                if (roomText.text == "Stanza : Secondo Piano") {
                    testUrlStringa ="https://tesilorenzo.azurewebsites.net?Room=SecondoPiano"

                    val urlDay =
                        URL(testUrlStringa) // in questo modo itera da solo gli orari che vanno dalle 8 alle 18 sempre della stessa stanza
                    val urlConnectionDay: HttpURLConnection =
                        urlDay.openConnection() as HttpURLConnection // ora sto aprendo la connessione per chiedere i dati che vanno dalle 8
                    val rdDay = BufferedReader(InputStreamReader(urlConnectionDay.inputStream))
                    val responseDay: String = rdDay.readLine()
                    val jsonObject = JSONObject(responseDay)

                    urlConnectionDay.disconnect()

                    val rumoreArray = jsonObject.getJSONArray("Rumore")
                    val xArray = jsonObject.getJSONArray("X")
                    val jsonArray =
                        JSONArray(rumoreArray.toString()) // qua mi arriva il vettore sotto forma di json
                    val jsonArrayX = JSONArray(xArray.toString())
                    for (i in 0 until jsonArray.length()) {   // qui prendo tutti i dati all'interno del file json
                        val item = jsonArray.getString(i)
                        val itemx= jsonArrayX.getString(i)
                        yD = item.toDouble()
                        xD = itemx.toDouble()
                        println("asses x"+xD)
                        series.appendData(DataPoint(xD, yD), false, 100000)
                    }
                    //graphView.removeAllSeries()
                    //graphView.addSeries(series)
                    graphView.onDataChanged(true, true)


                    graphView.addSeries(series)

                }
                if(roomText.text == ""){
                    series.resetData(arrayOf(DataPoint(8.0,0.0)))
                }
                isDoingUpdateDayStanza = false
                dataUpdateIstant()


            }
            catch (e: Exception) {
                println(testUrlStringa)
                print(e)
            }
            Thread.interrupted()
        }.start()
    }

    override fun onStop() {
        super.onStop()
        startThreadData = false
        series.resetData(arrayOf(DataPoint(8.0,0.0)))
        start = false
    }

    override fun onStart() {
        super.onStart()
        if (firstTime == false){

        ok = true
        start = true
            if (!isDoingUpdateDayStanza)
        startData()
            else
                dataUpdateIstant()
        }

        //dataDayRequest()
    }


}
