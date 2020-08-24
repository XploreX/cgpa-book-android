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
import com.android.volley.Request
import com.android.volley.Response
import com.example.cgpabook.R
import com.example.cgpabook.activity.MySingleton
import com.example.cgpabook.activity.SearchActivity
import com.example.cgpabook.classes.CollegeChooseModel
import com.example.cgpabook.utils.JsonArrayRequestCached
import com.example.cgpabook.utils.dashBoardButton
import com.example.cgpabook.utils.progressBarDestroy
import com.example.cgpabook.utils.progressBarInit
import org.json.JSONObject


class CollegeChoose : Fragment() {
    private var idlist: ArrayList<EditText> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_college_choose, container, false)
        val ll = v.findViewById<LinearLayout>(R.id.llcollegeselect)
        val arrayList: ArrayList<CollegeChooseModel> =
            ArrayList(
                listOf(
                    CollegeChooseModel(
                        "Select College",
                        "http://cgpa-book.herokuapp.com/academia/college-list"
                    ),
                    CollegeChooseModel(
                        "Select Course",
                        "http://cgpa-book.herokuapp.com/academia/course-list"
                    ),
                    CollegeChooseModel(
                        "Select Branch",
                        "http://cgpa-book.herokuapp.com/academia/branch-list"
                    ),
                    CollegeChooseModel(
                        "Select Semester",
                        "http://cgpa-book.herokuapp.com/academia/semester-list"
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
            editText.setOnClickListener {
                val intent = Intent(context, SearchActivity::class.java)
                val url = i.url
                for (temp in 0 until requiredbody.size)
                    body.put(
                        requiredbody[temp],
                        ll.findViewById<EditText>(temp).text.split('(')[0].trim()
                    )
                body.put("ignorecase", "true")
                println(body)
                for (k in j + 1 until arrayList.size)
                    ll.findViewById<EditText>(k).setText("")
                val progressBar = progressBarInit(v)
                val jsonObjectRequest =
                    JsonArrayRequestCached(
                        Request.Method.GET, url, body,
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
        v.findViewById<ImageView>(R.id.btnnext).setOnClickListener {
            var flag: Boolean = true
            for (i in 0 until arrayList.size) {
                var temp = ll.findViewById<EditText>(i)
                if (temp.text.toString() == "") {
                    temp.error = "Required"
                    flag = false
                } else {
                    temp.error = null
                }
            }
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
                        println(data.getStringExtra("selected"))
                        idlist[j].setText(data.getStringExtra("selected")!!.split("(")[0].trim())
                    } else {
                        Toast.makeText(context, "Some error occurred", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}