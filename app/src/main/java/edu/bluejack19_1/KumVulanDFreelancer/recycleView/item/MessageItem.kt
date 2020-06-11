package edu.bluejack19_1.KumVulanDFreelancer.recycleView.item

import android.content.Context
import android.view.Gravity
import android.widget.FrameLayout
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import edu.bluejack19_1.KumVulanDFreelancer.firebase.Message
import edu.bluejack19_1.KumVulanDFreelancer.R
import edu.bluejack19_1.KumVulanDFreelancer.firebaseAuth
import kotlinx.android.synthetic.main.item_text_message.*
import java.text.SimpleDateFormat

class MessageItem(val message: Message, val context: Context) : Item(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView_message_text.text = message.message
        setTimeText(viewHolder)
        setMessageRootGravity(viewHolder)
    }

    fun setTimeText(viewHolder: ViewHolder){
        val dateFormat = SimpleDateFormat
                .getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
        viewHolder.textView_message_time.text = dateFormat.format(message.date)
    }

    private fun setMessageRootGravity(viewHolder: ViewHolder){
        if(!message.email.equals(firebaseAuth().currentUser?.email)){
            viewHolder.message_root.apply {
                setBackgroundResource(R.drawable.rect_round_grey)
                val lParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.START)
                this.layoutParams = lParams
            }
        }
        else{
            viewHolder.message_root.apply {
                setBackgroundResource(R.drawable.rect_round_primary_color)
                val lParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.END)
                this.layoutParams = lParams
            }
        }
    }

    override fun getLayout(): Int = R.layout.item_text_message

    override fun isSameAs(other: com.xwray.groupie.Item<*>?): Boolean {
        if(other !is MessageItem) return false
        if(this.message != other.message) return false

        return true
    }

    override fun equals(other: Any?): Boolean {
        return isSameAs(other as? MessageItem)
    }
}