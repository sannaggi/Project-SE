package edu.bluejack19_1.KumVulanDFreelancer.recycleView.item

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import edu.bluejack19_1.KumVulanDFreelancer.firebase.FinishedJob
import edu.bluejack19_1.KumVulanDFreelancer.R
import edu.bluejack19_1.KumVulanDFreelancer.adapters.FinishedJobAdapter
import kotlinx.android.synthetic.main.item_taken_job_history.*

class TakenJobItem(var job: FinishedJob, val context: Context) : Item(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        init(viewHolder)
    }

    private fun init(viewHolder: ViewHolder){
        viewHolder.taken_job_name.text = job.name
        viewHolder.taken_job_deadline.text = job.deadline
        viewHolder.taken_job_description.text = job.description
        FinishedJob.fetchName(viewHolder.taken_job_client, job.client)
        viewHolder.taken_job_price.text = job.price
        viewHolder.taken_job_status.text = job.status
        viewHolder.taken_job_category_name.text = job.category

        changeStatusColor(viewHolder, job.status)
    }

    private fun changeStatusColor(viewHolder: ViewHolder, status : String){
        var status = status
        if(status.equals("Finished")) viewHolder.taken_job_status
                .setBackgroundResource(R.drawable.soft_border_pure_green)
        else if(status.equals("Rejected")) viewHolder.taken_job_status
                .setBackgroundResource(R.drawable.soft_border_yellow)
        else if(status.equals("Canceled")) viewHolder.taken_job_status
                .setBackgroundResource(R.drawable.soft_border_red)

    }

    override fun getLayout(): Int = R.layout.item_taken_job_history

}