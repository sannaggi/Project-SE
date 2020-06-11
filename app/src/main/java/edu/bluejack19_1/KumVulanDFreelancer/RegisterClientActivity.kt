package edu.bluejack19_1.KumVulanDFreelancer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_register_client.*

class RegisterClientActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_client)

        btnSubmit.setOnClickListener {
            val name = txtName.text.toString()

            if (name.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_LONG).show()
            } else {
                val newUserData = generateNewUserData(name)
                firebaseDatabase()
                    .collection("users")
                    .document(firebaseAuth().currentUser!!.email.toString())
                    .set(newUserData)
                    .addOnSuccessListener {
                        User.data = newUserData
                        finish()
                    }
            }
        }
    }

    private fun generateNewUserData(name: String): HashMap<String, Any> {
        val data = HashMap<String, Any>()
        data[User.NAME] = name
        data[User.PROFILE_IMAGE] = User.DEFAULT_PROFILE_IMAGE
        data[User.ROLE] = User.CLIENT
        data[User.ABOUT] = "Hi there! I'm using KumVulanDFreelancer!"

        return data
    }

}
