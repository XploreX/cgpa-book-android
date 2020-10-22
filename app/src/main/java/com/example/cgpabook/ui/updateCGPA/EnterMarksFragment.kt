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
import com.android.volley.Request
import com.android.volley.Response
import com.example.cgpabook.R
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
    private val semcreds = ArrayList<Double>()
    private val totalcreds = ArrayList<Int>()
    private var sgpa = MutableLiveData<Double>()
    private var subjectsData: ArrayList<SubjectsData> = ArrayList()
    private var index: Int = 0
    private val subjectsView = MutableLiveData<String>()
    private val creditsView = MutableLiveData<Int>()
    private val subjectsLeftView = MutableLiveData<Int>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_enter_marks, container, false)
        viewModel = getViewModel()
        dashBoardButton(v)
        initviewmodel()
        var url = HelperStrings.url + "/academia/semester?"
        val body = JSONObject()
        val temp = ArrayList(
            listOf(
                HelperStrings.college,
                HelperStrings.course,
                HelperStrings.branch,
                HelperStrings.semester
            )
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
            val semData = JSONObject()
            semData.put(HelperStrings.sgpa, sgpa.value)
            viewModel.setVal(HelperStrings.cgpa, sgpa.value)
            val semNo = viewModel.getVal<String>(HelperStrings.semester).toString()
            val array = viewModel.getVal<JSONObject>(HelperStrings.semdata) ?: JSONObject()
            array.put(semNo, semData)
            viewModel.setVal(HelperStrings.semdata, array)
            goToProfile()
        } else
            updateSub()
    }

    private fun calculatecgpa(grade: String) {
        val point: Double = gradesSchema[grade].toString().toDouble()
        semcreds.add(point * (creditsView.value)!!)
        totalcreds.add(creditsView.value!!)
        updatecgpa()
    }

    private fun updatecgpa() {
        var semcred = 0.0
        var denom = 0.0
        for (i in 0 until semcreds.size) {
            semcred += semcreds[i]
            denom += totalcreds[i]
        }
        if (denom == 0.0)
            sgpa.value = 0.0
        else
            sgpa.value = semcred / denom
    }

    private fun initviewmodel() {
        subjectsView.observe(viewLifecycleOwner, Observer {
            requireView().findViewById<TextView>(R.id.txt_subname).text = "Subject Name: $it"
        })
        creditsView.observe(viewLifecycleOwner, Observer {
            requireView().findViewById<TextView>(R.id.txt_credits).text = "Credits: $it"
        })
        subjectsLeftView.observe(viewLifecycleOwner, Observer {
            requireView().findViewById<TextView>(R.id.txt_subjectsleft).text =
                "Subjects Left: " + (subjectsData.size - index - 1)
        })
        sgpa.observe(viewLifecycleOwner, Observer {
            if (it == 0.0)
                requireView().findViewById<TextView>(R.id.txt_cgpa).text = ""
            else
                requireView().findViewById<TextView>(R.id.txt_cgpa).text =
                    "CGPA: ${String.format("%.2f", it)}"
        })
    }

    private fun updateSub() {
        subjectsView.value = subjectsData[index].subName
        creditsView.value = subjectsData[index].credits
        subjectsLeftView.value = index
    }
}