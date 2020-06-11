package edu.bluejack19_1.KumVulanDFreelancer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_job_detail_pre.*
import kotlinx.android.synthetic.main.applicant.view.*

class JobDetailActivityPre : AppCompatActivity() {

    lateinit var jobID: String
    lateinit var jobData: HashMap<String, Any>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_detail_pre)

        fetchIntentData()
        getJobData()
    }

    override fun onResume() {
        super.onResume()
        System.last_activity = System.JOB_DETAIL_ACTIVITY_PRE
    }

    private fun fetchIntentData() {
        jobID = intent.extras!!.get(TakenJob.ID).toString()
    }

    private fun getJobData() {
        firebaseDatabase()
            .collection("jobs")
            .document(jobID)
            .get()
            .addOnSuccessListener {
                jobData = it.data as HashMap<String, Any>

                firebaseDatabase()
                    .collection("users")
                    .document(jobData.get(TakenJob.CLIENT).toString())
                    .get()
                    .addOnSuccessListener { it2 ->
                        loadClientData(it2.data as HashMap<String, Any>)
                        launchActivity()
                    }
            }
    }

    private fun loadClientData(clientData: HashMap<String, Any>) {
        txtClientName.text = clientData.get(User.NAME).toString()
        loadImage(User.getProfileImagePath(clientData.get(User.PROFILE_IMAGE).toString()), imgClient)
    }

    private fun launchActivity() {
        showDatas()
        initializeClientImageOnClick()
    }


    private fun initializeClientImageOnClick() {
        imgClient.setOnClickListener {
            val intent = Intent(this, AccountActivityClient::class.java)
            intent.putExtra("ID", jobData.get(TakenJob.CLIENT).toString())

            startActivity(intent)
        }
    }

    private fun showDatas() {
        progress_circular.visibility = View.GONE
        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        container.layoutParams = params
        jobDetail.visibility = View.VISIBLE

        loadDatas()
    }

    private fun loadDatas() {
        txtJobName.text = jobData.get(TakenJob.NAME).toString()
        txtDeadline.text = jobData.get(TakenJob.DEADLINE).toString()
        txtPrice.text = jobData.get(TakenJob.EST_PRICE).toString()
        txtDescription.text = jobData.get(TakenJob.DESCRIPTION).toString()

        if (jobData.get(TakenJob.CLIENT).toString() != User.getEmail()) {
            applicantsContainer.visibility = View.GONE
            return
        }

        loadApplicants()
    }

    private fun loadApplicants() {
        val applicants = jobData.get(TakenJob.APPLICANTS) as ArrayList<String>
        if (applicants.isEmpty()) {
            applicantsContainer.visibility = View.GONE
            return
        }
        applicantsList.removeAllViews()

        applicants.forEach {
            var applicant = View.inflate(this, R.layout.applicant, null) as LinearLayout

            firebaseDatabase()
                .collection("users")
                .document(it)
                .get()
                .addOnSuccessListener { it2 ->
                    val applicantData = it2.data as HashMap<String, Any>

                    applicant.txtName.text = applicantData.get(User.NAME).toString()
                    loadImage(User.getProfileImagePath(applicantData.get(User.PROFILE_IMAGE).toString()), applicant.imgProfile)
                }

            applicant.btnAccept.setOnClickListener {_ -> acceptApplicant(it, applicant)}
            applicant.btnReject.setOnClickListener {_ -> rejectApplicant(it, applicant)}

            applicantsList.addView(applicant)
        }
    }

    private fun acceptApplicant(applicantID: String, view: View) {
        progress_loading.visibility = View.VISIBLE
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        acceptApplicant(applicantID)

        firebaseDatabase()
            .collection("jobs")
            .document(jobID)
            .update(jobData)
            .addOnSuccessListener {
                loadJobDetailPost(applicantID)
                progress_loading.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
    }

    private fun rejectApplicant(applicantID: String, view: View) {
        progress_loading.visibility = View.VISIBLE
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        firebaseDatabase()
            .collection("finished_jobs")
            .add(generateRejectData(applicantID))
            .addOnSuccessListener {
                removeApplicant(applicantID, view)

                firebaseDatabase()
                    .collection("jobs")
                    .document(jobID)
                    .set(jobData)
                    .addOnSuccessListener {
                        progress_loading.visibility = View.GONE
                        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        Log.d("testt", "succ")
                    }
            }
    }

    private fun loadJobDetailPost(applicantID: String) {
        val intent = Intent(this, JobDetailActivityPost::class.java)
        val otherPartyEmail = applicantID

        intent.putExtra(TakenJob.YOUR_ROLE, TakenJob.CLIENT)
        intent.putExtra(TakenJob.OTHER_PARTY_EMAIL, otherPartyEmail)
        intent.putExtra(TakenJob.ID, jobID)

        startActivity(intent)
        finish()
    }

    private fun acceptApplicant(applicantID: String) {
        jobData.remove(TakenJob.APPLICANTS)
        jobData.set(TakenJob.FREELANCER, applicantID)
        jobData.set(TakenJob.STATUS, TakenJob.ON_GOING)
    }

    private fun generateRejectData(applicantID: String): HashMap<String, Any> {
        val removeData = jobData.clone() as HashMap<String, Any>
        removeData.remove(TakenJob.APPLICANTS)
        removeData.set(TakenJob.FREELANCER, applicantID)
        removeData.set(TakenJob.STATUS, TakenJob.REJECTED)

        return removeData
    }

    private fun removeApplicant(applicantID: String, applicantView: View) {
        val applicants = jobData.get(TakenJob.APPLICANTS) as ArrayList<String>
        applicants.remove(applicantID)

        applicantsList.removeView(applicantView)
        if (applicants.isEmpty()) applicantsContainer.visibility = View.GONE
    }

    private fun loadImage(path: String, img: CircleImageView?) {
        firebaseStorageReference()
            .child(path)
            .downloadUrl
            .addOnSuccessListener{uri ->
                if (img != null) {
                    Glide.with(this)
                        .load(uri)
                        .into(img)
                }
            }
    }
}
