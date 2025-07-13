package com.matrix.myjournal

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ChartPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 2 // Number of tabs
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ColumnChartFragment()
            1 -> PieChartFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
