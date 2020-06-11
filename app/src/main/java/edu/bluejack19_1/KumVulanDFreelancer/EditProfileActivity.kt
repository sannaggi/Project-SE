package edu.bluejack19_1.KumVulanDFreelancer

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.flexbox.FlexboxLayout
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_edit_profile.progress_loading
import kotlinx.android.synthetic.main.activity_edit_profile.skillsContainer
import kotlinx.android.synthetic.main.activity_edit_profile.txtAbout
import kotlinx.android.synthetic.main.activity_edit_profile.txtAcademic
import kotlinx.android.synthetic.main.activity_edit_profile.txtName
import kotlinx.android.synthetic.main.activity_job_detail_pre.*
import kotlinx.android.synthetic.main.profile_edit_skill.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class EditProfileActivity : AppCompatActivity() {

    var skillsList: ArrayList<View> = ArrayList()
    lateinit var imagePath: Uri

    companion object {
        private val IMAGE_PICK_CODE = 1000
        private val PERMISSION_CODE = 1001
    }

    override fun onResume() {
        super.onResume()
        System.last_activity = System.EDIT_PROFILE_ACTIVITY
    }

    private fun addNewSkill(string: String) {
        var skill: LinearLayout = View.inflate(this, R.layout.profile_edit_skill, null) as LinearLayout

        val params: FlexboxLayout.LayoutParams = FlexboxLayout.LayoutParams(FlowLayout.LayoutParams.WRAP_CONTENT, FlowLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(5, 10, 5, 10)
        skill.layoutParams = params

        skill.text.text = string
        skill.btnRemove.setOnClickListener{
            skillsList.remove(skill)
            skillsContainer.removeView(skill)
        }

        skillsContainer.addView(skill)
        skillsList.add(skill)
    }

    private fun loadSkills() {
        val skills = User.getSkills()

        skills.forEach {
            addNewSkill(it)
        }
    }

    private fun loadProfileImage() {
        firebaseStorageReference().child(User.getProfileImage())
            .downloadUrl
            .addOnSuccessListener{ uri ->
                if (imgProfile != null) {
                    Glide.with(this)
                        .load(uri)
                        .thumbnail(0.25f)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imgProfile)
                }
        }
    }

    private fun loadName() {
        txtName.setText(User.getName())
    }

    private fun loadAbout() {
        txtAbout.setText(User.getAbout())
    }

    private fun loadAcademic() {
        txtAcademic.setText(User.getAcademicRecord())
    }

    private fun initializeInitialValues() {
        loadProfileImage()
        loadName()
        loadSkills()
        loadAbout()
        loadAcademic()
    }

    private fun getSkills(): List<String> {
        return skillsList.map {
            it.text.text.toString()
        }
    }

    private fun getNewProfileDatas(imageName: String): HashMap<String, Any> {
        val name = txtName.text.toString()
        val profileImage = if (imageName.isEmpty()) User.getProfileImageName() else imageName
        val skills = getSkills()
        val about = txtAbout.text.toString()
        val academic = txtAcademic.text.toString()

        val data = HashMap<String, Any>()
        data[User.NAME] = name
        data[User.PROFILE_IMAGE] = profileImage
        data[User.SKILLS] = skills
        data[User.ABOUT] = about
        data[User.ACADEMIC] = academic
        data[User.JOBS_DONE] = User.getJobsDone()
        data[User.RATING] = User.getRating().toDouble()
        data[User.REVIEWS] = User.getReviews()
        data[User.ROLE] = User.getRole()

        return data
    }

    private fun refreshUserData(data: HashMap<String, Any>) {
        User.data?.set(User.NAME, data[User.NAME].toString())
        User.data?.set(User.PROFILE_IMAGE, data[User.PROFILE_IMAGE].toString())
        User.data?.set(User.SKILLS, data[User.SKILLS] as List<String>)
        User.data?.set(User.ABOUT, data[User.ABOUT].toString())
        User.data?.set(User.ACADEMIC, data[User.ACADEMIC].toString())
    }

    private fun initializeCommitButton() {
        btnCommit.setOnClickListener {
            if (txtAbout.text.isEmpty() || txtAcademic.text.isEmpty() || txtName.text.isEmpty() || !containsAlpha(txtName.text.toString())) {
                when {
                    txtAbout.text.isEmpty() -> Toast.makeText(this, "About cannot be empty", Toast.LENGTH_LONG).show()
                    txtAcademic.text.isEmpty() -> Toast.makeText(this, "Academic record cannot be empty", Toast.LENGTH_LONG).show()
                    txtName.text.isEmpty() -> Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_LONG).show()
                    !containsAlpha(txtName.text.toString()) -> Toast.makeText(this, "Name must contains letter", Toast.LENGTH_LONG).show()
                }
            } else if (::imagePath.isInitialized) {
                showLoading()

                val imageName = UUID.randomUUID().toString()
                val ref = firebaseStorageReference().child("${User.PROFILE_IMAGE_DIR}/" + imageName)
                ref.putFile(imagePath)
                    .addOnSuccessListener {
                        Log.d("firebase", "image upload successful")
                        val data = getNewProfileDatas(imageName)
                        firebaseDatabase().collection("users")
                            .document(firebaseAuth().currentUser!!.email + "")
                            .update(data).addOnSuccessListener {
                                hideLoading()
                                refreshUserData(data)
                                Toast.makeText(applicationContext, "Profile updated successfully", Toast.LENGTH_LONG).show()
                                finish()
                            }.addOnFailureListener {
                                hideLoading()
                                Toast.makeText(applicationContext, "Profile update failed", Toast.LENGTH_LONG).show()
                            }
                    }
                    .addOnFailureListener {
                        Log.d("firebase", "image upload unsuccessful")
                    }
            } else {
                showLoading()
                val data = getNewProfileDatas("")
                firebaseDatabase().collection("users")
                    .document(firebaseAuth().currentUser!!.email + "")
                    .update(data).addOnSuccessListener {
                        hideLoading()
                        refreshUserData(data)
                        Toast.makeText(applicationContext, "Profile updated successfully", Toast.LENGTH_LONG).show()
                        finish()
                    }.addOnFailureListener {
                        hideLoading()
                        Toast.makeText(applicationContext, "Profile update failed", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

    private fun containsAlpha(name: String): Boolean {
        for (i in name.indices) {
            if (name[i].isLetter()) return true
        }
        return false
    }

    private fun initializeImageOnClick() {
        imgProfile.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery()
                } else {
                    // permission denied
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                    requestPermissions(permissions, PERMISSION_CODE)
                }
            } else {
                // system os < marshmallow
                pickImageFromGallery()
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_PICK_CODE)
    }

    private fun initializeAddSkillButton() {
        btnAddSkill.setOnClickListener {
            if(txtSkill.text.toString().isNotEmpty()) {
                addNewSkill(txtSkill.text.toString())
                txtSkill.setText("")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        initializeInitialValues()
        initializeCommitButton()
        initializeImageOnClick()
        initializeAddSkillButton()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            imgProfile.setImageURI(data?.data)
            imagePath = data?.data!!
        }
    }

    private fun showLoading() {
        progress_loading.visibility = View.VISIBLE
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun hideLoading() {
        progress_loading.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}
