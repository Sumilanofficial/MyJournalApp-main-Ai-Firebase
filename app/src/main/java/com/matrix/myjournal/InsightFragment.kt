package com.matrix.myjournal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.matrix.myjournal.databinding.FragmentInsightBinding
import com.matrix.myjournal.questionresdatabase.QuestionResDatabase
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator

class InsightFragment : Fragment() {

    private var binding: FragmentInsightBinding? = null
    private var questionResDatabase: QuestionResDatabase? = null

    private var totalJournals: Int = 0  // Variable to store the total number of journals

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInsightBinding.inflate(layoutInflater)

        // Ensure the bottom navigation is visible when the fragment is created
        (activity as? MainActivity)?.findViewById<BottomNavigationView>(R.id.bottomNavigation)?.visibility = View.VISIBLE

        // Initialize the database


        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe the total number of journals from the DAO
        questionResDatabase?.questionResDao()?.getTotalJournalEntries()?.observe(viewLifecycleOwner, Observer { count ->
            totalJournals = count
            // Optionally, display the total number of journals in the UI
//            binding?.txtjournal?.text = totalJournals.toString()
        })

        // Setup ViewPager2
        val pagerAdapter = ChartPagerAdapter(this)
        binding?.viewPager?.adapter = pagerAdapter

        // Setup page indicator
        val pageIndicator: WormDotsIndicator? = binding?.pageIndicator
        binding?.viewPager?.let { pageIndicator?.setViewPager2(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null  // Prevent memory leaks
    }
}
