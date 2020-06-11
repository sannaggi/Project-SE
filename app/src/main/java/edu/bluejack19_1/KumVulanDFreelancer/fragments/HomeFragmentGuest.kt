package edu.bluejack19_1.KumVulanDFreelancer.Fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import edu.bluejack19_1.KumVulanDFreelancer.*
import kotlinx.android.synthetic.main.fragment_home_guest.*

class HomeFragmentGuest(main: MainActivity) : Fragment() {

    val main = main

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_guest, container, false)
    }

    override fun onResume() {
        super.onResume()

        Log.d("testt", System.last_activity)
        if(System.last_activity == System.LOGIN_REGISTER_ACTIVITY && User.data!!.isNotEmpty()) {
            main.loginFromHome()
        }

        System.last_activity = System.HOME_FRAGMENT
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginRegisterBtn.setOnClickListener{
            val intent = Intent(this.context, LoginRegisterActivity::class.java)

            startActivity(intent)
        }
    }
}