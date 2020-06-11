package edu.bluejack19_1.KumVulanDFreelancer.fragments


import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import edu.bluejack19_1.KumVulanDFreelancer.*
import edu.bluejack19_1.KumVulanDFreelancer.firebase.ChatPeople
import edu.bluejack19_1.KumVulanDFreelancer.firebase.FirebaseUtil
import edu.bluejack19_1.KumVulanDFreelancer.recycleView.item.MessageItem
import edu.bluejack19_1.KumVulanDFreelancer.recycleView.item.PersonItem
import kotlinx.android.synthetic.main.fragment_people.*
import kotlinx.android.synthetic.main.person_item_fragment.view.*

class PeopleFragment(parent: MainActivity) : Fragment() {

    private val parent = parent

    private lateinit var userListenerRegistration: ListenerRegistration
    private var shouldInitRecyclerView = true
    private lateinit var peopleSection: Section
    private lateinit var items : List<Item>
    private var chatPeoples : List<ChatPeople>? = null
    private var currentChatCategory : String = "All"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        userListenerRegistration = FirebaseUtil.addChatPeopleListener(this.activity!!, this::updateRecyclerView)

        return inflater.inflate(R.layout.fragment_people, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        System.last_activity = System.PEOPLE_FRAGMENT
        initializeChatCategorySpinner()
    }

    private fun categoryFilter(chatPeoples: List<ChatPeople>){
        var toRet = mutableListOf<ChatPeople>()
        chatPeoples.forEach {
            Log.d("PeopleFragment", "${it.chat_id} ${it.isStarred} ${it.isArchive}")
            if(currentChatCategory == "All"){
                if(!it.isArchive){
                    toRet.add(it)
                }
            }
            if(currentChatCategory == "Starred"){
                if(it.isStarred){
                    toRet.add(it)
                }
            }
            if(currentChatCategory == "Archived"){
                if(it.isArchive){
                    toRet.add(it)
                }
            }
        }
        Log.d("PeopleFragment", toRet.size.toString())
        convertToItems(toRet)
    }

    private fun initializeChatCategorySpinner(){
        people_chat_category.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(this@PeopleFragment.chatPeoples == null) return
                var chatPeoples = this@PeopleFragment.chatPeoples
                this@PeopleFragment.currentChatCategory = people_chat_category.getItemAtPosition(position).toString()
                categoryFilter(chatPeoples!!)
                peopleSection.update(this@PeopleFragment.items)
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        FirebaseUtil.removeListener(userListenerRegistration);
        shouldInitRecyclerView = true;
    }

    private fun convertToItems(chatPeoples: List<ChatPeople>){
        var items = mutableListOf<Item>()
        chatPeoples.forEach {
            items.add(PersonItem(it, this.activity!!))
        }
        this.items = items
    }

    private fun updateRecyclerView(items: List<ChatPeople>){

        if(this@PeopleFragment.context == null) return

        this@PeopleFragment.chatPeoples = items
        fun init(){
            categoryFilter(items)
            recycler_view_people.apply {
                layoutManager = LinearLayoutManager(this@PeopleFragment.context)
                adapter = GroupAdapter<ViewHolder>().apply {
                    peopleSection = Section(this@PeopleFragment.items)
                    add(peopleSection)
                    setOnItemClickListener(onItemClick)
                }
            }
            peopleSection.update(this.items)
            shouldInitRecyclerView = false
        }
        fun updateItems(){
            categoryFilter(items!!)
            peopleSection.update(this.items)
        }

        if(shouldInitRecyclerView) init()
        else updateItems()
    }

    private val onItemClick = OnItemClickListener{ item, view ->
        if(item is PersonItem){
            ChatPageActivity.chat_id = item.people.chat_id
            ChatPageActivity.person = item.person
            ChatPageActivity.userDocRef = firebaseDatabase().collection("users").document(firebaseAuth().currentUser?.email + "")
            ChatPageActivity.otherDocRef = firebaseDatabase().collection("users").document(item.people.email)
            ChatPageActivity.chatPeople = item.people

            var intent = Intent(this.context, ChatPageActivity::class.java)
            startActivity(intent)
        }
    }



    override fun onResume() {
        super.onResume()
        if(System.last_activity != System.PEOPLE_FRAGMENT){
            shouldInitRecyclerView = true
            userListenerRegistration = FirebaseUtil.addChatPeopleListener(this.activity!!, this::updateRecyclerView)
        }
        System.last_activity = System.PEOPLE_FRAGMENT
    }
}
