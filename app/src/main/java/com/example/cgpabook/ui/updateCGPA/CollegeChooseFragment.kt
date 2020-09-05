package com.example.cgpabook.ui.updateCGPA

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.android.volley.Request
import com.android.volley.Response
import com.example.cgpabook.R
import com.example.cgpabook.activity.SearchActivity
import com.example.cgpabook.classes.CollegeChooseModel
import com.example.cgpabook.ui.SharedViewModel
import com.example.cgpabook.utils.*
import com.google.android.material.navigation.NavigationView
import org.json.JSONObject


class CollegeChoose : Fragment() {
    //    private var idlist: ArrayList<EditText> = ArrayList()
    private lateinit var viewModel: SharedViewModel
    private lateinit var arrayList: ArrayList<CollegeChooseModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_college_choose, container, false)
        val ll = v.findViewById<LinearLayout>(R.id.llcollegeselect)
        val domainurl = "http://cgpa-book.herokuapp.com"
        viewModel = getViewModel()
        arrayList =
            ArrayList(
                listOf(
                    CollegeChooseModel(
                        "Select College",
                        "$domainurl/academia/college-list?",
                        HelperStrings.college
                    ),
                    CollegeChooseModel(
                        "Select Course",
                        "$domainurl/academia/course-list?"
                        , HelperStrings.course
                    ),
                    CollegeChooseModel(
                        "Select Branch",
                        "$domainurl/academia/branch-list?"
                        , HelperStrings.branch
                    ),
                    CollegeChooseModel(
                        "Select Semester",
                        "$domainurl/academia/semester-list?"
                        , HelperStrings.semester
                    )
                )
            )

        val requiredbody = ArrayList<String>(
            listOf(
                HelperStrings.college,
                HelperStrings.course,
                HelperStrings.branch,
                HelperStrings.semester
            )
        )
        val queue = MySingleton.getInstance(context as Context)
        viewModel.getElement<String>(HelperStrings.college).observe(this, Observer {
            activity!!.findViewById<NavigationView>(R.id.nav_view).getHeaderView(0)
                .findViewById<TextView>(R.id.txt_email).text =
                it
        })

        fun makelayouts() {
            for (j in 0 until arrayList.size) {
                val i = arrayList[j]
                val b = inflater.inflate(R.layout.college_choose_frame, ll, false)
                i.setParv(b)
                viewModel.getElement<String>(arrayList[j].id).observe(this, Observer { t ->
                    arrayList[j].et.setText(t)
                })
                i.tv.text = i.name
                ll.addView(b)
                val et = i.et
                val value = viewModel.getVal<String>(arrayList[j].id)
                if (j != 0 && value.toString().trim() == "") {
                    et.isEnabled = false
                }
                et.setOnClickListener {
                    val body = JSONObject()
                    val intent = Intent(context, SearchActivity::class.java)
                    var url = i.url
                    for (temp in 0 until j)
                        if (arrayList[temp].et.text.split('(')[0].trim() != "") {
                            body.put(
                                requiredbody[temp],
                                arrayList[temp].et.text.split('(')[0].trim()
                            )
                        }
                    body.put("ignorecase", "true")
                    url = addparams(url, body)
                    println(url)
                    for (k in j + 1 until arrayList.size) {
                        viewModel.setVal(arrayList[k].id, "")
                        arrayList[k].et.isEnabled = false
                    }
                    val progressBar = progressBarInit(v)
                    val jsonObjectRequest =
                        JsonArrayRequestCached(
                            Request.Method.GET, url, null,
                            Response.Listener {
                                val arrayList = ArrayList<String>()
                                for (i in 0 until it.length())
                                    arrayList.add(it.get(i).toString())
                                println(arrayList)
                                println(j)
                                intent.putStringArrayListExtra("List", arrayList)
//                            progressDialog.hide()
                                progressBarDestroy(v, progressBar)
                                startActivityForResult(intent, j)
                            },
                            Response.ErrorListener {
                                if (!errorhandler(it)) {
                                    Toast.makeText(
                                        context,
                                        it.message.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                progressBarDestroy(v, progressBar)
                            }
                        )
                    queue?.addToRequestQueue(jsonObjectRequest)
                }
            }
        }
        Runnable {
            makelayouts()
        }.run()
        Runnable {
            dashBoardButton(v)
        }.run()
        fun validateerror(): Boolean {
            var flag = true
            for (i in 0 until arrayList.size) {
                val temp = arrayList[i].et
                if (temp.text.toString() == "") {
                    temp.error = "Required"
                    flag = false
                } else {
                    temp.error = null
                }
            }
            return flag
        }
        v.findViewById<ImageView>(R.id.btnnext).setOnClickListener {

            val flag = validateerror()
            if (flag)
                activity!!.supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, EnterMarksFragment())
                    .addToBackStack(getString(R.string.menu_update_cgpa)).commit()
        }

        return v
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        println(resultCode)
        println(requestCode)
        if (resultCode == Activity.RESULT_OK) {
            for (j in 0 until arrayList.size) {
                if (j == requestCode) {
                    if (data != null) {
                        viewModel.setVal(
                            arrayList[j].id,
                            data.getStringExtra("selected")!!.split("(")[0].trim()
                        )
                        if (j + 1 < arrayList.size)
                            arrayList[j + 1].et.isEnabled = true
                    } else {
                        Toast.makeText(context, "Some error occurred", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}