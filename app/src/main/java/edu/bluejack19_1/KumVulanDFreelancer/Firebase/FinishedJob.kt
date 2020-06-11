package edu.bluejack19_1.KumVulanDFreelancer.firebase

import android.widget.TextView
import edu.bluejack19_1.KumVulanDFreelancer.TakenJob
import edu.bluejack19_1.KumVulanDFreelancer.firebaseDatabase
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class FinishedJob(data: HashMap<String, Any>, id : String){
    val id = id
    val name = string(data[TakenJob.NAME])
    val client = string(data[TakenJob.CLIENT])
    val deadline = string(data[TakenJob.DEADLINE])
    val description = string(data[TakenJob.DESCRIPTION])
    val price = convertPrice(data[TakenJob.EST_PRICE] as Long)
    val originalPrice = data[TakenJob.EST_PRICE] as Long
    val freelancer = string(data[TakenJob.FREELANCER])
    val originalDeadline = SimpleDateFormat("dd/MM/yyy").parse(deadline)
    val status = string(data[TakenJob.STATUS])
    val category = FinishedJob.string(data["category"])
    var isReviewed = data["reviewed"] as Boolean?
    var isRated = data["rated"] as Boolean?

    companion object {
        val dateFormat = SimpleDateFormat
                .getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)

        fun string(item: Any?) : String{
            return item.toString()
        }

        fun convertPrice(price: Long) : String{
            var price = price
            var str = ""
            var count = 0
            while(price > 0){
                if(count == 3) str += "."
                str += (price % 10).toString()
                price /= 10
            }
            return str.reversed()
        }

        fun fetchName(textView: TextView, email: String){
            var name = ""
            firebaseDatabase().collection("users")
                    .document(email + "")
                    .get().addOnSuccessListener{
                        if(it.data != null){
                            name = it.data?.get("name").toString()
                            textView.text = name
                        }
            }
        }
    }
}