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
import com.google.firebase.firestore.FirebaseFirestore
import com.matrix.myjournal.DataClasses.JournalEntry
import com.matrix.myjournal.databinding.FragmentColumnChartBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ColumnChartFragment : Fragment() {

    private var binding: FragmentColumnChartBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentColumnChartBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchAndDisplayData()
    }

    private fun fetchAndDisplayData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val wordCounts = withContext(Dispatchers.IO) {
                    val snapshot = FirebaseFirestore.getInstance()
                        .collection("JournalEntries")
                        .get()
                        .await()

                    snapshot.documents.mapNotNull { doc ->
                        val entry = doc.toObject(JournalEntry::class.java)
                        entry?.let {
                            val date = it.entryDate ?: return@let null
                            val wordCount = it.combinedResponse?.split("\\s+".toRegex())?.size ?: 0
                            date to wordCount
                        }
                    }.groupBy { it.first } // Group by date
                        .mapValues { entry -> entry.value.sumOf { it.second } } // Sum word counts
                        .toList()
                }

                displayChart(wordCounts)
            } catch (e: Exception) {
                e.printStackTrace()
                // Show fallback/error UI if needed
            }
        }
    }

    private fun displayChart(wordCounts: List<Pair<String, Int>>) {
        val columnChartView: AnyChartView? = binding?.anyChartViewColumn
        val columnData: MutableList<DataEntry> = ArrayList()

        for ((date, count) in wordCounts) {
            columnData.add(ValueDataEntry(date, count))
        }

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

        columnChartView?.setChart(cartesian)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
