package edu.bluejack19_1.KumVulanDFreelancer.firebase

import edu.bluejack19_1.KumVulanDFreelancer.TakenJob
import java.text.SimpleDateFormat

class Job(data: HashMap<String, Any>, id: String){

    val id = id
    val name = FinishedJob.string(data[TakenJob.NAME])
    val client = FinishedJob.string(data[TakenJob.CLIENT])
    val deadline = FinishedJob.string(data[TakenJob.DEADLINE])
    val description = FinishedJob.string(data[TakenJob.DESCRIPTION])
    val price = FinishedJob.convertPrice(data[TakenJob.EST_PRICE] as Long)
    val originalPrice = data[TakenJob.EST_PRICE] as Long
    val freelancer = FinishedJob.string(data[TakenJob.FREELANCER])
    val originalDeadline = SimpleDateFormat("dd/MM/yyy").parse(deadline)
    val status = FinishedJob.string(data[TakenJob.STATUS])
    val category = FinishedJob.string(data["category"])
    val applicants = data[TakenJob.APPLICANTS] as ArrayList<String>?
}