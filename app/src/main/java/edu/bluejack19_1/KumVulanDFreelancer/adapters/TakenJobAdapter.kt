package edu.bluejack19_1.KumVulanDFreelancer.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import edu.bluejack19_1.KumVulanDFreelancer.*
import edu.bluejack19_1.KumVulanDFreelancer.fragments.HomeFragment
import kotlinx.android.synthetic.main.taken_job.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class TakenJobAdapter(private val context: Context, private val jobs: ArrayList<TakenJob>): BaseAdapter(){

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val WAITING_CONFIMATION = "Waiting confirmation"

    override fun getCount(): Int {
        return jobs.size
    }

    override fun getItem(position: Int): Any {
        return jobs[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = inflater.inflate(R.layout.taken_job, parent, false)
        val job = getItem(position) as TakenJob

        loadValues(row as LinearLayout, job)

        row.setOnClickListener {
            if (job.status == TakenJob.ISSUED) {
                loadJobDetailPre(job)
            } else {
                loadJobDetailPost(job)
            }
        }

        if (HomeFragment.role == TakenJob.CLIENT && job.status == TakenJob.WAITING_FREELANCER) {
            disableButton(row)
            return row
        }

        if (HomeFragment.role == TakenJob.FREELANCER && job.status == TakenJob.WAITING_CLIENT) {
            disableButton(row)
            return row
        }

        row.txtStatus.setTextColor(ContextCompat.getColor(context, R.color.colorBlue))
        if (job.status != TakenJob.ON_GOING) {
            row.btnFinishJob.text = "Confirm finished"
        }

        row.btnFinishJob.setOnClickListener {
            confirmationPopUp(job)
        }

        return row
    }

    private fun loadJobDetailPre(job: TakenJob) {
        val intent = Intent(context, JobDetailActivityPre::class.java)
        intent.putExtra(TakenJob.ID, job.id)
        ContextCompat.startActivity(context, intent, null)
    }

    private fun loadJobDetailPost(job: TakenJob) {
        val intent = Intent(context, JobDetailActivityPost::class.java)
        lateinit var otherPartyEmail: String
        when (HomeFragment.role) {
            TakenJob.CLIENT -> {
                otherPartyEmail = job.freelancer
            }
            TakenJob.FREELANCER -> {
                otherPartyEmail = job.client
            }
        }
        intent.putExtra(TakenJob.YOUR_ROLE, HomeFragment.role)
        intent.putExtra(TakenJob.OTHER_PARTY_EMAIL, otherPartyEmail)
        intent.putExtra(TakenJob.ID, job.id)

        ContextCompat.startActivity(context, intent, null)
    }

    private fun disableButton(row: View) {
        row.txtStatus.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
        row.btnFinishJob.isEnabled = false
        row.btnFinishJob.background = ContextCompat.getDrawable(context, R.drawable.soft_border_gray)
        row.btnFinishJob.text = WAITING_CONFIMATION
    }


    private fun confirmationPopUp(job: TakenJob) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirmation")
        builder.setMessage("Do you really want to finish this job?")
            .setCancelable(true)
            .setPositiveButton("YES") { _, _ ->
                updateJobStatus(job)
            }
            .setNegativeButton("NO"){_, _ ->  }
        builder.create().show()
    }

    private fun updateJobStatus(job: TakenJob) {
        lateinit var newStatus: String

        if (HomeFragment.role == TakenJob.CLIENT) {
            when (job.status) {
                TakenJob.ON_GOING -> {
                    newStatus = TakenJob.WAITING_FREELANCER
                }
                TakenJob.WAITING_CLIENT -> {
                    newStatus = TakenJob.FINISHED
                }
            }
        } else {
            when (job.status) {
                TakenJob.ON_GOING -> {
                    newStatus = TakenJob.WAITING_CLIENT
                }
                TakenJob.WAITING_FREELANCER -> {
                    newStatus = TakenJob.FINISHED
                }
            }
        }

        job.status = newStatus

        if (newStatus == TakenJob.FINISHED) {
            finishJob(job)
            jobs.remove(job)
        } else {
            updateData(job)
        }

        notifyDataSetChanged()
    }

    private fun updateData(job: TakenJob) {
        firebaseDatabase()
            .collection("jobs")
            .document(job.id)
            .update(generateUpdatedData(job))
    }

    private fun finishJob(job: TakenJob) {
        firebaseDatabase()
            .collection("jobs")
            .document(job.id)
            .delete()

        firebaseDatabase()
            .collection("finished_jobs")
            .document(job.id)
            .set(generateDuplicateData(job))
    }

    private fun generateUpdatedData(job: TakenJob): HashMap<String, Any> {
        val data = HashMap<String, Any>()
        data.set(TakenJob.STATUS, job.status)

        return data
    }

    private fun generateDuplicateData(job: TakenJob): HashMap<String, Any> {
        val data = HashMap<String, Any>()
        data.set(TakenJob.NAME, job.name)
        data.set(TakenJob.FREELANCER, job.freelancer)
        data.set(TakenJob.CLIENT, job.client)
        data.set(TakenJob.EST_PRICE, job.est_price)
        data.set(TakenJob.DEADLINE, job.deadline)
        data.set(TakenJob.STATUS, job.status)
        data.set(TakenJob.DESCRIPTION, job.description)

        return data
    }

    private fun loadValues(row: LinearLayout, job: TakenJob) {
        row.txtJobName.text = job.name
        row.txtDeadline.text = job.deadline
        row.txtStatus.text = job.status

        if (job.status == TakenJob.ISSUED) {
            row.otherPartyContainer.visibility = View.GONE
            row.imgOtherParty.visibility = View.GONE
            row.btnFinishJob.visibility = View.GONE
            return
        }
        loadOtherPartyDatas(row, job)
    }

    private fun loadOtherPartyDatas(row: View, job: TakenJob) {
        val otherParty = if (HomeFragment.role == "freelancer") job.client else job.freelancer
        firebaseDatabase()
            .collection("users")
            .document(otherParty)
            .get()
            .addOnSuccessListener {
                if (row.txtOtherParty != null) {
                    val data = it.data as HashMap<String, Any>
                    row.txtOtherParty.text = data.get(User.NAME).toString()

                    loadImage(data.get(User.PROFILE_IMAGE).toString(), row)
                }
            }
    }

    private fun loadImage(imagePath: String, row: View) {
        firebaseStorageReference()
            .child(User.getProfileImagePath(imagePath))
            .downloadUrl
            .addOnSuccessListener{uri ->
                if (row.imgOtherParty != null && this != null && this.context != null && row != null) {
                    Glide.with(this.context)
                        .load(uri)
                        .into(row.imgOtherParty)
                }
            }
            .addOnFailureListener {
                Log.d("firebase", it.toString())
            }
    }
}