package edu.bluejack19_1.KumVulanDFreelancer

import android.util.Log
import com.google.firebase.firestore.ListenerRegistration
import java.math.BigDecimal

class User {

    companion object {
        var data: HashMap<String, Any>? = HashMap()

        val JOBS_DONE = "jobs_done"
        val PROFILE_IMAGE = "profile_image"
        val RATING = "rating"
        val NAME = "name"
        val ACADEMIC = "academic"
        val REVIEWS = "reviews"
        val REVIEW = "review"
        val SKILLS = "skills"
        val ABOUT = "about"
        val PROFILE_IMAGE_DIR = "/profile_images"
        val ROLE = "role"
        val FREELANCER = "Freelancer"
        val CLIENT = "Client"
        val DEFAULT_PROFILE_IMAGE = "default_profile.png"
        val CHAT_PEOPLE = "chat_people"
        fun getChatPeople() : ArrayList<HashMap<String, Any>>?{
            return data?.get(CHAT_PEOPLE) as ArrayList<HashMap<String, Any>>?
        }

        fun getEmail(): String {
            return firebaseAuth().currentUser!!.email!!
        }

        fun getProfileImage(): String {
            return "${PROFILE_IMAGE_DIR}/${getProfileImageName()}"
        }

        fun getProfileImagePath(path: String): String {
            return "${PROFILE_IMAGE_DIR}/${path}"
        }

        fun getProfileImageName(): String {
            return data?.get(PROFILE_IMAGE).toString()
        }

        fun getName(): String {
            return data?.get(NAME).toString()
        }

        fun getJobsDone(): Int {
            return data?.get(JOBS_DONE).toString().toInt()
        }

        fun getRating(): BigDecimal {
            return data?.get(RATING).toString().toBigDecimal()
        }

        fun getSkills(): List<String> {
            return data?.get(SKILLS) as List<String>
        }

        fun getAbout(): String {
            return data?.get(ABOUT).toString()
        }

        fun getAcademicRecord(): String {
            return data?.get(ACADEMIC).toString()
        }

        fun getReviews(): ArrayList<Map<String, Any>> {
            return data?.get(REVIEWS) as ArrayList<Map<String, Any>>
        }

        fun getRole(): String {
            return data?.get(ROLE).toString()
        }

    }
}