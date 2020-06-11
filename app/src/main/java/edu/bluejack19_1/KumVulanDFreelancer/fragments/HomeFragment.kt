package edu.bluejack19_1.KumVulanDFreelancer.fragments

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import edu.bluejack19_1.KumVulanDFreelancer.*
import edu.bluejack19_1.KumVulanDFreelancer.adapters.TakenJobAdapter
import edu.bluejack19_1.KumVulanDFreelancer.firebase.Job
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home_container.*
import kotlinx.android.synthetic.main.redirect_to_jobs_button.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class HomeFragment(main: MainActivity): Fragment(), OnItemSelectedListener {
    val main = main
    var firstLoad = true

    companion object {
        var role: String = TakenJob.CLIENT
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onResume() {
        super.onResume()
        if (!firstLoad && System.last_activity != System.HISTORY_FRAGMENT) fetchData()
        if (System.last_activity != System.LOGIN_REGISTER_ACTIVITY && System.last_activity != System.REGISTER_FREELANCER_ACTIVITY && System.last_activity != System.EDIT_PROFILE_ACTIVITY) System.last_activity = System.HOME_FRAGMENT
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeSpinner()
        initializeRedirectButton()
        initializeHistoryButton()
        initializeAddJobButton()
        initializeNotification()
    }

    private fun initializeNotification() {

    }

    private fun initializeAddJobButton() {
        btnNewJob.setOnClickListener{
            startActivity(Intent(this.context, AddNewJobActivity::class.java))
        }
    }

    private fun initializeHistoryButton() {
        btnHistory.setOnClickListener {
            homeFragmentContainer.viewPager.currentItem = 1
        }
    }

    private fun fetchData() {
        firstLoad = false
        Log.d("firebase", role)

        emptyJobMessageContainer.visibility = View.GONE
        progress_circular.visibility = View.VISIBLE
        onGoingJobsContainer.visibility = View.GONE
        btnNewJob.visibility = View.GONE

        val jobs = ArrayList<HashMap<String, Any>>()
        firebaseDatabase()
            .collection("jobs")
            .whereEqualTo(role, firebaseAuth().currentUser!!.email)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val data = document.data as HashMap<String, Any>
                    data.set("id", document.id)
                    jobs.add(data)
                }
                updateJobsUI(jobs, role)
            }
    }

    private fun updateJobsUI(jobs: ArrayList<HashMap<String, Any>>, role: String) {
        if (jobsContainer == null) return

        progress_circular.visibility = View.GONE

        if (role == TakenJob.CLIENT)
            btnNewJob.visibility = View.VISIBLE

        if (jobs.isEmpty()) {
            if (role == TakenJob.FREELANCER)
                emptyJobMessageContainer.visibility = View.VISIBLE

            return
        }

        onGoingJobsContainer.visibility = View.VISIBLE
        var jobs = getTakenJobsList(jobs)
        jobs = ArrayList(sort(jobs))
        val adapter = TakenJobAdapter(this.context!!, jobs)
//        setNotifications(jobs)
        listTakenJobs.adapter = adapter

        var size = jobs.size

        var totalHeight = 0
        for(i in 0 until size) {
            val item = adapter.getView(i, null, listTakenJobs)
            item.measure(0, 0)
            totalHeight += item.measuredHeight
        }

        val params = listTakenJobs.layoutParams
        params.height = totalHeight + (listTakenJobs.dividerHeight * (listTakenJobs.count - 1))
        listTakenJobs.layoutParams = params
    }

//    private fun setNotifications(jobs: ArrayList<TakenJob>) {
//        for(job in jobs) {
//            val calendar = Calendar.getInstance()
//
//            val dateParts = job.deadline.split("/")
//            val date = dateParts[0].toInt()
//            val month = dateParts[1].toInt() - 1
//            val year = dateParts[2].toInt()
//            val currDate = calendar.get(Calendar.DAY_OF_MONTH)
//            val currYear = calendar.get(Calendar.YEAR)
//            val currMonth = calendar.get(Calendar.MONTH)
//            val sdf = SimpleDateFormat("dd/MM/yyyy")
//            val today = sdf.format(calendar.time)
//
//            Log.d("testt", "$today today")
//            Log.d("testt", job.deadline + " deadline")
//            Log.d("testt", "$date, $month, $year aa")
//            Log.d("testt", "$currDate, $currMonth, $currYear bb")
//
//            val limit = Calendar.getInstance()
//            limit.set(year, month, date + 1)
//            calendar.set(year, month, date - 2)
//            Log.d("testt", calendar.get(Calendar.DAY_OF_MONTH).toString())
//
//            if (Calendar.getInstance().after(calendar) && Calendar.getInstance().before(limit)) {
//                val intent = Intent(context, MainActivity::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//
//                val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
//
//                val nbuilder = NotificationCompat.Builder(context!!, "NOTIF_CHANNEL")
//                nbuilder
//                    .setSmallIcon(R.drawable.ic_notifications_black_24dp)
//                    .setContentTitle("Taken Job Notification")
//                    .setContentText("Your job, " + job.name + " will meet its deadline soon")
//                    .setWhen(calendar.timeInMillis)
//                    .setContentIntent(pendingIntent)
//
//                val manager = mainActivityInstance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                manager.notify(ascii(job.id), nbuilder.build())
//            }
//        }
//    }
//
//    private fun ascii(str: String): Int {
//        var asc = 0
//        for (c in str) {
//            asc += c.toByte()
//        }
//
//        return asc
//    }

    private fun sort(jobs: List<TakenJob>): List<TakenJob> {
        var jobs2 = jobs

        jobs2 = jobs2.sortedBy {
            SimpleDateFormat("dd/MM/yyyy").parse(it.deadline)
        }
        return jobs2
    }

    private fun setNotifications(jobs: ArrayList<TakenJob>) {
        for(job in jobs) {
            val calendar = Calendar.getInstance()

            val dateParts = job.deadline.split("/")
            val date = dateParts[0].toInt()
            val month = dateParts[1].toInt() - 1
            val year = dateParts[2].toInt()
            val currDate = calendar.get(Calendar.DAY_OF_MONTH)
            val currYear = calendar.get(Calendar.YEAR)
            val currMonth = calendar.get(Calendar.MONTH)
            val sdf = SimpleDateFormat("dd/MM/yyyy")
            val today = sdf.format(calendar.time)

            Log.d("testt", "$today today")
            Log.d("testt", job.deadline + " deadline")
            Log.d("testt", "$date, $month, $year aa")
            Log.d("testt", "$currDate, $currMonth, $currYear bb")

            val limit = Calendar.getInstance()
            limit.set(year, month, date + 1)
            calendar.set(year, month, date - 2)
            Log.d("testt", calendar.get(Calendar.DAY_OF_MONTH).toString())

            if (Calendar.getInstance().after(calendar) && Calendar.getInstance().before(limit)) {
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

                val nbuilder = NotificationCompat.Builder(context!!, "NOTIF_CHANNEL")
                nbuilder
                    .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                    .setContentTitle("Taken Job Notification")
                    .setContentText("Your job, " + job.name + " will meet its deadline soon")
                    .setWhen(calendar.timeInMillis)
                    .setContentIntent(pendingIntent)

                val manager = mainActivityInstance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(ascii(job.id), nbuilder.build())
            }
        }
    }

    private fun ascii(str: String): Int {
        var asc = 0
        for (c in str) {
            asc += c.toByte()
        }

        return asc
    }

    private fun getTakenJobsList(list: ArrayList<HashMap<String, Any>>): ArrayList<TakenJob> {
        val jobs = ArrayList<TakenJob>()
        val size = list.size

        for (i in 0 until size) {
            val name = list[i].get(TakenJob.NAME).toString()
            val client = list[i].get(TakenJob.CLIENT).toString()
            val deadline = list[i].get(TakenJob.DEADLINE).toString()
            val description = list[i].get(TakenJob.DESCRIPTION).toString()
            val est_price = list[i].get(TakenJob.EST_PRICE).toString().toInt()
            val freelancer = list[i].get(TakenJob.FREELANCER).toString()
            val status = list[i].get(TakenJob.STATUS).toString()
            val id = list[i].get(TakenJob.ID).toString()

            jobs.add(TakenJob(name, client, deadline, description, est_price, freelancer, status, id))
        }

        return jobs
    }

    private fun initializeSpinner() {
        spinnerRole.onItemSelectedListener = this
        spinnerRole.setSelection(0)

        if (User.getRole() == User.CLIENT) {
            spinnerRole.visibility = View.GONE
            return
        }

        ArrayAdapter.createFromResource(
            this.context!!,
            R.array.role_array,
            android.R.layout.simple_spinner_item
        ) .also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerRole.adapter = adapter
        }

        if (User.getRole() == User.FREELANCER) {
            spinnerRole.setSelection(1)
        }

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        role = if (position == 0) TakenJob.CLIENT else TakenJob.FREELANCER
        fetchData()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    private fun initializeRedirectButton() {
        btnRedirectJobs.setOnClickListener {
            main.jumpToJobsFragment()
        }
    }

}