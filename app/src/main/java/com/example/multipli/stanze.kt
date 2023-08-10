package com.example.multipli

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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

class stanze : Fragment() {
    private  var series1: LineGraphSeries<DataPoint> = LineGraphSeries()
    private  var series2: LineGraphSeries<DataPoint> = LineGraphSeries()
    private  var series3: LineGraphSeries<DataPoint> = LineGraphSeries()
    private lateinit var semineterrato : TextView
    private lateinit var PrimoPiano : TextView
    private lateinit var SecondoPiano : TextView
    private lateinit var urlConnectionIsts: URL
    private lateinit var graphView1: GraphView
    private lateinit var graphView2: GraphView
    private lateinit var graphView3: GraphView
    private var strOrarioDay = ""
    private var jsonResponseSt = StringBuilder()
    private var xD1 = 8.0
    private var xD2 = 8.0
    private var xD3 = 8.0
    private var yD1 = 0.0
    private var yD2 = 0.0
    private var yD3 = 0.0
    private var i1 = 0.0
    private var i2 = 0.0
    private var i3 =0.0
    private var start : Boolean = true
    private var isDoingUpdateDayStanze = false
    private var startThreadData = true
     var dontUpdate = true



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view : View = inflater.inflate(R.layout.fragment_stanze, container,false)
        graphView1 = view.findViewById(R.id.stanza1)
        graphView2 = view.findViewById(R.id.stanza2)
        graphView3 = view.findViewById(R.id.stanza3)
        semineterrato = view.findViewById(R.id.textIstantSeminterrato)
        PrimoPiano = view.findViewById(R.id.textIstantPrimoPiano)
        SecondoPiano = view.findViewById(R.id.textIstantSecondoPiano)
        graphView1.viewport.isYAxisBoundsManual = true
        graphView2.viewport.isYAxisBoundsManual = true
        graphView3.viewport.isYAxisBoundsManual = true
        graphView1.viewport.setMinY(0.0)
        graphView1.viewport.setMaxY(100.0)
        graphView2.viewport.setMinY(0.0)
        graphView2.viewport.setMaxY(100.0)
        graphView3.viewport.setMaxY(0.0)
        graphView3.viewport.setMaxY(100.0)
        graphView1.viewport.setMinX(8.0)
        graphView1.viewport.isXAxisBoundsManual= true
        graphView2.viewport.isXAxisBoundsManual= true
        graphView3.viewport.isXAxisBoundsManual= true
        graphView1.viewport.setMaxX(18.0)
        graphView2.viewport.setMinX(8.0)
        graphView2.viewport.setMaxX(18.0)
        graphView3.viewport.setMinX(8.0)
        graphView3.viewport.setMaxX(18.0)

        graphView1.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter(){
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                if(!isValueX){
                    return getYAxisLabel(value)
                }
                return super.formatLabel(value, isValueX)
            }
        }
        graphView2.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter(){
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                if(!isValueX){
                    return getYAxisLabel(value)
                }
                return super.formatLabel(value, isValueX)
            }
        }
        graphView3.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter(){
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                if(!isValueX){
                    return getYAxisLabel(value)
                }
                return super.formatLabel(value, isValueX)
            }
        }
        dataDayRequest()
        //dataIntRequest()


        return view
    }

    private fun dataIntRequest() {
        Thread{
            val urlStringSeminterratoIstant = "https://tesilorenzo.azurewebsites.net/istant?Room=Seminterrato"
            val urlStringPrimoPianoIstant = "https://tesilorenzo.azurewebsites.net/istant?Room=PrimoPiano"
            val urlStringSecondoPianoIstant = "https://tesilorenzo.azurewebsites.net/istant?Room=SecondoPiano"
            while (startThreadData){
                if (start){
                    try {
                        val urlSeminterratoInst = URL(urlStringSeminterratoIstant)
                        val urlPrimoPianoInst = URL(urlStringPrimoPianoIstant)
                        val urlSecondoPianoInst = URL(urlStringSecondoPianoIstant)
                        urlConnectionIsts = urlSeminterratoInst
                        var urlConnectionDatiIst : HttpURLConnection = urlConnectionIsts.openConnection() as HttpURLConnection
                        var rd = BufferedReader(InputStreamReader(urlConnectionDatiIst.inputStream))
                        var dati : String = rd.readLine()
                        if (dati != "no" && dati != "non ancora creata"){
                           i1 = dati.toDouble()
                            if (i1==0.0){
                                semineterrato.text = "No Data"
                            }
                            if (i1 >0.0 && i1<30.0 )
                                semineterrato.text = "Silenzio"
                            if (i1 >=30.0 && i1< 50.0)
                                semineterrato.text = "Brusio"
                            if (i1 >= 50.0 && i1< 70.0)
                                semineterrato.text = "Riunione"
                            if (i1 >= 70.0 && i1 < 90.0)
                                semineterrato.text = "Rumore"
                            if (i1 >= 90.0 && i1 <= 100.0){
                                semineterrato.text = "Frastuono"
                            }
                        }
                        urlConnectionIsts = urlPrimoPianoInst
                         urlConnectionDatiIst  = urlConnectionIsts.openConnection() as HttpURLConnection
                         rd = BufferedReader(InputStreamReader(urlConnectionDatiIst.inputStream))
                         dati  = rd.readLine()
                        if (dati != "no" && dati != "non ancora creata"){
                            i2 = dati.toDouble()
                            if (i2==0.0){
                                PrimoPiano.text = "No Data"
                            }
                            if (i2 >0.0 && i2<30.0 )
                                PrimoPiano.text = "Silenzio"
                            if (i2 >=30.0 && i2< 50.0)
                                PrimoPiano.text = "Brusio"
                            if (i2 >= 50.0 && i2< 70.0)
                                PrimoPiano.text = "Riunione"
                            if (i2 >= 70.0 && i2 < 90.0)
                                PrimoPiano.text = "Rumore"
                            if (i2 >= 90.0 && i2 <= 100.0){
                                PrimoPiano.text = "Frastuono"
                            }
                        }
                        urlConnectionIsts = urlSecondoPianoInst
                        urlConnectionDatiIst  = urlConnectionIsts.openConnection() as HttpURLConnection
                        rd = BufferedReader(InputStreamReader(urlConnectionDatiIst.inputStream))
                        dati  = rd.readLine()
                        if (dati != "no" && dati != "non ancora creata"){
                            i3 = dati.toDouble()
                            if (i3==0.0){
                                SecondoPiano.text = "No Data"
                            }
                            if (i3 >0.0 && i3<30.0 )
                                SecondoPiano.text = "Silenzio"
                            if (i3 >=30.0 && i3< 50.0)
                                SecondoPiano.text = "Brusio"
                            if (i3 >= 50.0 && i3< 70.0)
                                SecondoPiano.text = "Riunione"
                            if (i3 >= 70.0 && i3 < 90.0)
                                SecondoPiano.text = "Rumore"
                            if (i3 >= 90.0 && i3 <= 100.0){
                                SecondoPiano.text = "Frastuono"
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
        Thread{
            try {

                isDoingUpdateDayStanze = true


                    var urlDay = URL("https://tesilorenzo.azurewebsites.net?Room=Seminterrato")
                var urlConnectionDay: HttpURLConnection =
                    urlDay.openConnection() as HttpURLConnection // ora sto aprendo la connessione per chiedere i dati che vanno dalle 8
                var rdDay = BufferedReader(InputStreamReader(urlConnectionDay.inputStream))
                var responseDay: String = rdDay.readLine()
                var jsonObject = JSONObject(responseDay)

                urlConnectionDay.disconnect()

                var rumoreArray = jsonObject.getJSONArray("Rumore")
                var xArray = jsonObject.getJSONArray("X")
                var jsonArray =
                    JSONArray(rumoreArray.toString()) // qua mi arriva il vettore sotto forma di json
                var jsonArrayX = JSONArray(xArray.toString())
                for (i in 0 until jsonArray.length()) {   // qui prendo tutti i dati all'interno del file json
                    val item = jsonArray.getString(i)
                    val itemx= jsonArrayX.getString(i)
                    yD1 = item.toDouble()
                    xD1 = itemx.toDouble()
                    println("asses x"+xD1)
                    series1.appendData(DataPoint(xD1, yD1), false, 100000)
                }
                //graphView.removeAllSeries()
                //graphView.addSeries(series)
                graphView1.onDataChanged(true, true)


                graphView1.addSeries(series1)


                     urlDay = URL("https://tesilorenzo.azurewebsites.net?Room=PrimoPiano")
                     urlConnectionDay = urlDay.openConnection() as HttpURLConnection // ora sto aprendo la connessione per chiedere i dati che vanno dalle 8
                 rdDay = BufferedReader(InputStreamReader(urlConnectionDay.inputStream))
                 responseDay = rdDay.readLine()
                 jsonObject = JSONObject(responseDay)

                urlConnectionDay.disconnect()

                rumoreArray = jsonObject.getJSONArray("Rumore")
                xArray = jsonObject.getJSONArray("X")
                 jsonArray = JSONArray(rumoreArray.toString()) // qua mi arriva il vettore sotto forma di json
                 jsonArrayX = JSONArray(xArray.toString())
                for (i in 0 until jsonArray.length()) {   // qui prendo tutti i dati all'interno del file json
                    val item = jsonArray.getString(i)
                    val itemx= jsonArrayX.getString(i)
                    yD2 = item.toDouble()
                    xD2 = itemx.toDouble()

                    series2.appendData(DataPoint(xD2, yD2), false, 100000)
                }
                //graphView.removeAllSeries()
                //graphView.addSeries(series)
                graphView2.onDataChanged(true, true)


                graphView2.addSeries(series2)

                     urlDay = URL("https://tesilorenzo.azurewebsites.net?Room=SecondoPiano")
                urlConnectionDay = urlDay.openConnection() as HttpURLConnection // ora sto aprendo la connessione per chiedere i dati che vanno dalle 8
                 rdDay = BufferedReader(InputStreamReader(urlConnectionDay.inputStream))
                 responseDay = rdDay.readLine()
                 jsonObject = JSONObject(responseDay)

                urlConnectionDay.disconnect()

                 rumoreArray = jsonObject.getJSONArray("Rumore")
                 xArray = jsonObject.getJSONArray("X")
                jsonArray = JSONArray(rumoreArray.toString()) // qua mi arriva il vettore sotto forma di json
                 jsonArrayX = JSONArray(xArray.toString())
                for (i in 0 until jsonArray.length()) {   // qui prendo tutti i dati all'interno del file json
                    val item = jsonArray.getString(i)
                    val itemx= jsonArrayX.getString(i)
                    yD3 = item.toDouble()
                    xD3 = itemx.toDouble()

                    series3.appendData(DataPoint(xD3, yD3), false, 100000)
                }
                //graphView.removeAllSeries()
                //graphView.addSeries(series)
                graphView3.onDataChanged(true, true)


                graphView3.addSeries(series3)
                isDoingUpdateDayStanze = false
                dataIntRequest()


        }
            catch (e:Exception){
                print(e)
            }
        }.start()

    }

    private fun getYAxisLabel(value: Double): String {
        try {


        return when {
            value == 0.0 -> "No data"
            value > 0.0 && value <= 30.0 -> "Silenzio"
            value > 30.0 && value <= 50.0 -> "Brusio"
            value >= 50.0 && value <= 70.0 -> "Riunione"
            value > 70.0 && value <= 90.0 -> "Rumore"
            value > 90.0 && value <= 100.0 -> "Frastuono"
            else -> value.toString()
        }
        }catch (e:Exception){
            println(e)
        }
        return value.toString()
    }

    override fun onStop() {
        super.onStop()
        start = false
        series1.resetData(arrayOf(DataPoint(8.0,0.0)))
        series2.resetData(arrayOf(DataPoint(8.0,0.0)))
        series3.resetData(arrayOf(DataPoint(8.0,0.0)))
        startThreadData = false

    }

    override fun onStart() {
        super.onStart()
        start = true
        startThreadData = true
        if(!graphView1.isShown )
        {
            //if (!isDoingUpdateDayStanze){
            series1.resetData(arrayOf(DataPoint(8.0,0.0)))
            series2.resetData(arrayOf(DataPoint(8.0,0.0)))
            series3.resetData(arrayOf(DataPoint(8.0,0.0)))
        dataDayRequest()
          //  }
        }
        dataIntRequest()
    }

}