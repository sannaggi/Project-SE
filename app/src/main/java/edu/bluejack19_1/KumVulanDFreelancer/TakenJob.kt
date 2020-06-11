package edu.bluejack19_1.KumVulanDFreelancer

class TakenJob(name: String, client: String, deadline: String, description: String, est_price: Int, freelancer: String, status: String, id: String) {
    val name = name
    val client = client
    val deadline = deadline
    val description = description
    val est_price = est_price
    val freelancer = freelancer
    var status = status
    val id = id

    companion object {
        val NAME = "name"
        val CLIENT = "client"
        val DEADLINE = "deadline"
        val DESCRIPTION = "description"
        val EST_PRICE = "est_price"
        val FREELANCER = "freelancer"
        val STATUS = "status"
        val APPLICANTS = "applicants"
        val OTHER_PARTY_EMAIL = "otherPartyEmail"
        val YOUR_ROLE = "yourRole"
        val ID = "id"
        val ON_GOING = "On going"
        val WAITING_CLIENT = "Waiting client"
        val WAITING_FREELANCER = "Waiting freelancer"
        val FINISHED = "Finished"
        val ISSUED = "Issued"
        val REJECTED = "Rejected"
    }
}