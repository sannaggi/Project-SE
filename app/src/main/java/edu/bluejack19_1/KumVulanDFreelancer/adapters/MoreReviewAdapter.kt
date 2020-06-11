package edu.bluejack19_1.KumVulanDFreelancer.adapters

import android.content.Context
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ListenerRegistration
import edu.bluejack19_1.KumVulanDFreelancer.Review

object MoreReviewAdapter{

    fun initializeReviewListener(docRef : DocumentReference, context: Context, onListen: (List<Review>) -> Unit) : ListenerRegistration {
        return docRef.addSnapshotListener{
            snapshots, e ->
            if(e != null) return@addSnapshotListener

            var items = snapshots!!["reviews"] as ArrayList<HashMap<String, Any>>
            var toRet = mutableListOf<Review>()
            items.forEach {
                toRet.add(Review(
                        it["name"].toString(),
                        it["profile_image"].toString(),
                        it["rating"] as Number,
                        it["review"].toString(),
                        it["id"].toString()
                ))
            }

            onListen(toRet)
        }
    }
}