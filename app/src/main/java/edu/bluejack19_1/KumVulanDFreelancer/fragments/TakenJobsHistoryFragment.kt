package edu.bluejack19_1.KumVulanDFreelancer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.Section
import edu.bluejack19_1.KumVulanDFreelancer.R

class TakenJobsHistoryFragment : Fragment() {

    private lateinit var takenJobListenerRegistration: ListenerRegistration
    private var shouldInitRecyclerView = true
    private lateinit var jobSection: Section

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_taken_jobs_history, container, false)
    }
}
