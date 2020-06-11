package edu.bluejack19_1.KumVulanDFreelancer.recycleView.item

import android.content.Context
import android.graphics.Color
import android.util.Log
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldValue
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import edu.bluejack19_1.KumVulanDFreelancer.*
import edu.bluejack19_1.KumVulanDFreelancer.firebase.ChatPeople
import edu.bluejack19_1.KumVulanDFreelancer.firebase.FetchUser
import kotlinx.android.synthetic.main.person_item_fragment.*
import java.lang.Exception


class PersonItem(val people: ChatPeople, private val context: Context)
    :Item(){
    lateinit var person : FetchUser
    override fun bind(viewHolder: ViewHolder, position: Int) {
        firebaseDatabase().collection("users").document(people.email + "")
                .get().addOnSuccessListener {
                    if (it.data != null) {
                        person = FetchUser(it.data as HashMap<String, Any>)
                        Log.d("firebase", "curr user initiated: ${it.data.toString()}")
                        System.last_activity = System.LOGIN_REGISTER_ACTIVITY

                        init(viewHolder)
                    }
                }
                .addOnFailureListener{
                    Log.d("PersonItem", "PersonItem Bind Error")
                }
    }

    private fun init(viewHolder: ViewHolder){

        viewHolder.textView_name.text = person.getName();
        viewHolder.textView_last_message.text = people.last_message;
        viewHolder.btnArchive.setTextColor(Color.DKGRAY)
        viewHolder.btnStarred.setTextColor(Color.DKGRAY)
        if(people.isArchive) viewHolder.btnArchive.setTextColor(Color.GREEN)
        if(people.isStarred) viewHolder.btnStarred.setTextColor(Color.rgb(245, 183, 38))
        viewHolder.btnArchive.setOnClickListener {
            if(!people.isArchive) viewHolder.btnArchive.setTextColor(Color.GREEN)
            else viewHolder.btnArchive.setTextColor(Color.DKGRAY)
            var toUser = HashMap<String, Any>()
            toUser.put("chat_people", FieldValue.arrayRemove(
                    ChatPeople(
                            people.chat_id,
                            people.email,
                            people.isArchive,
                            people.isStarred,
                            people.last_message
                    )))

            firebaseDatabase().collection("users").document(firebaseAuth().currentUser?.email.toString()).update(toUser)
            people.isArchive = !people.isArchive
            toUser.put("chat_people", FieldValue.arrayUnion(
                    ChatPeople(
                            people.chat_id,
                            people.email,
                            people.isArchive,
                            people.isStarred,
                            people.last_message
                    )))
            firebaseDatabase().collection("users").document(firebaseAuth().currentUser?.email.toString()).update(toUser)
        }
        viewHolder.btnStarred.setOnClickListener {
            if(!people.isStarred) viewHolder.btnStarred.setTextColor(Color.rgb(245, 183, 38))
            else viewHolder.btnStarred.setTextColor(Color.DKGRAY)
            var toUser = HashMap<String, Any>()
            toUser.put("chat_people", FieldValue.arrayRemove(
                    ChatPeople(
                            people.chat_id,
                            people.email,
                            people.isArchive,
                            people.isStarred,
                            people.last_message
                    )))

            firebaseDatabase().collection("users").document(firebaseAuth().currentUser?.email.toString()).update(toUser)
            people.isStarred = !people.isStarred
            toUser.put("chat_people", FieldValue.arrayUnion(
                    ChatPeople(
                            people.chat_id,
                            people.email,
                            people.isArchive,
                            people.isStarred,
                            people.last_message
                    )))
            firebaseDatabase().collection("users").document(firebaseAuth().currentUser?.email.toString()).update(toUser)
        }
        try {
            firebaseStorageReference()
                    .child(person.getProfileImage())
                    .downloadUrl
                    .addOnSuccessListener{
                        uri -> Glide.with(context)
                            .load(uri)
                            .into(viewHolder.imageView_profile_picture)
                    }
                    .addOnFailureListener {
                        Log.d("firebase", it.toString())
                    }
        } catch (e: Exception) {
            Log.d("firebase", "invalid image loading intercepted")
        }


    }

    override fun getLayout() = R.layout.person_item_fragment
}
