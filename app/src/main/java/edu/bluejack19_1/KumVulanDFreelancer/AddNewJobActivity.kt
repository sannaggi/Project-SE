package edu.bluejack19_1.KumVulanDFreelancer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_new_job.*
import kotlinx.android.synthetic.main.activity_add_new_job.txtDeadline
import kotlinx.android.synthetic.main.activity_add_new_job.txtDescription
import kotlinx.android.synthetic.main.item_taken_job_history.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AddNewJobActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    var deadline: String = ""
    var category: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_job)

        initializeSubmitButton()
        initializeDeadlineView()
        intializeCategorySpinner()
    }

    private fun intializeCategorySpinner()  {
        ArrayAdapter.createFromResource(
            this,
            R.array.job_categories_array,
            android.R.layout.simple_spinner_item
        ) .also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        spinner.setSelection(0)
        spinner.onItemSelectedListener = this
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        category = spinner.selectedItem.toString()
        Log.d("testt", category)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun initializeDeadlineView() {
        deadline = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        val minDate = Calendar.getInstance()

        txtDeadline.minDate = minDate.timeInMillis
        txtDeadline.setOnDateChangeListener {_, year, month, day ->
            var dayString = day.toString()
            var monthString = (month + 1).toString()

            Log.d("testt", "month $monthString")
            if (day < 10) dayString = "0$dayString"
            if (month + 1 < 10) monthString = "0$monthString"
            deadline = "$dayString/$monthString/$year"
        }
    }

    private fun initializeSubmitButton() {
        btnSubmit.setOnClickListener {
            val name = txtName.text.toString()
            val description = txtDescription.text.toString()
            val price = txtPrice.text.toString()

            Log.d("testt", deadline)

            if (name.isEmpty() || !containsAlpha(name) || description.isEmpty() || !containsAlpha(name) || price.isEmpty() || price.toInt() < 10000) {
                when {
                    name.isEmpty() -> toast("Job name cannot be empty")
                    !containsAlpha(name) -> toast("Job name must contains alphabet")
                    description.isEmpty() -> toast("Job description cannot be empty")
                    !containsAlpha(name) -> toast("Job name must contains alphabet")
                    price.isEmpty() -> toast("Price cannot be empty")
                    price.toLong() < 10000 -> toast("Job price must be more than Rp. 10000")
                }
            } else {
                showLoading()
                firebaseDatabase()
                    .collection("jobs")
                    .add(generateJobData(name, description, price))
                    .addOnSuccessListener {
                        toast("Job successfully inserted")
                        hideLoading()
                        finish()
                    }
                    .addOnFailureListener {
                        hideLoading()
                    }
            }
        }
    }

    private fun generateJobData(name: String, description: String, price: String): HashMap<String, Any> {
        val data = HashMap<String, Any>()
        data.set("name", name)
        data.set("deadline", deadline)
        data.set("description", description)
        data.set("status", TakenJob.ISSUED)
        data.set("est_price", price.toInt())
        data.set("client", firebaseAuth().currentUser!!.email.toString())
        data.set("applicants", ArrayList<String>())
        data.set("category", category)

        return data
    }

    private fun toast(str: String) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show()
    }

    private fun containsAlpha(name: String): Boolean {
        for (i in name.indices) {
            if (name[i].isLetter()) return true
        }
        return false
    }

    private fun showLoading() {
        progress_loading.visibility = View.VISIBLE
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun hideLoading() {
        progress_loading.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}
