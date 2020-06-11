package edu.bluejack19_1.KumVulanDFreelancer.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexboxLayout
import edu.bluejack19_1.KumVulanDFreelancer.*
import edu.bluejack19_1.KumVulanDFreelancer.adapters.ReviewAdapter
import kotlinx.android.synthetic.main.fragment_account_client.*
import kotlinx.android.synthetic.main.fragment_account_freelancer.*
import kotlinx.android.synthetic.main.fragment_account_freelancer.btnEdit
import kotlinx.android.synthetic.main.fragment_account_freelancer.btnLogout
import kotlinx.android.synthetic.main.fragment_account_freelancer.profileImage
import kotlinx.android.synthetic.main.fragment_account_freelancer.txtAbout
import kotlinx.android.synthetic.main.fragment_account_freelancer.txtName
import java.lang.Exception
import kotlin.collections.ArrayList

class AccountFragmentFreelancer(parent: MainActivity) : Fragment() {

    val parent = parent

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account_freelancer, container, false)
    }

    override fun onResume() {
        super.onResume()


        if(System.last_activity == System.EDIT_PROFILE_ACTIVITY) {
            loadEditableDatas()
        }

        System.last_activity = System.ACCOUNT_FRAGMENT
    }

    private fun loadName() {
        txtName.text = User.getName()
    }

    private fun loadJobsTaken() {
        txtJobsTaken.text = getString(R.string.profile_jobs_done, User.getJobsDone().toString())
    }

    private fun loadRating() {
        txtRating.text = getString(R.string.profile_rating, User.getRating())
    }

    private fun loadProfileImage() {

        try {
            firebaseStorageReference()
                .child(User.getProfileImage())
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

    private fun loadSkills() {
        val skills = User.getSkills()
        skillsContainer.removeAllViews()

        skills.forEach {
            var skill: TextView = View.inflate(context, R.layout.profile_skill, null) as TextView
            skill.text = it

            val params: FlexboxLayout.LayoutParams = FlexboxLayout.LayoutParams(FlowLayout.LayoutParams.WRAP_CONTENT, FlowLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(5, 10, 5, 10)
            skill.layoutParams = params

            skillsContainer.addView(skill)
        }
    }

    private fun loadAbout() {
        txtAbout.text = User.getAbout()
    }

    private fun loadAcademic() {
        txtAcademic.text = User.getAcademicRecord()
    }

    private fun getReviewArrayList(list: ArrayList<Map<String, Any>>): ArrayList<Review> {
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
        return reviews
    }

    private fun loadReview() {
        val reviews = User.getReviews()

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
        val adapter = ReviewAdapter(this.context!!, getReviewArrayList(reviews))
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

    private fun loadEditableDatas() {
        loadProfileImage()
        loadName()
        loadSkills()
        loadAbout()
        loadAcademic()
    }

    private fun loadProfileDatas() {
        loadEditableDatas()
        loadJobsTaken()
        loadRating()
        loadReview()
    }

    private fun initializeLogoutButton() {
        btnLogout.setOnClickListener {
            firebaseAuth().signOut()
            parent.logout()
        }
    }

    private fun initializeEditButton() {
        btnEdit.setOnClickListener {
            val intent = Intent(this.context, EditProfileActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadProfileDatas()
        initializeLogoutButton()
        initializeAboutDisclaimer()
        initializeEditButton()
    }
    private fun initializeAboutDisclaimer(){
        btnAboutAndDisclaimerFreelancer.setOnClickListener {
            val intent = Intent(this.context, AboutDisclaimer::class.java);
            startActivity(intent)
        }
    }
}
