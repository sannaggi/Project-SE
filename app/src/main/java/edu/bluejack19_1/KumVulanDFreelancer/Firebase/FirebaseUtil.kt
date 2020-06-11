package edu.bluejack19_1.KumVulanDFreelancer.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.kotlinandroidextensions.Item
import edu.bluejack19_1.KumVulanDFreelancer.firebaseAuth
import edu.bluejack19_1.KumVulanDFreelancer.firebaseDatabase
import edu.bluejack19_1.KumVulanDFreelancer.recycleView.item.PersonItem

object FirebaseUtil {

    private val currentUserDocRef: DocumentReference
            = firebaseDatabase().collection("users").document(firebaseAuth().currentUser?.email.toString())

    fun addChatPeopleListener(context: Context, onListen: (List<ChatPeople>) -> Unit): ListenerRegistration {
        return currentUserDocRef.addSnapshotListener{
            snapshots, e ->
            if(e != null){
                Log.e("FIRESTORE", "Chat People Listener error", e)
                return@addSnapshotListener
            }
            if(snapshots!!["chat_people"] == null){
                return@addSnapshotListener
            }
            val items = snapshots!!["chat_people"] as ArrayList<Map<String, Any>>
            var retItems = mutableListOf<ChatPeople>()
            items.forEach {
                val temp = ChatPeople(
                        it["chat_id"].toString(),
                        it["email"].toString(),
                        it["archive"] as Boolean,
                        it["starred"] as Boolean,
                        it["last_message"].toString())
                retItems.add(temp)
            }

            onListen(retItems.asReversed());
        }
    }

    fun removeListener(listener: ListenerRegistration) = listener.remove()
}