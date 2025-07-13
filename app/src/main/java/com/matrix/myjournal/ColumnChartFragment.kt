package com.matrix.myjournal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Cartesian
import com.anychart.core.cartesian.series.Column
import com.anychart.enums.Anchor
import com.anychart.enums.HoverMode
import com.anychart.enums.Position
import com.anychart.enums.TooltipPositionMode
import com.matrix.myjournal.databinding.FragmentColumnChartBinding
import com.matrix.myjournal.questionresdatabase.QuestionResDatabase
import com.matrix.myjournal.questionresdatabase.WordCountPerDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ColumnChartFragment : Fragment() {

    private var binding: FragmentColumnChartBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentColumnChartBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchAndDisplayData()
    }

    private fun fetchAndDisplayData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Fetch data from the database
                val wordCounts = withContext(Dispatchers.IO) {
                    QuestionResDatabase.getInstance(requireContext()).questionResDao().getWordCountPerDay()
                }

                // Display the chart with the fetched data
                displayChart(wordCounts)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun displayChart(wordCounts: List<WordCountPerDay>) {
        val columnChartView: AnyChartView? = binding?.anyChartViewColumn

        // Prepare data entries for the column chart
        val columnData: MutableList<DataEntry> = ArrayList()
        for (wordCount in wordCounts) {
            columnData.add(ValueDataEntry(wordCount.entryDate, wordCount.wordCount))
        }

        // Create and configure the column chart
        val cartesian: Cartesian = AnyChart.cartesian()
        val column: Column = cartesian.column(columnData)
        column.tooltip()
            .titleFormat("{%X}")
            .position(Position.CENTER_BOTTOM)
            .anchor(Anchor.CENTER_BOTTOM)
            .offsetX(0.0)
            .offsetY(5.0)
            .format("{%Value} words")

        cartesian.animation(true)
        cartesian.title("Words Written Each Day")
        cartesian.yScale().minimum(0.0)
        cartesian.yAxis(0).labels().format("{%Value} words")
        cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
        cartesian.interactivity().hoverMode(HoverMode.BY_X)
        cartesian.xAxis(0).title("Date")
        cartesian.yAxis(0).title("Word Count")

        // Set the column chart to the view
        columnChartView?.setChart(cartesian)
    }


}
