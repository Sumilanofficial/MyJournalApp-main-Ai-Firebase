package com.matrix.myjournal

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.enums.TooltipPositionMode
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.matrix.myjournal.DataClasses.JournalEntry
import com.matrix.myjournal.databinding.FragmentPieChartBinding

class PieChartFragment : Fragment() {

    private var binding: FragmentPieChartBinding? = null
    private lateinit var pieChartView: AnyChartView
    private val TAG = "PieChartFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPieChartBinding.inflate(inflater, container, false)
        pieChartView = binding!!.anyChartViewPie  // Initialize here
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchAndDisplayData()
    }

    private fun fetchAndDisplayData() {
        Firebase.firestore.collection("JournalEntries")
            .get()
            .addOnSuccessListener { result ->
                var totalWords = 0
                var totalImages = 0

                for (document in result.documents) {
                    val entry = document.toObject(JournalEntry::class.java)
                    if (entry != null) {
                        totalWords += entry.combinedResponse?.split("\\s+".toRegex())?.size ?: 0
                        totalImages += entry.imageUrls?.size ?: 0
                    }
                }

                Log.d(TAG, "Total Words: $totalWords, Total Images: $totalImages")
                displayChart(totalWords, totalImages)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching Firestore data", e)
            }
    }

    private fun displayChart(totalWords: Int, totalImages: Int) {
        val pieData: MutableList<DataEntry> = ArrayList()
        pieData.add(ValueDataEntry("Total Words", totalWords))
        pieData.add(ValueDataEntry("Total Images", totalImages))

        val pie = AnyChart.pie()
        pie.data(pieData)
        pie.title("Total Words and Images")
        pie.labels().position("outside")
        pie.legend().title().enabled(true)
        pie.legend().title().text("Categories").padding(0.0, 0.0, 10.0, 0.0)
        pie.legend().position("bottom")
        pie.tooltip().positionMode(TooltipPositionMode.POINT)

        pieChartView.setChart(pie)

        Log.d(TAG, "Pie chart displayed successfully")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
