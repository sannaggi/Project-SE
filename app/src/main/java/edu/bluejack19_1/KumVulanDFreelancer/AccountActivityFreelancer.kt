package edu.bluejack19_1.KumVulanDFreelancer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexboxLayout
import edu.bluejack19_1.KumVulanDFreelancer.adapters.ReviewAdapter
import kotlinx.android.synthetic.main.activity_account_freelancer.*
import java.lang.Exception
import java.math.BigDecimal

@Suppress("UNCHECKED_CAST")
class AccountActivityFreelancer : AppCompatActivity() {

    lateinit var userID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_freelancer)

        fetchIntentData()
        loadProfileDatas()
        initializeShowAll()
    }

    private fun initializeShowAll(){
        show_all_review_button.setOnClickListener {
            var intent = Intent(this, ReviewActivity::class.java)
            intent.putExtra("ID", userID)
            startActivity(intent)
        }
    }

    private fun fetchIntentData() {
        userID = intent.extras!!.get("ID").toString()
    }

    private fun loadName(name: String) {
        txtName.text = name
    }

    private fun loadJobsTaken(jobsDone: Int) {
        txtJobsTaken.text = getString(R.string.profile_jobs_done, jobsDone.toString())
    }

    private fun loadRating(rating: BigDecimal) {
        txtRating.text = getString(R.string.profile_rating, rating)
    }

    private fun loadProfileImage(imagePath: String) {
        try {
            firebaseStorageReference()
                .child(imagePath)
                .downloadUrl
                .addOnSuccessListener{uri ->
                    if (profileImage != null) {
                        Glide.with(this)
                            .load(uri)
                            .into(profileImage)
                    }
                }
                .addOnFailureListener {
                    Log.d("firebase", it.toString())
                }
        } catch (e: Exception) {
            Log.d("firebase", "invalid image loading intercepted")
        }
//      uri -> Picasso.with(this.context).load(uri).into(profileImage)
    }

    private fun loadSkills(skills: ArrayList<String>) {
        skillsContainer.removeAllViews()

        skills.forEach {
            var skill: TextView = View.inflate(this, R.layout.profile_skill, null) as TextView
            skill.text = it

            val params: FlexboxLayout.LayoutParams = FlexboxLayout.LayoutParams(FlowLayout.LayoutParams.WRAP_CONTENT, FlowLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(5, 10, 5, 10)
            skill.layoutParams = params

            skillsContainer.addView(skill)
        }
    }

    private fun loadAbout(about: String) {
        txtAbout.text = about
    }

    private fun loadAcademic(academic: String) {
        txtAcademic.text = academic
    }

    private fun getReviewArrayList(list: ArrayList<Map<String, Any>>): ArrayList<Review> {
        var list = list.reversed()
        var reviews: ArrayList<Review> = ArrayList()
        var size = list.size

        if(size >= 5)
            size = 5

        for (i in 0 until size) {
            val name = list[i].get(User.NAME).toString()
            val profile_image = list[i].get(User.PROFILE_IMAGE).toString()
            val rating = list[i].get(User.RATING).toString().toBigDecimal()
            val review = list[i].get(User.REVIEW).toString()
            val id = list[i].get("id").toString()
            reviews.add(Review(name, profile_image, rating, review, id))
        }

        var toRet = ArrayList<Review>()
        reviews.reversed().forEach {
            toRet.add(it)
        }


        return toRet
    }

    private fun loadReview(reviews: ArrayList<Map<String, Any>>) {
        if (reviews.isEmpty()) {
            hideReviews()
            return
        }
        populateReviewsContainer(reviews)

    }

    private fun hideReviews() {
        txtReview.visibility = View.GONE
        listReview.visibility = View.GONE
    }

    private fun populateReviewsContainer(reviews: ArrayList<Map<String, Any>>) {
        val adapter = ReviewAdapter(this, getReviewArrayList(reviews))
        listReview.adapter = adapter

        var size = reviews.size

        if(size >= 5)
            size = 5

        var totalHeight = 0
        for(i in 0 until size) {
            val item = adapter.getView(i, null, listReview)
            item.measure(0, 0)
            totalHeight += item.measuredHeight
        }

        val params = listReview.layoutParams
        params.height = totalHeight + (listReview.dividerHeight * (listReview.count - 1))
        listReview.layoutParams = params
    }

    private fun loadEditableDatas(data: HashMap<String, Any>) {
        loadProfileImage(User.getProfileImagePath(data.get(User.PROFILE_IMAGE).toString()))
        loadName(data.get(User.NAME).toString())
        loadSkills(data.get(User.SKILLS) as ArrayList<String>)
        loadAbout(data.get(User.ABOUT).toString())
        loadAcademic(data.get(User.ACADEMIC).toString())
    }

    private fun loadProfileDatas() {
        firebaseDatabase()
            .collection("users")
            .document(userID)
            .get()
            .addOnSuccessListener {
                disableLoading()
                showDatas(it.data as HashMap<String, Any>)
            }
    }


    private fun disableLoading() {
        progress_circular.visibility = View.GONE
        container.visibility = View.VISIBLE

        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        bigContainer.layoutParams = params
    }

    private fun showDatas(data: HashMap<String, Any>) {
        loadEditableDatas(data)
        loadJobsTaken(data.get(User.JOBS_DONE).toString().toInt())
        loadRating(data.get(User.JOBS_DONE).toString().toBigDecimal())
        loadReview(data.get(User.REVIEWS) as ArrayList<Map<String, Any>>)
    }
}
