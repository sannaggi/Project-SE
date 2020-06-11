package edu.bluejack19_1.KumVulanDFreelancer.adapters

import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import edu.bluejack19_1.KumVulanDFreelancer.fragments.HistoryFragment
import edu.bluejack19_1.KumVulanDFreelancer.fragments.HomeFragment
import edu.bluejack19_1.KumVulanDFreelancer.mainActivityInstance

class HomeFragmentAdapter(fragmentManager: FragmentManager): FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        if (position == 0) return HomeFragment(mainActivityInstance)
        return HistoryFragment(mainActivityInstance)
    }

    override fun getCount(): Int {
        return 2
    }

}