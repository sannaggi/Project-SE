package edu.bluejack19_1.KumVulanDFreelancer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.facebook.appevents.AppEventsLogger
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login_register.*

import com.facebook.login.LoginResult
import com.facebook.login.LoginManager
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.facebook.*
import com.google.firebase.auth.FacebookAuthProvider
import java.util.*
import kotlin.collections.HashMap


class LoginRegisterActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 9001
    var callbackManager : CallbackManager? = null;

    private fun getGoogleSignInClient() : GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(this, gso)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_register);
//        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(application);

        login_register_button.visibility = View.VISIBLE
        login_register_loading.visibility = View.GONE
        
        Log.d("firebase", "shtasdsa")
        googleLoginBtn.setOnClickListener{
            Log.d("firebase", "sht3")
            val signInIntent = getGoogleSignInClient().signInIntent;
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        facebookLoginBtn.setPermissions("email", "public_profile");
        facebookLoginBtn.setOnClickListener(View.OnClickListener {

//            startAFR
            login_register_button.visibility = View.GONE
            login_register_loading.visibility = View.VISIBLE
            callbackManager = CallbackManager.Factory.create()
//            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
            LoginManager.getInstance().registerCallback(callbackManager,
                    object : FacebookCallback<LoginResult> {
                        override fun onSuccess(loginResult: LoginResult) {
                            Log.d("LoginRegisterActivity", "Token: " + loginResult.accessToken.token);
//                            startActivity(applicationContext, )
                            handleFacebookAccessToken(loginResult.accessToken);
                        }

                        override fun onCancel() {
                            login_register_button.visibility = View.VISIBLE
                            login_register_loading.visibility = View.GONE
                            finish()
                            System.last_activity = System.LOGIN_REGISTER_ACTIVITY;

                        }

                        override fun onError(exception: FacebookException) {

                            login_register_button.visibility = View.VISIBLE
                            login_register_loading.visibility = View.GONE
                        }
                    })
        })
    }

    private fun handleFacebookAccessToken(token: AccessToken){
        Log.d("", "handleFacebookAccessToken:$token");

        var credential = FacebookAuthProvider.getCredential(token.token);
        firebaseAuth().signInWithCredential(credential)
            .addOnCompleteListener(this){ task ->
                if(task.isSuccessful){
                    val currentUser = firebaseAuth().currentUser
                    Log.d("firebase", currentUser.toString())

                    firebaseDatabase().collection("users")
                            .document(currentUser!!.email + "")
                            .get().addOnSuccessListener {

                                login_register_button.visibility = View.VISIBLE
                                login_register_loading.visibility = View.GONE
                                if (it.data != null) {
                                    User.data = it.data as HashMap<String, Any>
                                    Log.d("firebase", "curr user initiated: ${it.data.toString()}")
                                    finish()
                                    System.last_activity = System.LOGIN_REGISTER_ACTIVITY
                                } else {
                                    val builder = AlertDialog.Builder(this)
                                    builder.setTitle("New User")
                                    builder.setMessage("Your email hasn't been registered yet! Do you want to register?")
                                            .setCancelable(true)
                                            .setPositiveButton("YES") { dialog, which ->
                                                val intent = Intent(this, RegisterClientActivity::class.java)
                                                startActivity(intent)
                                                System.last_activity = System.LOGIN_REGISTER_ACTIVITY
                                                finish()
                                            }
                                            .setNegativeButton("NO"){dialog, which ->  }
                                    builder.create().show()
                                }
                            }
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RC_SIGN_IN) {
            Log.d("firebase", "sht4")
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("firebase", "Google sign in failed", e)
                // ...
            }
        }
        else{
            callbackManager?.onActivityResult(requestCode, resultCode, data);
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("firebase", "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("firebase", "signInWithCredential:success")
                    val currentUser = firebaseAuth().currentUser
                    Log.d("firebase", currentUser.toString())

                    firebaseDatabase().collection("users")
                        .document(currentUser!!.email + "")
                        .get().addOnSuccessListener {
                            if (it.data != null) {
                                User.data = it.data as HashMap<String, Any>
                                Log.d("firebase", "curr user initiated: ${it.data.toString()}")
                                Log.d("testt", "az")
                                System.last_activity = System.LOGIN_REGISTER_ACTIVITY
                                finish()
                            } else {
                                val builder = AlertDialog.Builder(this)
                                builder.setTitle("New User")
                                builder.setMessage("Your email hasn't been registered yet! Do you want to register?")
                                    .setCancelable(true)
                                    .setPositiveButton("YES") { dialog, which ->
                                        val intent = Intent(this, RegisterClientActivity::class.java)
                                        startActivity(intent)
                                        System.last_activity = System.LOGIN_REGISTER_ACTIVITY
                                        finish()
                                    }
                                    .setNegativeButton("NO"){dialog, which ->  }
                                builder.create().show()
                            }
                        }
                }
            }
    }

}
