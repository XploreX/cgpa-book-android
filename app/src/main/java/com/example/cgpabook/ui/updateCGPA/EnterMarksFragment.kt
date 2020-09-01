package com.example.cgpabook.ui.updateCGPA

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
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

    private lateinit var llv: LinearLayout
    private lateinit var viewModel: SharedViewModel
    private lateinit var grades: ArrayList<String>
    private lateinit var llh: LinearLayout
    private lateinit var gradesSchema: JSONObject
    private val semcreds = ArrayList<Float>()
    private val totalcreds = ArrayList<Int>()
    private var cgpa = MutableLiveData<Float>()
    private var subjectsData: ArrayList<SubjectsData> = ArrayList()
    private var index: Int = 0
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
                    subjectsData.add(
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
        //TODO("use api for grades_scheme")
        gradesSchema = JSONObject()
        grades = ArrayList(listOf("O", "A+", "A", "B+", "B", "C", "F"))
        for (i in 0 until grades.size) {
            if (grades[i] == "F")
                gradesSchema.put("F", 0)
            else
                gradesSchema.put(grades[i], 10 - i)
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

        v.findViewById<ImageView>(R.id.btn_undo).setOnClickListener {
            if (index > 0) {
                index--
                semcreds.removeAt(index)
                totalcreds.removeAt(index)
                updatecgpa()
                updateSub()
            }
        }
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
    private fun buttononclick(v: View) {
        index++
        Runnable {
            calculatecgpa((v as Button).text.toString())
        }.run()
        if (index == subjectsData.size) {
            goToProfile()
        } else
            updateSub()
    }

    private fun calculatecgpa(grade: String) {
        val point: Float = gradesSchema[grade].toString().toFloat()
        semcreds.add(point * (viewModel.getVal<Int>("credits"))!!)
        totalcreds.add(viewModel.getVal<Int>("credits")!!)
        updatecgpa()
    }

    private fun updatecgpa() {
        var semcred = 0.0F
        var denom = 0.0F
        for (i in 0 until semcreds.size) {
            semcred += semcreds[i]
            denom += totalcreds[i]
        }
        if (denom == 0.0F)
            cgpa.value = 0.0F
        else
            cgpa.value = semcred / denom
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
                "Subjects Left: " + (subjectsData.size - index - 1)
        })
        cgpa.observe(this, Observer {
            if (it == 0.0F)
                view!!.findViewById<TextView>(R.id.txt_cgpa).text = ""
            else
                view!!.findViewById<TextView>(R.id.txt_cgpa).text = "CGPA: $it"
        })
    }

    private fun updateSub() {
        viewModel.setVal("subject", subjectsData[index].subName)
        viewModel.setVal("credits", subjectsData[index].credits)
        viewModel.setVal("subdataindex", index)
    }
}