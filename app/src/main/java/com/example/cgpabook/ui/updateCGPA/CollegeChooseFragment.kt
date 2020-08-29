package com.example.cgpabook.ui.updateCGPA

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.example.cgpabook.R
import com.example.cgpabook.activity.MySingleton
import com.example.cgpabook.activity.SearchActivity
import com.example.cgpabook.classes.CollegeChooseModel
import com.example.cgpabook.ui.SharedViewModel
import com.example.cgpabook.utils.addparams
import com.example.cgpabook.utils.dashBoardButton
import com.example.cgpabook.utils.progressBarDestroy
import com.example.cgpabook.utils.progressBarInit
import org.json.JSONObject


class CollegeChoose : Fragment() {
    private var idlist: ArrayList<EditText> = ArrayList()
    private lateinit var viewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_college_choose, container, false)
        val ll = v.findViewById<LinearLayout>(R.id.llcollegeselect)
        val domainurl = "http://cgpa-book.herokuapp.com"
        viewModel = activity?.run { ViewModelProviders.of(this)[SharedViewModel::class.java] }
            ?: throw Exception("Invalid Activity")
        val arrayList: ArrayList<CollegeChooseModel> =
            ArrayList(
                listOf(
                    CollegeChooseModel(
                        "Select College",
                        "$domainurl/academia/college-list?"
                    ),
                    CollegeChooseModel(
                        "Select Course",
                        "$domainurl/academia/course-list?"
                    ),
                    CollegeChooseModel(
                        "Select Branch",
                        "$domainurl/academia/branch-list?"
                    ),
                    CollegeChooseModel(
                        "Select Semester",
                        "$domainurl/academia/semester-list?"
                    )
                )
            )

        idlist.clear()
        val body = JSONObject()
        val requiredbody = ArrayList<String>(listOf("college", "course", "branch", "semester"))
        val queue = MySingleton.getInstance(context as Context)
        for (j in 0 until arrayList.size) {
            val i = arrayList[j]
            val b = inflater.inflate(R.layout.college_choose_frame, ll, false)
            b.findViewById<TextView>(R.id.txtcollege).text = i.name
            ll.addView(b)
            val editText = b.findViewById<EditText>(R.id.college_choose)
            if (j != 0 && b.findViewById<EditText>(R.id.college_choose).text.toString() != "")
                editText.isEnabled = false
            editText.setOnClickListener {
                val intent = Intent(context, SearchActivity::class.java)
                var url = i.url
                for (temp in 0 until j)
                    if (ll.findViewById<EditText>(temp).text.split('(')[0].trim() != "") {
                        body.put(
                            requiredbody[temp],
                            ll.findViewById<EditText>(temp).text.split('(')[0].trim()
                        )
                    }
                body.put("ignorecase", "true")
                url = addparams(url, body)
                println(url)
                for (k in j + 1 until arrayList.size) {
                    ll.findViewById<EditText>(k).setText("")
                    ll.findViewById<EditText>(k).isEnabled = false
                }
                val progressBar = progressBarInit(v)
                val jsonObjectRequest =
                    JsonArrayRequest(
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
                            print(JSONObject(String(it.networkResponse.data)))
                            print(it.networkResponse.allHeaders)
                            print(it.networkResponse.statusCode)
                            Toast.makeText(
                                context,
                                "Network/Server Issue. Please try again",
                                Toast.LENGTH_SHORT
                            ).show()
                            progressBarDestroy(v, progressBar)
                        }
                    )
                queue?.addToRequestQueue(jsonObjectRequest)
            }
            b.findViewById<EditText>(R.id.college_choose).id = j
            idlist.add(editText)
        }
        dashBoardButton(v)
        fun validateerror(): Boolean {
            var flag: Boolean = true
            for (i in 0 until arrayList.size) {
                val temp = ll.findViewById<EditText>(i)
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
            for (j in 0 until idlist.size) {
                if (j == requestCode) {
                    if (data != null) {
                        idlist[j].setText(data.getStringExtra("selected")!!.split("(")[0].trim())
                        if (j + 1 < idlist.size)
                            idlist[j + 1].isEnabled = true
                    } else {
                        Toast.makeText(context, "Some error occurred", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}