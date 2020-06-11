package edu.bluejack19_1.KumVulanDFreelancer.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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

class AccountFragmentClient(parent: MainActivity) : Fragment() {

    val parent = parent

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account_client, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadProfileDatas()
        initializeLogoutButton()
        initializeAboutDisclaimer()
        initializeEditButton()
        initializeRegisterFreelancerButton()
    }

    private fun initializeAboutDisclaimer(){
        btnAboutAndDisclaimerClient.setOnClickListener {
            val intent = Intent(this.context, AboutDisclaimer::class.java);
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        if(System.last_activity == System.EDIT_PROFILE_ACTIVITY) {
            loadProfileDatas()
        } else if (System.last_activity == System.REGISTER_FREELANCER_ACTIVITY) {
            parent.loginFromAccount()
        }

        System.last_activity = System.ACCOUNT_FRAGMENT
    }

    private fun loadName() {
        txtName.text = User.getName()
    }

    private fun loadProfileImage() {

        try {
            firebaseStorageReference()
                .child(User.getProfileImage())
                .downloadUrl
                .addOnSuccessListener{
                        uri -> Glide.with(this)
                    .load(uri)
                    .into(profileImage)
                }
                .addOnFailureListener {
                    Log.d("firebase", it.toString())
                }
        } catch (e: Exception) {
            Log.d("firebase", "invalid image loading intercepted")
        }
    }

    private fun loadAbout() {
        txtAbout.text = User.getAbout()
    }

    private fun loadProfileDatas() {
        loadProfileImage()
        loadName()
        loadAbout()
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

    private fun initializeRegisterFreelancerButton() {
        btnRegisterFreelancer.setOnClickListener {
            val builder = AlertDialog.Builder(this.context!!)
            builder.setTitle("Register as Freelancer")
            builder.setMessage("Are you sure you want to register as freelancer?")
                .setCancelable(true)
                .setPositiveButton("YES") { dialog, which ->
                    val intent = Intent(this.context, RegisterFreelancerActivity::class.java)
                    startActivity(intent)
                    System.last_activity = System.LOGIN_REGISTER_ACTIVITY
                }
                .setNegativeButton("NO"){dialog, which ->  }
            builder.create().show()
        }
    }
}
