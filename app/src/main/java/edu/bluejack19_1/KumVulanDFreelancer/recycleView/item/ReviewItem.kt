package edu.bluejack19_1.KumVulanDFreelancer.recycleView.item

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import edu.bluejack19_1.KumVulanDFreelancer.*
import kotlinx.android.synthetic.main.review.*
import java.lang.Exception

class ReviewItem (var review: Review, val context: Context) : Item(){
    private lateinit var reviewDocRef : DocumentReference
    private var isLike = false
    private var isDislike = false
    private var likerCount = 0
    private var dislikerCount = 0

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.txtName.text = review.name
        viewHolder.txtRating.text = review.rating.toString() + " ★"
        viewHolder.txtReview.text = review.review
        reviewDocRef = firebaseDatabase().collection("reviews").document(review.id)
        viewHolder.review_like_dislike.visibility = View.VISIBLE
        reviewDocRef.get().addOnSuccessListener {
            if(it.data == null) return@addOnSuccessListener
            var data = it.data!!
            var liker = data["liker"] as List<String>
            var disliker = data["disliker"] as List<String>

            if(liker.contains(User.getEmail())){
                isLike = true
                viewHolder.review_like_btn.setTextColor(Color.BLUE)
            }
            if(disliker.contains(User.getEmail())){
                isDislike = true
                viewHolder.review_dislike_btn.setTextColor(Color.RED)
            }

            likerCount = liker.size
            dislikerCount = disliker.size

            viewHolder.review_like_count.text = "${likerCount}"
            viewHolder.review_dislike_count.text = "${dislikerCount}"
        }
        viewHolder.review_like_btn.setOnClickListener {
            if(isDislike){
                Toast.makeText(context, "You cannot Like and Dislike same Review", Toast.LENGTH_LONG)
                return@setOnClickListener
            }
            var forUpdate = HashMap<String, Any>()

            if(!isLike){
                isLike = true
                likerCount++
                forUpdate.put("liker", FieldValue.arrayUnion(User.getEmail()))
            }
            else{
                isLike = false
                likerCount--
                forUpdate.put("liker", FieldValue.arrayRemove(User.getEmail()))
            }
            reviewDocRef.update(forUpdate)

            viewHolder.review_like_count.text = "${likerCount}"
        }
        viewHolder.review_dislike_btn.setOnClickListener {
            if(isLike){
                Toast.makeText(context, "You cannot Like and Dislike same Review", Toast.LENGTH_LONG)
                return@setOnClickListener
            }

            var forUpdate = HashMap<String, Any>()

            if(!isDislike){
                isDislike = true
                dislikerCount++
                forUpdate.put("disliker", FieldValue.arrayUnion(User.getEmail()))
            }
            else{
                isDislike = false
                dislikerCount--
                forUpdate.put("disliker", FieldValue.arrayRemove(User.getEmail()))
            }

            reviewDocRef.update(forUpdate)

            viewHolder.review_dislike_count.text = "${dislikerCount}"
        }
        Log.d("ReviewItem", review.review)

        loadProfileImage(viewHolder)
    }

    private fun loadProfileImage(viewHolder: ViewHolder){
        try {
            firebaseStorageReference()
                    .child(User.getProfileImagePath(review.profile_image))
                    .downloadUrl
                    .addOnSuccessListener{
                        uri -> Glide.with(context)
                            .load(uri)
                            .into(viewHolder.imgProfile)
                    }
                    .addOnFailureListener {
                        Log.d("firebase", it.toString())
                    }
        } catch (e: Exception) {
            Log.d("firebase", "invalid image loading intercepted")
        }
    }

    override fun getLayout(): Int = R.layout.review

}