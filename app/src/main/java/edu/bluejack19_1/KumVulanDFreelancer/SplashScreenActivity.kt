package edu.bluejack19_1.KumVulanDFreelancer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class SplashScreenActivity : AppCompatActivity() {

    private fun launchMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val currentUser = firebaseAuth().currentUser
        if(currentUser != null) {
            firebaseDatabase().collection("users")
                .document(currentUser.email + "")
                .get().addOnSuccessListener {
                    if(it.data != null) {
                        User.data = it.data as HashMap<String, Any>
                        Log.d("firebase", "curr user initiated: ${it.data.toString()}")
                    }
                    launchMain()
                }
        } else {
            Log.d("firebase", "current user null")
            launchMain()
        }
    }
}
