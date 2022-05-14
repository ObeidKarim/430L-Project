package com.kfo00.currencyexchange.fragments

import android.app.ActionBar
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.marginLeft
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

import com.kfo00.currencyexchange.R

import com.kfo00.currencyexchange.api.ExchangeService

import com.kfo00.currencyexchange.api.model.Coordinate

import com.kfo00.currencyexchange.api.model.Stat

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList




class StatisticsFragment : Fragment() {

    private var volume: TextView? = null
    private var totalTransactions : TextView? = null


    private var buyMax : TextView? = null
    private var sellMax: TextView? = null
    private var buyMedian: TextView? = null
    private var sellMedian: TextView? = null
    private var buyMode: TextView? = null
    private var sellMode: TextView? = null
    private var buyStd: TextView? = null
    private var sellStd: TextView? = null
    private var buyVar: TextView? = null
    private var sellVar: TextView? = null

    //    graph
    private var graph: LineChart? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        drawGraph()
        getStats()
    }

    override fun onResume() {
        super.onResume()
        drawGraph()
        getStats()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view: View = inflater.inflate(R.layout.fragment_statistics, container, false);
        volume = view.findViewById(R.id.volumeText)
        totalTransactions = view.findViewById(R.id.totalTransText)

        buyMax = view.findViewById(R.id.buyMax)
        buyMedian = view.findViewById(R.id.buyMedian)
        buyMode = view.findViewById(R.id.buyMode)
        buyStd = view.findViewById(R.id.buyStd)
        buyVar = view.findViewById(R.id.buyVar)

        sellMax = view.findViewById(R.id.sellMax)
        sellMedian = view.findViewById(R.id.sellMedian)
        sellMode = view.findViewById(R.id.sellMode)
        sellStd = view.findViewById(R.id.sellStd)
        sellVar = view.findViewById(R.id.sellVar)

        graph = view.findViewById(R.id.lineChart)

//        chartView = view.findViewById(R.id.hc)

        drawGraph()
        getStats()
        return view
    }


//    private fun drawGraph(){
//        var options: HIOptions = HIOptions()
//        var chart: HIChart = HIChart()
//
//
//        chart.zoomType = "x"
//        options.chart = chart
//
//        var title: HITitle = HITitle()
//        title.text = "LBP to USD Exchange Rate Over Time"
//        options.title = title
//
//        var subtitle: HISubtitle = HISubtitle()
//        subtitle.text = "Click and drag in the plot area to zoom"
//        options.subtitle = subtitle
//
//        var xaxis: HIXAxis = HIXAxis()
//        xaxis.type = "datetime"
//        options.xAxis = ArrayList()
//
//        var yaxis: HIYAxis = HIYAxis()
//        yaxis.title = HITitle()
//        yaxis.title.text = "Exchange Rate"
//
//        var legend: HILegend = HILegend()
//        legend.enabled = false
//        options.legend = legend
//
//        var plotoptions : HIPlotOptions = HIPlotOptions()
//        plotoptions.area = HIArea()
//        var stops: LinkedList<HIStop> = LinkedList<HIStop>()
//        stops.add(HIStop(0F, HIColor.initWithRGB(47,126,216)))
//        stops.add(HIStop(1F, HIColor.initWithRGBA(47,126,216, 0.0)))
//        plotoptions.area.fillColor = HIColor.initWithLinearGradient(HIGradient(),stops)
//        plotoptions.area.marker = HIMarker()
//        plotoptions.area.marker.radius = 2
//        plotoptions.area.lineWidth = 1
//
//        var state: HIStates = HIStates()
//        state.hover = HIHover()
//        state.hover.lineWidth = 1
//
//        plotoptions.area.states = state
//        options.plotOptions = plotoptions
//
//        var area:HIArea = HIArea()
//        area.name = "USD to LBP"
//
//
//        var coordinates = getData()
//        var areaData : ArrayList<List<Number>> = ArrayList()
//        for (point in coordinates){
//            var date = Date.parse(point.date_x)
//            var temp = listOf(date,point.buyRate_y!!)
//            areaData.add(temp)
//        }
//
//        Log.d("Coordinates", coordinates.toString())
//        Log.d("Graph Points", areaData.toString())
//
//
//
//        area.data = areaData
//        options.series = ArrayList<HISeries>(Collections.singletonList(area))
//
//
//        chartView?.options = options
//
//
//    }

//    private fun getData():ArrayList<Coordinate>{
//        var coordinates: ArrayList<Coordinate> = ArrayList()
//            ExchangeService.exchangeApi().getCoordinates()
//                .enqueue(object : Callback<List<Coordinate>> {
//                    override fun onFailure(call: Call<List<Coordinate>>, t: Throwable) {
//                        return
//                    }
//                    override fun onResponse(call: Call<List<Coordinate>>, response: Response<List<Coordinate>>
//                    ) {
//                        Log.d("Response", response.body().toString())
//                        coordinates.addAll(response.body()!!)
//                        Log.d("CoordinatesAPI", coordinates.toString())
//                    }
//                })
//
//        return coordinates
//    }


    private fun getStats(){

        ExchangeService.exchangeApi().getStats()
            .enqueue(object : Callback<Stat> {
                override fun onFailure(call: Call<Stat>, t: Throwable) {
                    return
                }
                override fun onResponse(call: Call<Stat>, response: Response<Stat>) {
                    val stats: Stat? = response.body();
                    volume?.text = "Volume: " + stats?.volume.toString() + " USD"
                    totalTransactions?.text = "Total Transactions: " + stats?.numberOfTransactions.toString()

                    buyMax?.text = stats?.max_lToU.toString()
                    buyMedian?.text = stats?.median_lToU.toString()
                    buyMode?.text = stats?.mode_lToU.toString()
                    buyStd?.text = stats?.stdev_lToU.toString()
                    buyVar?.text = stats?.variance_lToU.toString()

                    sellMax?.text = stats?.max_uTol.toString()
                    sellMedian?.text = stats?.median_uTol.toString()
                    sellMode?.text = stats?.mode_uTol.toString()
                    sellStd?.text = stats?.stdev_uTol.toString()
                    sellVar?.text = stats?.variance_uTol.toString()


                }
            })
    }


    private fun drawGraph() {
        ExchangeService.exchangeApi().getCoordinates().enqueue(object :
            Callback<List<Coordinate>> {
            override fun onResponse(call: Call<List<Coordinate>>, response: Response<List<Coordinate>>
            ) {
                val points: List<Coordinate>? = response.body()
                buildGraph(points)
            }


            override fun onFailure(call: Call<List<Coordinate>>, t: Throwable) {
                return
            }
        })
    }

    private fun buildGraph(points: List<Coordinate>?) {

        var xAxisValues: ArrayList<String> = ArrayList()
        var yAxisValues: ArrayList<Entry>  = ArrayList()
        if (points != null) {
            for (index in points.indices){
                xAxisValues.add(points[index].date_x!!)
                yAxisValues.add(Entry(index.toFloat(),points[index].buyRate_y!!))
            }
        }


        var dataSets: ArrayList<ILineDataSet?>? = ArrayList()

        var set: LineDataSet
        set = LineDataSet(yAxisValues, "Buy Rate")
        set.color = Color.rgb(65, 168, 121)
        dataSets!!.add(set)


        graph!!.xAxis.labelRotationAngle = 0f
        graph!!.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisValues)

        graph!!.axisRight.isEnabled = false

        graph!!.setTouchEnabled(true)
        graph!!.setPinchZoom(true)

        graph!!.animateX(1800, Easing.EaseInExpo)

        val data = LineData(dataSets)
        graph!!.data = data
        graph!!.description.isEnabled = false
        graph!!.invalidate()

    }
}

