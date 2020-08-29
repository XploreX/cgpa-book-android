package com.example.cgpabook.ui.updateCGPA

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.android.volley.Request
import com.android.volley.Response
import com.example.cgpabook.R
import com.example.cgpabook.classes.SubjectsData
import com.example.cgpabook.ui.SharedViewModel
import com.example.cgpabook.utils.*
import org.json.JSONObject

class EnterMarksFragment : Fragment() {

    lateinit var llv: LinearLayout
    private lateinit var viewModel: SharedViewModel
    lateinit var grades: ArrayList<String>
    lateinit var llh: LinearLayout
    lateinit var grades_schema: JSONObject
    var subjects_data: ArrayList<SubjectsData> = ArrayList()
    var index: Int = 0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_enter_marks, container, false)
        viewModel = activity?.run { ViewModelProviders.of(this)[SharedViewModel::class.java] }
            ?: throw Exception("Invalid Activity")
        dashBoardButton(v)
        var url = "http://cgpa-book.herokuapp.com/semester"
        val body = JSONObject()
        body.put("type", "1")
        url = addparams(url, body)
        JsonObjectRequestCached(Request.Method.GET, url, null, Response.Listener {
            if (it.getString("success") == "true") {
                grades_schema = it.getJSONObject("grades")
                val subdata = it.getJSONArray("subjects")
                for (i in 0 until subdata.length()) {
                    val temp = subdata.getJSONObject(i)
                    subjects_data.add(
                        SubjectsData(
                            i + 1,
                            temp.getString("name"),
                            temp.getInt("credits")
                        )
                    )
                }
                for (key in grades_schema.keys()) grades.add(key)
            } else
                Toast.makeText(context, "Could not acquire grades", Toast.LENGTH_SHORT).show()
        }, Response.ErrorListener {
            Toast.makeText(context, "Could not acquire grades", Toast.LENGTH_SHORT).show()
        })
//        setFragmentResultListener("requestKey") { key, bundle ->
//            // We use a String here, but any type that can be put in a Bundle is supported
//            val result = bundle.getString("bundleKey")
//            // Do something with the result...
//        }
        grades = ArrayList(listOf("O", "A+", "A", "B+", "B", "C", "F"))
        llv = v.findViewById(R.id.llv_grade_button)
        for (i in 0 until grades.size) {
            if (i % 4 == 0) {
                llh = createllh(llv)
            }
            addButton(llh, grades[i], R.drawable.grade_buttons, R.color.black) { m ->
                buttononclick(
                    m
                )
            }
        }
//        addButton(llh,"2")
//        addButton(llh,"3")
        return v
    }

    //    fun addButton(llh: ViewGroup, text: String) {
//        val v = LayoutInflater.from(context).inflate(R.layout.grade_buttons, llh, false) as Button
//        v.text = text
//        v.setOnClickListener {
//            buttononclick()
//        }
//        llh.addView(v, v.layoutParams)
//    }
    fun buttononclick(v: View) {
        Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show()
        if (index == subjects_data.size) {
            return
        }
        view!!.findViewById<TextView>(R.id.txt_subjectsleft).text =
            (subjects_data.size - 1 - index).toString()
        view!!.findViewById<TextView>(R.id.txt_subname).text = subjects_data[index].subName
        view!!.findViewById<TextView>(R.id.txt_credits).text =
            subjects_data[index].credits.toString()
        viewModel.cgpa.value = grades_schema.getInt((v as Button).text.toString()).toFloat()
    }
}