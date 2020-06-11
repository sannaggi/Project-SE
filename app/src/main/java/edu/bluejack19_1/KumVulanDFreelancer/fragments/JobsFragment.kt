package com.example.kumvulandfreelancer.Fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import edu.bluejack19_1.KumVulanDFreelancer.AccountActivityClient
import edu.bluejack19_1.KumVulanDFreelancer.R
import edu.bluejack19_1.KumVulanDFreelancer.System
import edu.bluejack19_1.KumVulanDFreelancer.adapters.JobAdapter
import edu.bluejack19_1.KumVulanDFreelancer.firebase.FirebaseUtil
import edu.bluejack19_1.KumVulanDFreelancer.firebase.Job
import edu.bluejack19_1.KumVulanDFreelancer.firebaseDatabase
import edu.bluejack19_1.KumVulanDFreelancer.recycleView.item.JobItem
import kotlinx.android.synthetic.main.fragment_jobs.*
import kotlinx.android.synthetic.main.item_job.*
import kotlinx.android.synthetic.main.item_job.view.*
import java.util.*

class JobsFragment : Fragment() {

    private lateinit var jobListenerRegistration: ListenerRegistration
    private var shouldInitRecyclerView = true
    private lateinit var jobSection: Section
    private var jobs: List<Job>? = null
    private lateinit var jobsFiltered : List<Job>
    private lateinit var items: List<Item>
    private var currentSortCategory : String = "None"
    private var currentJobCategory : String = "None"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        jobListenerRegistration = JobAdapter.initializeJobListener(this.activity!!, this::updateRecyclerView)

        return inflater.inflate(R.layout.fragment_jobs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        System.last_activity = System.JOB_FRAGMENT
        initializeSortSpinner()
        initializeCategorySpinner()
        initializeSearchBox()
    }

    private fun jobCategoryFilter(finishedJobs: List<Job>){
        var finishedJobs = finishedJobs
        var toRet = mutableListOf<Job>()
        finishedJobs.forEach {
            if(it.category == currentJobCategory){
                toRet.add(it)
            }
            if(currentJobCategory == "None") toRet.add(it)
        }
        jobsFiltered = toRet
        sort(toRet)
    }


    private fun initializeCategorySpinner(){
        job_category_spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(this@JobsFragment.jobs == null) return
                var finishedJobs = this@JobsFragment.jobs
                this@JobsFragment.currentJobCategory = job_category_spinner.getItemAtPosition(position).toString()
                jobCategoryFilter(finishedJobs!!)
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("JobsFragment", "Kontolmu di destroy")
        FirebaseUtil.removeListener(jobListenerRegistration)
        shouldInitRecyclerView = true
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("JobsFragment", "Kontolmu di detach")
        FirebaseUtil.removeListener(jobListenerRegistration)
        shouldInitRecyclerView = true
    }

    private fun initializeSearchBox(){
        job_search_box.addTextChangedListener {
            var name = job_search_box.text.toString().toLowerCase(Locale.getDefault())
            var finishedJobs = this@JobsFragment.jobs
            var forTransfer = mutableListOf<Job>()
            finishedJobs!!.forEach {
                if(it.name.toLowerCase(Locale.getDefault()).contains(name)){
                    forTransfer.add(it)
                }
            }
            Log.d("JobsFragment", "${name}, ${finishedJobs?.size}, ${forTransfer.size}")

            sort(forTransfer)
        }
    }

    private fun initializeSortSpinner(){
        job_sort_spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long){
                if(this@JobsFragment.jobs == null) return
                lateinit var finishedJobs : List<Job>
                if(currentJobCategory == "None") finishedJobs = this@JobsFragment.jobs!!
                else finishedJobs = this@JobsFragment.jobsFiltered
                currentSortCategory = job_sort_spinner.getItemAtPosition(position).toString()
                sort(finishedJobs!!)
            }

            override fun onNothingSelected(parent: AdapterView<*>){

            }
        }
    }

    private fun sort(finishedJobs: List<Job>){
        var finishedJobs = finishedJobs
        if(currentSortCategory.equals("Deadline")){
            finishedJobs = finishedJobs?.sortedByDescending {
                it.originalDeadline
            }
        }
        else if(currentSortCategory.equals("Price")){
            finishedJobs = finishedJobs?.sortedByDescending{
                it.originalPrice
            }
        }
        convertToItems(finishedJobs!!)
        jobSection.update(items)
    }

    private fun convertToItems(jobs: List<Job>){
        var items = mutableListOf<Item>()
        if (this.activity == null) return
        jobs.forEach{
            items.add(JobItem(it, this.activity!!))
        }

        this.items = items
    }

    private fun updateRecyclerView(jobs: List<Job>){
        this.jobs = jobs
        if (this@JobsFragment.context == null) return
        convertToItems(jobs)
        fun init(){
            job_recycler_view.apply {
                layoutManager = LinearLayoutManager(this@JobsFragment.context)
                adapter = GroupAdapter<ViewHolder>().apply {
                    jobSection = Section(items)
                    add(jobSection)
                    setOnItemClickListener(onItemClick)
                }
            }
            shouldInitRecyclerView = false
        }
        fun updateItems(){

        }

        if(shouldInitRecyclerView) init()
        else updateItems()
    }

    private val onItemClick = OnItemClickListener{ item, view ->
        if(item is JobItem){
            view.job_client.setOnClickListener{
                var intent = Intent(this@JobsFragment.activity!!, AccountActivityClient::class.java)
                intent.putExtra("ID", item.job.client)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        shouldInitRecyclerView = true
        jobListenerRegistration = JobAdapter.initializeJobListener(this.activity!!, this::updateRecyclerView)
        System.last_activity = System.JOB_FRAGMENT
    }
}