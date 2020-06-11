package edu.bluejack19_1.KumVulanDFreelancer.recycleView.item

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import com.google.firebase.firestore.FieldValue
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import edu.bluejack19_1.KumVulanDFreelancer.*
import edu.bluejack19_1.KumVulanDFreelancer.firebase.FinishedJob
import edu.bluejack19_1.KumVulanDFreelancer.firebase.Job
import kotlinx.android.synthetic.main.item_job.*

class JobItem(var job: Job, val context: Context) : Item(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        checkingApplyAndInitialize(viewHolder)
        viewHolder.job_deadline.text = job.deadline;
        viewHolder.job_description.text = job.description
        viewHolder.job_name.text = job.name
        viewHolder.job_price.text = job.price
        viewHolder.job_category_name.text = job.category
        FinishedJob.fetchName(viewHolder.job_client, job.client);

    }

    private fun checkingApplyAndInitialize(viewHolder: ViewHolder){
        if(job.applicants.isNullOrEmpty() || !job.applicants?.contains(firebaseAuth().currentUser?.email)!!){

            Log.d("JobItem", "${job.name}")
            viewHolder.job_apply_button.visibility = View.VISIBLE
            viewHolder.job_applied_text.visibility = View.GONE
            viewHolder.job_apply_button.setOnClickListener {
                var insertApplicants = HashMap<String, Any>()
                insertApplicants.put(TakenJob.APPLICANTS, FieldValue.arrayUnion(firebaseAuth().currentUser?.email))
                firebaseDatabase().collection("jobs").document(job.id).update(insertApplicants)

                viewHolder.job_apply_button.visibility = View.GONE
                viewHolder.job_applied_text.visibility = View.VISIBLE
            }
        }
        else{
            viewHolder.job_apply_button.visibility = View.GONE
            viewHolder.job_applied_text.visibility = View.VISIBLE
        }
    }

    override fun getLayout(): Int = R.layout.item_job

}