package com.example.cgpabook.ui.updateCGPA

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.android.volley.Request
import com.android.volley.Response
import com.example.cgpabook.R
import com.example.cgpabook.activity.MySingleton
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
        initviewmodel()
        var url = "http://cgpa-book.herokuapp.com/academia/semester?"
        val body = JSONObject()
        val temp = ArrayList(
            listOf("college", "course", "branch", "semester")
        )
        for (i in temp) {
            body.put(i, viewModel.getVal<String>(i))
        }
        body.put("ignorecase", "true")
        url = addparams(url, body)
        println(url)
//        for (key in grades_schema.keys()) grades.add(key)
        val pb = progressBarInit(v)
        val queue = MySingleton.getInstance(context as Context)
        val jsonObject = JsonObjectRequestCached(Request.Method.GET, url, null, Response.Listener {
            if (!errorhandler(it)) {
                val subdata = it.getJSONArray("subjects")
                for (i in 0 until subdata.length()) {
                    val temp = subdata.getJSONObject(i)
                    subjects_data.add(
                        SubjectsData(
                            temp.getString("subject"),
                            temp.getString("subjectCode"),
                            temp.getInt("credits")
                        )
                    )
                }
                updateSub()
                progressBarDestroy(v, pb)
            }
        }, Response.ErrorListener {
            if (!errorhandler(it))
                Toast.makeText(context, "Could not acquire grades", Toast.LENGTH_SHORT).show()
            progressBarDestroy(v, pb)
        })
        queue?.addToRequestQueue(jsonObject)
//        setFragmentResultListener("requestKey") { key, bundle ->
//            // We use a String here, but any type that can be put in a Bundle is supported
//            val result = bundle.getString("bundleKey")
//            // Do something with the result...
//        }
        grades_schema = JSONObject()
        grades = ArrayList(listOf("O", "A+", "A", "B+", "B", "C", "F"))
        for (i in 0 until grades.size) {
            if (grades[i] == "F")
                grades_schema.put("F", 0)
            else
                grades_schema.put(grades[i], 10 - i)
        }
        llv = v.findViewById(R.id.llv_grade_button)
        Runnable {
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
        }.run()

//        addButton(llh,"2")
//        addButton(llh,"3")
        return v
        TODO("use api for grades_scheme")
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
        index++
        calculatecgpa((v as Button).text.toString())
        if (index == subjects_data.size) {
            goToProfile()
        } else
            updateSub()
    }

    private fun calculatecgpa(grade: String) {
        val point: Float = grades_schema[grade].toString().toFloat()
        viewModel.setVal(
            "semcreds",
            (viewModel.getVal<Float>("semcreds")
                ?: 0.0F) + (point * (viewModel.getVal<Int>("credits"))!!)
        )
        viewModel.setVal(
            "totcreds",
            (viewModel.getVal<Float>("totcreds") ?: 0.0F) + (viewModel.getVal<Int>("credits")!!)
        )
        println(viewModel.getVal<Float>("semcreds"))
        println(viewModel.getVal<Float>("totcreds"))
        println(point)
        viewModel.setVal(
            "cgpa",
            viewModel.getVal<Float>("semcreds")?.div(viewModel.getVal<Float>("totcreds") as Float)
        )
    }

    private fun initviewmodel() {
        viewModel.getElement<String>("subject").observe(this, Observer {
            view!!.findViewById<TextView>(R.id.txt_subname).text = "Subject Name: $it"
        })
        viewModel.getElement<Int>("credits").observe(this, Observer {
            view!!.findViewById<TextView>(R.id.txt_credits).text = "Credits: $it"
        })
        viewModel.getElement<Int>("subdataindex").observe(this, Observer {
            view!!.findViewById<TextView>(R.id.txt_subjectsleft).text =
                "Subjects Left: " + (subjects_data.size - index - 1)
        })
        viewModel.getElement<Float>("cgpa").observe(this, Observer {
            view!!.findViewById<TextView>(R.id.txt_cgpa).text = "CGPA: $it"
        })
    }

    private fun updateSub() {
        viewModel.setVal("subject", subjects_data[index].subName)
        viewModel.setVal("credits", subjects_data[index].credits)
        viewModel.setVal("subdataindex", index)
    }
}