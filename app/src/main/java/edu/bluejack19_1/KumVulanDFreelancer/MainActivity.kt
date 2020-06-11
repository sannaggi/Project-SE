package edu.bluejack19_1.KumVulanDFreelancer

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.example.kumvulandfreelancer.Fragments.JobsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.bluejack19_1.KumVulanDFreelancer.Fragments.AccountFragmentClient
import edu.bluejack19_1.KumVulanDFreelancer.Fragments.AccountFragmentFreelancer
import edu.bluejack19_1.KumVulanDFreelancer.Fragments.HomeFragmentGuest
import edu.bluejack19_1.KumVulanDFreelancer.adapters.FinishedJobAdapter
import edu.bluejack19_1.KumVulanDFreelancer.fragments.AccountFragmentGuest
import edu.bluejack19_1.KumVulanDFreelancer.fragments.HistoryFragment
import edu.bluejack19_1.KumVulanDFreelancer.fragments.HomeFragmentContainer
import edu.bluejack19_1.KumVulanDFreelancer.fragments.PeopleFragment
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var homeFragment: Fragment
    private lateinit var jobsFragment: JobsFragment
    private lateinit var accountFragment: Fragment
    private var chatFragment: Fragment? = null

    fun jumpToJobsFragment() {
        addFragment(jobsFragment)
        bottom_navigation.selectedItemId = R.id.nav_jobs
    }

    fun logout() {
        accountFragment = AccountFragmentGuest(this)
        homeFragment = HomeFragmentGuest(this)
        chatFragment = accountFragment;
        addFragment(accountFragment)
    }

    fun loginFromAccount() {
        accountFragment = if (User.getRole() == User.CLIENT) {
            AccountFragmentClient(this)
        } else {
            AccountFragmentFreelancer(this)
        }
        FinishedJobAdapter.takenJobQuery = FinishedJobAdapter.documentRef.whereEqualTo("freelancer", User.getEmail())
        FinishedJobAdapter.postedJobQuery = FinishedJobAdapter.documentRef.whereEqualTo("client", User.getEmail())

        chatFragment = PeopleFragment(this)
        homeFragment = HomeFragmentContainer()
        addFragment(accountFragment)
    }

    fun loginFromHome() {
        accountFragment = if (User.getRole() == User.CLIENT) {
            AccountFragmentClient(this)
        } else {
            AccountFragmentFreelancer(this)
        }
        FinishedJobAdapter.takenJobQuery = FinishedJobAdapter.documentRef.whereEqualTo("freelancer", User.getEmail())
        FinishedJobAdapter.postedJobQuery = FinishedJobAdapter.documentRef.whereEqualTo("client", User.getEmail())

        chatFragment = PeopleFragment(this)
        homeFragment = HomeFragmentContainer()
        addFragment(homeFragment)
    }

    private fun addFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim)
            .replace(R.id.fragment_container, fragment, fragment.javaClass.simpleName)
            .commit()
    }

    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when(item.itemId) {
            R.id.nav_home -> {
                addFragment(homeFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_jobs -> {
                addFragment(jobsFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_account -> {
                if(!::accountFragment.isInitialized)
                    return@OnNavigationItemSelectedListener true
                addFragment(accountFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_chat ->{

                addFragment(chatFragment!!)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainActivityInstance = this

        bottom_navigation.setOnNavigationItemSelectedListener(navListener)

        if(User.data!!.isNotEmpty()) {
            accountFragment =  if (User.getRole() == User.CLIENT) {
                AccountFragmentClient(this)
            } else {
                AccountFragmentFreelancer(this)
            }

            FinishedJobAdapter.takenJobQuery = FinishedJobAdapter.documentRef.whereEqualTo("freelancer", User.getEmail())
            FinishedJobAdapter.postedJobQuery = FinishedJobAdapter.documentRef.whereEqualTo("client", User.getEmail())

            chatFragment = PeopleFragment(this)
            homeFragment = HomeFragmentContainer()
        } else {
            accountFragment = AccountFragmentGuest(this)
            homeFragment = HomeFragmentGuest(this)
            chatFragment = accountFragment;
        }

        jobsFragment = JobsFragment()
        addFragment(homeFragment)

        if (firebaseAuth().currentUser == null) return
        val jobs = ArrayList<HashMap<String, Any>>()
        firebaseDatabase()
            .collection("jobs")
            .whereEqualTo("client", firebaseAuth().currentUser!!.email)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val data = document.data as HashMap<String, Any>
                    data.set("id", document.id)
                    jobs.add(data)
                }
                firebaseDatabase()
                    .collection("jobs")
                    .whereEqualTo("freelancer", firebaseAuth().currentUser!!.email)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            val data = document.data as HashMap<String, Any>
                            data.set("id", document.id)
                            jobs.add(data)
                        }
                        setNotifications(getTakenJobsList(jobs))
                    }
            }
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
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

                val nbuilder = NotificationCompat.Builder(this!!, "NOTIF_CHANNEL")
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


}
