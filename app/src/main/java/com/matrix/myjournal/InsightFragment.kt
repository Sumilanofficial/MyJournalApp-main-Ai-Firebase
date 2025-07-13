package com.matrix.myjournal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.matrix.myjournal.databinding.FragmentInsightBinding
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator

class InsightFragment : Fragment() {

    private var binding: FragmentInsightBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInsightBinding.inflate(inflater, container, false)

        // Show bottom nav bar
        (activity as? MainActivity)?.findViewById<BottomNavigationView>(R.id.bottomNavigation)?.visibility = View.VISIBLE

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up ViewPager2 for insights
        val pagerAdapter = ChartPagerAdapter(this)
        binding?.viewPager?.adapter = pagerAdapter

        // Set up dots indicator
        val pageIndicator: WormDotsIndicator? = binding?.pageIndicator
        binding?.viewPager?.let { pageIndicator?.setViewPager2(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
