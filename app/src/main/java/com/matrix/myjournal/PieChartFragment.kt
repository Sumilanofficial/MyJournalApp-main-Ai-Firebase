package com.matrix.myjournal

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Pie
import com.anychart.enums.TooltipPositionMode
import com.matrix.myjournal.databinding.FragmentPieChartBinding
import com.matrix.myjournal.questionresdatabase.QuestionResDatabase
import com.matrix.myjournal.questionresdatabase.WordCountPerDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PieChartFragment : Fragment() {

    private var binding: FragmentPieChartBinding? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPieChartBinding.inflate(inflater, container, false)
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

                // Observe the total images count
                val totalImagesCountLiveData = QuestionResDatabase.getInstance(requireContext()).questionResDao().getTotalImagesCount()
                totalImagesCountLiveData.observe(viewLifecycleOwner) { totalImagesCount ->
                    // Log fetched data for debugging
                    Log.d("PieChartFragment", "Fetched wordCounts: $wordCounts")
                    Log.d("PieChartFragment", "Fetched totalImagesCount: $totalImagesCount")

                    // Display the chart with the fetched data
                    displayChart(wordCounts, totalImagesCount)
                }
            } catch (e: Exception) {
                // Log error for debugging
                Log.e("PieChartFragment", "Error fetching data", e)
            }
        }
    }


    private fun displayChart(wordCounts: List<WordCountPerDay>, totalImagesCount: Int) {
        val pieChartView: AnyChartView? = binding?.anyChartViewPie

        // Prepare data entries for the pie chart
        val pieData: MutableList<DataEntry> = ArrayList()
        val totalWords = wordCounts.sumOf { it.wordCount }
        pieData.add(ValueDataEntry("Total Words", totalWords))
        pieData.add(ValueDataEntry("Total Images", totalImagesCount))

        // Create and configure the pie chart
        val pie = AnyChart.pie()
        pie.data(pieData)
        pie.title("Total Words and Images")
        pie.labels().position("outside")
        pie.legend().title().enabled(true)
        pie.legend().title().text("Categories").padding(0.0, 0.0, 10.0, 0.0)
        pie.legend().position("bottom")
        pie.tooltip().positionMode(TooltipPositionMode.POINT)

        // Set the pie chart to the view
        pieChartView?.setChart(pie)

        // Log chart setup for debugging
        Log.d("PieChartFragment", "Pie chart displayed with data: $pieData")
    }

    
}
