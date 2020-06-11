package edu.bluejack19_1.KumVulanDFreelancer

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_giving_review.*

class GivingReviewActivity : AppCompatActivity() {

    companion object{
        lateinit var userID : String
        lateinit var jobID : String
        lateinit var view: ViewHolder
    }

    var rating = 0;
    var reviewString = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_giving_review)

        System.last_activity = System.HOME_FRAGMENT
        setRatingColor()
        giving_review_star1.setOnClickListener {
            rating = 1
            setRatingColor()
        }
        giving_review_star2.setOnClickListener {
            rating = 2
            setRatingColor()
        }
        giving_review_star3.setOnClickListener {
            rating = 3
            setRatingColor()
        }
        giving_review_star4.setOnClickListener {
            rating = 4
            setRatingColor()
        }
        giving_review_star5.setOnClickListener {
            rating = 5
            setRatingColor()
        }

        giving_review_submit_button.setOnClickListener {
            if(validateReview()) return@setOnClickListener

            updateJob()
            updateReview()
            System.last_activity = System.HOME_FRAGMENT
            this@GivingReviewActivity.finish()
        }
    }

    private fun updateJob(){
        var forUpdate = HashMap<String, Any>()
        forUpdate.put("reviewed", true)
        forUpdate.put("rated", true)

        firebaseDatabase().collection("finished_jobs").document(jobID).update(forUpdate)
    }

    private fun updateReview(){
        var newReview = HashMap<String, Any>()
        newReview.put("liker", FieldValue.arrayUnion())
        newReview.put("disliker", FieldValue.arrayUnion())

        firebaseDatabase().collection("reviews").add(newReview).addOnSuccessListener {

            var forUpdate = HashMap<String, Any>()
            forUpdate.put("reviews", FieldValue.arrayUnion(
                    Review(
                            User.getName(),
                            User.getProfileImageName(),
                            rating,
                            reviewString,
                            it.id
                    )
            ))

            firebaseDatabase().collection("users").document(userID).update(forUpdate)
        }

    }

    private fun validateReview() : Boolean{
        if(rating == 0){
            Toast.makeText(this, "Please input rating!", Toast.LENGTH_LONG)
            return true
        }
        reviewString = giving_review_review_box.text.toString()
        if(reviewString.equals("")){
            Toast.makeText(this, "Please input rating!", Toast.LENGTH_LONG)
            return true
        }

        return false
    }

    private fun setRatingColor(){
        giving_review_star1.setTextColor(Color.LTGRAY)
        giving_review_star2.setTextColor(Color.LTGRAY)
        giving_review_star3.setTextColor(Color.LTGRAY)
        giving_review_star4.setTextColor(Color.LTGRAY)
        giving_review_star5.setTextColor(Color.LTGRAY)

        if(1 <= rating)
            giving_review_star1.setTextColor(Color.YELLOW)
        if(2 <= rating)
            giving_review_star2.setTextColor(Color.YELLOW)
        if(3 <= rating)
            giving_review_star3.setTextColor(Color.YELLOW)
        if(4 <= rating)
            giving_review_star4.setTextColor(Color.YELLOW)
        if(5 <= rating)
            giving_review_star5.setTextColor(Color.YELLOW)
    }
}
