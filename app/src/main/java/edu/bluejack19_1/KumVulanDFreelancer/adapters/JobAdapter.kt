package edu.bluejack19_1.KumVulanDFreelancer.adapters

import android.content.Context
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.kotlinandroidextensions.Item
import edu.bluejack19_1.KumVulanDFreelancer.TakenJob
import edu.bluejack19_1.KumVulanDFreelancer.firebase.Job
import edu.bluejack19_1.KumVulanDFreelancer.firebaseDatabase

object JobAdapter {
    val docRef = firebaseDatabase().collection("jobs").whereEqualTo("status", TakenJob.ISSUED)

    fun initializeJobListener(context: Context, onListen: (List<Job>) -> Unit) : ListenerRegistration{
        return docRef.addSnapshotListener{
            documents, e ->
            if(e != null) return@addSnapshotListener

            var items = mutableListOf<Job>()
            documents!!.forEach {
                items.add(Job(it.data as HashMap<String, Any>, it.id))
            }

            onListen(items)
        }
    }
}