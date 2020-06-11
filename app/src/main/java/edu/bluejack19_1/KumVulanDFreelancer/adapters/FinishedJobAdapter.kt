package edu.bluejack19_1.KumVulanDFreelancer.adapters

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.xwray.groupie.kotlinandroidextensions.Item
import edu.bluejack19_1.KumVulanDFreelancer.firebase.FinishedJob
import edu.bluejack19_1.KumVulanDFreelancer.User
import edu.bluejack19_1.KumVulanDFreelancer.firebaseDatabase
import edu.bluejack19_1.KumVulanDFreelancer.recycleView.item.PostedJobItem
import edu.bluejack19_1.KumVulanDFreelancer.recycleView.item.TakenJobItem

object FinishedJobAdapter{

    val documentRef = firebaseDatabase().collection("finished_jobs")
    lateinit var postedJobQuery: Query
    lateinit var takenJobQuery: Query

    fun fetchPostedJobData(doc: DocumentSnapshot, context: Context): List<Item>{
        var items = mutableListOf<Item>()
        postedJobQuery.startAfter(doc).limit(5).get().addOnSuccessListener {
            documents ->
            for (document in documents){
                items.add(
                        PostedJobItem(FinishedJob(document.data as HashMap<String, Any>, document.id), context)
                )
            }
        }

        return items
    }

    fun fetchTakenJobData(doc: DocumentSnapshot, context: Context) : List<Item>{
        var items = mutableListOf<Item>()
        takenJobQuery.startAfter(doc).limit(5).get().addOnSuccessListener {
            documents ->
            for (document in documents){
                items.add(
                        TakenJobItem(FinishedJob(document.data as HashMap<String, Any>, document.id), context)
                )
            }
        }

        return items
    }

    fun initializePostedJobListener(context: Context, onListen : (List<FinishedJob>) -> Unit) : ListenerRegistration {
        return postedJobQuery.addSnapshotListener{
            snapshots, e ->
            if(e != null) return@addSnapshotListener

            var items = mutableListOf<FinishedJob>()
            snapshots!!.forEach {
                items.add(FinishedJob(it.data as HashMap<String, Any>, it.id)
                )
                Log.d("POSTEDJOB", "Data : ${it.data}")
            }

            onListen(items)
        }
    }

    fun initializeTakenJobListener(context: Context, onListen: (List<FinishedJob>) -> Unit) : ListenerRegistration{
        return takenJobQuery.addSnapshotListener{
            snapshots, e ->
            if(e != null) return@addSnapshotListener

            var items = mutableListOf<FinishedJob>()
            snapshots!!.forEach {
                items.add(FinishedJob(it.data as HashMap<String, Any>, it.id))
            }

            onListen(items)
        }
    }
}