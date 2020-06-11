package edu.bluejack19_1.KumVulanDFreelancer.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import edu.bluejack19_1.KumVulanDFreelancer.R
import edu.bluejack19_1.KumVulanDFreelancer.Review
import edu.bluejack19_1.KumVulanDFreelancer.User
import edu.bluejack19_1.KumVulanDFreelancer.firebaseStorageReference
import kotlinx.android.synthetic.main.fragment_account_freelancer.*
import org.w3c.dom.Text
import java.lang.Exception

class ReviewAdapter(private val context: Context, private val reviews: List<Review>): BaseAdapter(){

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return reviews.size
    }

    override fun getItem(position: Int): Any {
        return reviews[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = inflater.inflate(R.layout.review, parent, false)

        val imgProfile = row.findViewById(R.id.imgProfile) as de.hdodenhof.circleimageview.CircleImageView
        val txtName = row.findViewById(R.id.txtName) as TextView
        val txtRating = row.findViewById(R.id.txtRating) as TextView
        val txtReview = row.findViewById(R.id.txtReview) as TextView

        val review = getItem(position) as Review

        try {
            firebaseStorageReference()
                .child("${User.PROFILE_IMAGE_DIR}/${review.profile_image}")
                .downloadUrl.addOnSuccessListener{
                    uri -> Glide.with(context)
                .load(uri)
                .thumbnail(0.25f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgProfile)
            }
        } catch (e: Exception) {
            Log.d("firebase", "invalid image loading intercepted")
        }
        txtName.text = review.name
        txtRating.text = "${review.rating} ★"
        txtReview.text = review.review

        return row
    }
}