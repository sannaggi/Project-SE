package edu.bluejack19_1.KumVulanDFreelancer.adapters

import android.content.Context
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.kotlinandroidextensions.Item
import edu.bluejack19_1.KumVulanDFreelancer.ChatPageActivity
import edu.bluejack19_1.KumVulanDFreelancer.firebaseAuth
import edu.bluejack19_1.KumVulanDFreelancer.firebase.Message
import edu.bluejack19_1.KumVulanDFreelancer.recycleView.item.MessageItem
import java.util.Calendar

object MessagingAdapter {

    lateinit var messageDocRef : DocumentReference
    fun initialize(docRef: DocumentReference, context: Context, onListen : (List<Item>) -> Unit) : ListenerRegistration{
        messageDocRef = docRef
        return initializeMessageListener(context, onListen)
    }

    fun initializeMessageListener(context: Context, onListen : (List<Item>) -> Unit) : ListenerRegistration{
        return messageDocRef.addSnapshotListener{ snapshots, e ->
            if(e != null){
                Log.e("FIRESTORE", "Chat People Listener error", e)
                return@addSnapshotListener
            }
            if(snapshots!!["chats"] == null){
                Log.e("FIRESTORE", "Chats null")
                return@addSnapshotListener
            }
            val items = snapshots!!["chats"] as ArrayList<Map<String, Any>>
            var retItems = mutableListOf<Item>()
            items.forEach {
                var stamp = (it["date"] as Timestamp).toDate()

                val temp = Message(
                        stamp,
                        it["email"].toString(),
                        it["message"].toString()
                )
                retItems.add(MessageItem(temp, context))
            }
            onListen(retItems);
        }
    }

    fun sendMessage(message : String){
        var addChatToArrayMap = HashMap<String, Any>()
        addChatToArrayMap.put("chats", FieldValue.arrayUnion(
                Message(
                        Calendar.getInstance().time,
                        firebaseAuth().currentUser?.email.toString(),
                        message
                )
        )
        )
        ChatPageActivity.last_message = message

        messageDocRef.update(addChatToArrayMap)
    }
}