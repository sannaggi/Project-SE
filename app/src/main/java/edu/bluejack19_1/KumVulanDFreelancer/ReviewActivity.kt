package edu.bluejack19_1.KumVulanDFreelancer

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import edu.bluejack19_1.KumVulanDFreelancer.adapters.MoreReviewAdapter
import edu.bluejack19_1.KumVulanDFreelancer.recycleView.item.ReviewItem
import kotlinx.android.synthetic.main.activity_review.*

class ReviewActivity : AppCompatActivity() {

    private lateinit var reviewListenerRegistration: ListenerRegistration
    private lateinit var reviewSection: Section
    private var shouldInitRecyclerView = true
    private var reviews : List<Review>? = null
    private lateinit var items: List<Item>
    private var currentSortCategory = "None"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        var docRef = firebaseDatabase().collection("users").document(intent.extras!!.get("ID").toString())
        reviewListenerRegistration = MoreReviewAdapter.initializeReviewListener(docRef, this, this::updateRecyclerView)

        initializeReviewSpinner()
    }

    private fun initializeReviewSpinner(){

        review_sort_spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long){
                if(this@ReviewActivity.reviews == null) return
                var reviews = this@ReviewActivity.reviews
                currentSortCategory = review_sort_spinner.getItemAtPosition(position).toString()
                sort(reviews!!)
                Log.d("SPINNER", "Spinner selected = ${review_sort_spinner.getItemAtPosition(position)}")
            }

            override fun onNothingSelected(parent: AdapterView<*>){

            }
        }
    }

    private fun sort(reviews: List<Review>){
        if(reviews == null) return
        var reviews = reviews
        if(currentSortCategory.equals("Name")){
            reviews = reviews?.sortedBy {
                it.name
            }
        }
        else if(currentSortCategory.equals("Rating")){
            reviews = reviews?.sortedByDescending{
                it.rating.toLong()
            }
        }
        convertToItems(reviews!!)
        reviewSection.update(items)
    }

    private fun convertToItems(reviews: List<Review>){
        var items = mutableListOf<Item>()
        reviews.forEach{
            items.add(ReviewItem(it, this))
        }

        this.items = items
    }

    private fun updateRecyclerView(reviews: List<Review>){
        this.reviews = reviews
        fun init(){
            convertToItems(reviews)
            review_recycler_view.apply {
                layoutManager = LinearLayoutManager(this@ReviewActivity)
                adapter = GroupAdapter<ViewHolder>().apply {
                    reviewSection = Section(items)
                    add(reviewSection)
                }
            }
            shouldInitRecyclerView = false
        }
        fun updateItems(){
            sort(reviews)
        }

        if(shouldInitRecyclerView) init()
        else updateItems()
    }


}
