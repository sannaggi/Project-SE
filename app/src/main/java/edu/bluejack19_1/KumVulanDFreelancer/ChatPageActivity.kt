package edu.bluejack19_1.KumVulanDFreelancer

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import edu.bluejack19_1.KumVulanDFreelancer.firebase.FetchUser
import edu.bluejack19_1.KumVulanDFreelancer.firebase.FirebaseUtil
import edu.bluejack19_1.KumVulanDFreelancer.adapters.MessagingAdapter
import edu.bluejack19_1.KumVulanDFreelancer.firebase.ChatPeople
import kotlinx.android.synthetic.main.activity_chat_page.*
import java.lang.Exception

class ChatPageActivity : AppCompatActivity() {

    companion object{
        lateinit var chat_id: String
        lateinit var person: FetchUser
        lateinit var chatPeople: ChatPeople
        lateinit var last_message: String
        lateinit var userDocRef : DocumentReference
        lateinit var otherDocRef : DocumentReference
        val user_email = firebaseAuth().currentUser?.email + ""
    }

    private lateinit var messagesListenerRegistration: ListenerRegistration
    private lateinit var chatSection: Section
    private var shouldInitRecyclerView = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_page)

        onFirstOpen()
    }

    override fun onDestroy() {
        super.onDestroy()
        FirebaseUtil.removeListener(messagesListenerRegistration)
        shouldInitRecyclerView = true
    }

    private fun onFirstOpen(){
        initProfileImage()
        initProfileName()

        val docRef = firebaseDatabase().collection("chat_messages").document(chat_id)
        messagesListenerRegistration = MessagingAdapter.initialize(docRef, this, this::updateRecyclerView)

        initSendButton()
    }

    private fun initSendButton(){
        chat_page_send_button.setOnClickListener {
            MessagingAdapter.sendMessage(chat_page_chatbox.text.toString())
            updateLastMessage()
            chat_page_chatbox.setText("")
        }
    }

    private fun updateLastMessage(){
        var toUser = HashMap<String, Any>()
        toUser.put("chat_people", FieldValue.arrayUnion(
                ChatPeople(
                        chat_id,
                        chatPeople.email,
                        chatPeople.isArchive,
                        chatPeople.isStarred,
                        last_message
                )))

        userDocRef.update(toUser)
        toUser.put("chat_people", FieldValue.arrayRemove(
                ChatPeople(
                        chat_id,
                        chatPeople.email,
                        chatPeople.isArchive,
                        chatPeople.isStarred,
                        chatPeople.last_message
                )))

        userDocRef.update(toUser)

        var toOther = HashMap<String, Any>()
        toOther.put("chat_people", FieldValue.arrayUnion(
                ChatPeople(
                        chat_id,
                        user_email,
                        chatPeople.isArchive,
                        chatPeople.isStarred,
                        last_message
                )))

        otherDocRef.update(toOther)
        toOther.put("chat_people", FieldValue.arrayRemove(
                ChatPeople(
                        chat_id,
                        user_email,
                        chatPeople.isArchive,
                        chatPeople.isStarred,
                        chatPeople.last_message
                )))
        otherDocRef.update(toOther)

        chatPeople.last_message = last_message
    }

    private fun updateRecyclerView(items: List<Item>){
        fun init(){
            chat_page_recycler_view.apply {
                layoutManager = LinearLayoutManager(this@ChatPageActivity)
                adapter = GroupAdapter<ViewHolder>().apply {
                    chatSection = Section(items)
                    add(chatSection)
                }
            }
            shouldInitRecyclerView = false
        }
        fun updateItems(){
            chatSection.update(items)

        }

        if(shouldInitRecyclerView) init()
        else updateItems()

        chat_page_recycler_view.scrollToPosition(chat_page_recycler_view.adapter!!.itemCount - 1)
    }

    private fun initProfileImage(){
        try {
            firebaseStorageReference()
                    .child(person.getProfileImage())
                    .downloadUrl
                    .addOnSuccessListener{
                        uri -> Glide.with(this)
                            .load(uri)
                            .into(chat_page_profile_image)
                    }
                    .addOnFailureListener {
                        Log.d("firebase", it.toString())
                    }
        } catch (e: Exception) {
            Log.d("firebase", "invalid image loading intercepted")
        }
    }

    private fun initProfileName(){
        findViewById<TextView>(R.id.chat_page_profile_name).text = person.getName()
    }
}
