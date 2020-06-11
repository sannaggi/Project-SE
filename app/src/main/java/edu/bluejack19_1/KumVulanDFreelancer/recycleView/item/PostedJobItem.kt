package edu.bluejack19_1.KumVulanDFreelancer.recycleView.item

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import edu.bluejack19_1.KumVulanDFreelancer.GivingReviewActivity
import edu.bluejack19_1.KumVulanDFreelancer.firebase.FinishedJob
import edu.bluejack19_1.KumVulanDFreelancer.R
import kotlinx.android.synthetic.main.item_posted_job_history.*

class PostedJobItem (var job: FinishedJob, val context: Context) : Item(){
    lateinit var viewHolder: ViewHolder

    override fun bind(viewHolder: ViewHolder, position: Int) {
        this.viewHolder = viewHolder
        init(viewHolder)
    }

    override fun getLayout(): Int = R.layout.item_posted_job_history

    private fun init(viewHolder: ViewHolder){
        viewHolder.posted_job_name.text = job.name
        viewHolder.posted_job_deadline.text = job.deadline
        viewHolder.posted_job_description.text = job.description
        FinishedJob.fetchName(viewHolder.posted_job_freelancer, job.freelancer);
        viewHolder.posted_job_price.text = job.price
        viewHolder.posted_job_status.text = job.status
        viewHolder.posted_job_category_name.text = job.category

        changeStatusColor(viewHolder, job.status)
        initReviewRatingButton(viewHolder)
    }

    private fun changeStatusColor(viewHolder: ViewHolder, status : String){
        var status = status
        if(status.equals("Finished")) viewHolder.posted_job_status
                .setBackgroundResource(R.drawable.soft_border_pure_green)
        else if(status.equals("Rejected")) viewHolder.posted_job_status
                .setBackgroundResource(R.drawable.soft_border_yellow)
        else if(status.equals("Canceled")) viewHolder.posted_job_status
                .setBackgroundResource(R.drawable.soft_border_red)

    }

    private fun initReviewRatingButton(viewHolder: ViewHolder){
        var isRated = false
        var isReviewed = false
        if(job.isRated == true) isRated = true
        if(job.isReviewed == true) isReviewed = true
        if(!job.status.equals("Finished")){
            isRated = true
            isReviewed = true
        }

        if(isRated and isReviewed) viewHolder.posted_job_review_rating.visibility = View.GONE
    }
}