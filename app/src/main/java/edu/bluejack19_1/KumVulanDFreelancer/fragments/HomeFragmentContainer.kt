package edu.bluejack19_1.KumVulanDFreelancer.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import edu.bluejack19_1.KumVulanDFreelancer.R
import edu.bluejack19_1.KumVulanDFreelancer.System
import edu.bluejack19_1.KumVulanDFreelancer.adapters.HomeFragmentAdapter
import edu.bluejack19_1.KumVulanDFreelancer.homeFragmentContainer
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home_container.*

class HomeFragmentContainer : Fragment() {

    lateinit var adapter: FragmentStatePagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_container, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeFragmentContainer = this

        adapter = HomeFragmentAdapter(fragmentManager!!)
        viewPager.adapter = adapter
    }

    override fun onResume() {
        super.onResume()

        if (System.last_activity != System.HOME_FRAGMENT && System.last_activity != System.JOB_DETAIL_ACTIVITY_PRE && System.last_activity != System.JOB_DETAIL_ACTIVITY_POST) {
            adapter = HomeFragmentAdapter(fragmentManager!!)
            viewPager.adapter = adapter
        }
        System.last_activity = System.HOME_FRAGMENT_CONTAINER
    }
}
