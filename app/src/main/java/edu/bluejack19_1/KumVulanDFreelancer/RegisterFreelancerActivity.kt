package edu.bluejack19_1.KumVulanDFreelancer

import android.Manifest
import android.app.Activity
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
import com.google.android.flexbox.FlexboxLayout
import kotlinx.android.synthetic.main.activity_register_freelancer.*
import kotlinx.android.synthetic.main.activity_register_freelancer.btnAddSkill
import kotlinx.android.synthetic.main.activity_register_freelancer.imgProfile
import kotlinx.android.synthetic.main.activity_register_freelancer.skillsContainer
import kotlinx.android.synthetic.main.activity_register_freelancer.txtAbout
import kotlinx.android.synthetic.main.activity_register_freelancer.txtName
import kotlinx.android.synthetic.main.activity_register_freelancer.txtSkill
import kotlinx.android.synthetic.main.profile_edit_skill.view.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class RegisterFreelancerActivity : AppCompatActivity() {

    var skillsList: ArrayList<View> = ArrayList()
    lateinit var imagePath: Uri

    companion object {
        private val IMAGE_PICK_CODE = 1000
        private val PERMISSION_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_freelancer)

        loadDatas()
        initializeAddSkillButton()
    }

    private fun loadDatas() {
        loadName()
        loadProfileImage()
        loadAbout()
        initializeCommitButton()
        initializeImageOnClick()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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

    private fun initializeCommitButton() {
        btnCommit.setOnClickListener {
            if (txtAbout.text.isEmpty() || txtAcademic.text.isEmpty() || txtName.text.isEmpty()) {
                when {
                    txtAbout.text.isEmpty() -> Toast.makeText(this, "About cannot be empty", Toast.LENGTH_LONG).show()
                    txtAcademic.text.isEmpty() -> Toast.makeText(this, "Academic record cannot be empty", Toast.LENGTH_LONG).show()
                    txtName.text.isEmpty() -> Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_LONG).show()
                }
            } else if (::imagePath.isInitialized) {

                // TODO add progress bar
                val progress: ProgressBar = ProgressBar(this)

                val imageName = UUID.randomUUID().toString()
                val ref = firebaseStorageReference().child("${User.PROFILE_IMAGE_DIR}/" + imageName)
                ref.putFile(imagePath)
                    .addOnSuccessListener {
                        Log.d("firebase", "image upload successful")
                        register(getNewProfileDatas(imageName))
                    }
                    .addOnFailureListener {
                        Log.d("firebase", "image upload not successful")
                    }
            } else {
                register(getNewProfileDatas(""))
            }
        }
    }

    private fun register(data: HashMap<String, Any>) {
        showLoading()
        firebaseDatabase().collection("users")
            .document(firebaseAuth().currentUser!!.email + "")
            .update(data)
            .addOnSuccessListener {
                hideLoading()
                refreshUserData(data)
                System.last_activity = System.REGISTER_FREELANCER_ACTIVITY
                finish()
            }.addOnFailureListener {
                hideLoading()
                Toast.makeText(applicationContext, "Failed registration as Freelancer", Toast.LENGTH_LONG).show()
            }
    }

    private fun refreshUserData(data: HashMap<String, Any>) {
        User.data?.set(User.NAME, data[User.NAME].toString())
        User.data?.set(User.PROFILE_IMAGE, data[User.PROFILE_IMAGE].toString())
        User.data?.set(User.SKILLS, data[User.SKILLS] as List<String>)
        User.data?.set(User.ABOUT, data[User.ABOUT].toString())
        User.data?.set(User.ACADEMIC, data[User.ACADEMIC].toString())
        User.data?.set(User.ROLE, data[User.ROLE].toString())
        User.data?.set(User.JOBS_DONE, data[User.JOBS_DONE].toString().toInt())
        User.data?.set(User.RATING, data[User.RATING].toString().toBigDecimal())
        User.data?.set(User.REVIEWS, data[User.REVIEWS] as ArrayList<Map<String, Any>>)
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
        data[User.JOBS_DONE] = 0
        data[User.RATING] = 0
        data[User.REVIEWS] = ArrayList<Map<String, Any>>()
        data[User.ROLE] = User.FREELANCER

        return data
    }

    private fun getSkills(): List<String> {
        return skillsList.map {
            it.text.text.toString()
        }
    }

    private fun initializeAddSkillButton() {
        btnAddSkill.setOnClickListener {
            if(txtSkill.text.toString().isNotEmpty()) {
                addNewSkill(txtSkill.text.toString())
                txtSkill.setText("")
            }
        }
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

    private fun loadName() {
        txtName.setText(User.getName())
    }

    private fun loadProfileImage() {
        firebaseStorageReference()
            .child(User.getProfileImage())
            .downloadUrl
            .addOnSuccessListener{ uri ->
                if (imgProfile != null) {
                    Glide.with(this)
                        .load(uri)
                        .into(imgProfile)
                }
            }
    }

    private fun loadAbout() {
        txtAbout.setText(User.getAbout())
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
