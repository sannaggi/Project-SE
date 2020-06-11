package edu.bluejack19_1.KumVulanDFreelancer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import edu.bluejack19_1.KumVulanDFreelancer.fragments.HomeFragment
import kotlinx.android.synthetic.main.activity_job_detail_post.*

class JobDetailActivityPost : AppCompatActivity() {

    lateinit var jobID: String
    lateinit var otherPartyEmail: String
    lateinit var role: String
    lateinit var data: HashMap<String, Any>
    lateinit var otherPartyData: HashMap<String, Any>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_detail_post)

        fetchIntentData()
        getJobData()
    }

    override fun onResume() {
        super.onResume()

        System.last_activity = System.JOB_DETAIL_ACTIVITY_POST
    }

    private fun fetchIntentData() {
        jobID = intent.extras!!.get(TakenJob.ID).toString()
        otherPartyEmail = intent.extras!!.get(TakenJob.OTHER_PARTY_EMAIL).toString()
        role = intent.extras!!.get(TakenJob.YOUR_ROLE).toString()
    }

    private fun getJobData() {
        firebaseDatabase()
            .collection("jobs")
            .document(jobID)
            .get()
            .addOnSuccessListener {
                data = it.data as HashMap<String, Any>

                firebaseDatabase()
                    .collection("users")
                    .document(otherPartyEmail)
                    .get()
                    .addOnSuccessListener { it2 ->
                        otherPartyData = it2.data as HashMap<String, Any>
                        launchActivity()
                    }
            }
    }

    private fun launchActivity() {
        showDatas()
        initializeFinishButton()
        initializeClientImageOnClick()
        initializeFreelancerImageOnClick()
    }

    private fun initializeClientImageOnClick() {
        imgClient.setOnClickListener {
            val intent = Intent(this, AccountActivityClient::class.java)
            intent.putExtra("ID", data.get(TakenJob.CLIENT).toString())

            startActivity(intent)
        }
    }

    private fun initializeFreelancerImageOnClick() {
        imgFreelancer.setOnClickListener {
            val intent = Intent(this, AccountActivityFreelancer::class.java)
            intent.putExtra("ID", data.get(TakenJob.FREELANCER).toString())

            startActivity(intent)
        }
    }

    private fun initializeFinishButton() {
        if (HomeFragment.role == TakenJob.CLIENT && data.get(TakenJob.STATUS).toString() == TakenJob.WAITING_FREELANCER) {
            disableButton()
            return
        }

        if (HomeFragment.role == TakenJob.FREELANCER && data.get(TakenJob.STATUS).toString() == TakenJob.WAITING_CLIENT) {
            disableButton()
            return
        }

        btnFinish.setOnClickListener {
            confirmationPopupFinish()
        }

        btnCancel.setOnClickListener {
            confirmationPopupCancel()
        }
    }

    private fun confirmationPopupFinish() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmation")
        builder.setMessage("Do you really want to finish this job?")
            .setCancelable(true)
            .setPositiveButton("YES") { _, _ ->
                updateJobStatus()
            }
            .setNegativeButton("NO"){_, _ ->  }
        builder.create().show()
    }

    private fun confirmationPopupCancel() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmation")
        builder.setMessage("Do you really want to cancel this job?")
            .setCancelable(true)
            .setPositiveButton("YES") { _, _ ->
                cancelJob()
            }
            .setNegativeButton("NO"){_, _ ->  }
        builder.create().show()
    }

    private fun cancelJob() {
        data.set(TakenJob.STATUS, "Canceled")

        firebaseDatabase()
            .collection("jobs")
            .document(jobID)
            .delete()

        firebaseDatabase()
            .collection("finished_jobs")
            .document(jobID)
            .set(data)

        finish()
    }

    private fun updateJobStatus() {
        lateinit var newStatus: String

        if (role == TakenJob.CLIENT) {
            when (data.get(TakenJob.STATUS).toString()) {
                TakenJob.ON_GOING -> {
                    newStatus = TakenJob.WAITING_FREELANCER
                }
                TakenJob.WAITING_CLIENT -> {
                    newStatus = TakenJob.FINISHED
                }
            }
        } else {
            when (data.get(TakenJob.STATUS).toString()) {
                TakenJob.ON_GOING -> {
                    newStatus = TakenJob.WAITING_CLIENT
                }
                TakenJob.WAITING_FREELANCER -> {
                    newStatus = TakenJob.FINISHED
                }
            }
        }

        data.set(TakenJob.STATUS, newStatus)

        if (newStatus == TakenJob.FINISHED) {
            finishJob()
            finish()
        } else {
            updateData()
        }
        txtStatus.text = newStatus
        txtStatus.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
        disableButton()
    }

    private fun updateData() {
        firebaseDatabase()
            .collection("jobs")
            .document(jobID)
            .update(data)
    }

    private fun finishJob() {
        firebaseDatabase()
            .collection("jobs")
            .document(jobID)
            .delete()

        firebaseDatabase()
            .collection("finished_jobs")
            .document(jobID)
            .set(data)
    }

    private fun disableButton() {
        txtStatus.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
        btnFinish.isEnabled = false
        btnFinish.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
        btnFinish.text = getString(R.string.waiting_confirmation)
    }

    private fun showDatas() {
        progress_circular.visibility = View.GONE
        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.NO_GRAVITY
        container.layoutParams = params
        jobDetail.visibility = View.VISIBLE

        assignDatas()
    }

    private fun assignDatas() {
        txtJobName.text = data.get(TakenJob.NAME).toString()
        txtPrice.text = "Rp. ${data.get(TakenJob.EST_PRICE).toString()}"
        txtDeadline.text = data.get(TakenJob.DEADLINE).toString()
        txtDescription.text = data.get(TakenJob.DESCRIPTION).toString()
        txtStatus.text = data.get(TakenJob.STATUS).toString()

        loadUserDatas()
    }

    private fun loadUserDatas() {
        when (role) {
            TakenJob.CLIENT -> {
                txtClientName.text = User.getName()
                txtFreelancerName.text = otherPartyData.get(User.NAME).toString()

                loadImage(User.getProfileImage(), imgClient)
                loadImage(User.getProfileImagePath(otherPartyData.get(User.PROFILE_IMAGE).toString()), imgFreelancer)
            }
            TakenJob.FREELANCER -> {
                txtClientName.text = otherPartyData.get(User.NAME).toString()
                txtFreelancerName.text = User.getName()

                loadImage(User.getProfileImage(), imgFreelancer)
                loadImage(User.getProfileImagePath(otherPartyData.get(User.PROFILE_IMAGE).toString()), imgClient)
            }
        }
    }

    private fun loadImage(path: String, img: CircleImageView?) {
        firebaseStorageReference()
            .child(path)
            .downloadUrl
            .addOnSuccessListener{uri ->
                if (img != null && this != null) {
                    Glide.with(this)
                        .load(uri)
                        .into(img)
                }
            }
    }
}
