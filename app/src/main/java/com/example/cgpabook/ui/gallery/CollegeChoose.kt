package com.example.cgpabook.ui.gallery

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.example.cgpabook.R
import com.example.cgpabook.activity.MySingleton
import com.example.cgpabook.activity.SearchActivity
import com.example.cgpabook.utils.JSONArrayRequestCached
import java.util.*
import kotlin.collections.ArrayList


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CollegeChoose.newInstance] factory method to
 * create an instance of this fragment.
 */
class CollegeChoose : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var idlist: ArrayList<EditText> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_college_choose, container, false)
        val ll = v.findViewById<LinearLayout>(R.id.llcollegeselect)
        val arrayList: ArrayList<String> =
            ArrayList(Arrays.asList("Select College", "Select Course", "Select Semester"))
        idlist.clear()
        val queue = MySingleton.getInstance(context as Context)
        for (j in 0 until arrayList.size) {
            val i = arrayList.get(j)
            val b = inflater.inflate(R.layout.college_choose_button, ll, false)
            b.findViewById<TextView>(R.id.txtcollege).text = i
            ll.addView(b)
            val editText = b.findViewById<EditText>(R.id.college_choose)
            editText.setOnClickListener {
                val intent = Intent(context, SearchActivity::class.java)
                val url = "http://cgpa-book.herokuapp.com/academia/college-list"
                val progressDialog = ProgressDialog(context)
                progressDialog.show()
                val jsonObjectRequest: JsonArrayRequest =
                    JSONArrayRequestCached(
                        Request.Method.GET, url, null,
                        Response.Listener { it ->
                            val arrayList = ArrayList<String>()
                            for (i in 0 until it.length())
                                arrayList.add(it.get(i).toString())
                            println(arrayList)
                            println(j)
                            intent.putStringArrayListExtra("List", arrayList)
                            progressDialog.hide()
                            startActivityForResult(intent, j)
                        },
                        Response.ErrorListener { error ->
                            // TODO: Handle error
                        }
                    )
                queue?.addToRequestQueue(jsonObjectRequest)
            }
            idlist.add(editText)
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
                        idlist.get(j).setText(data.getStringExtra("selected"))
                    } else {
                        println("data=null")
                    }
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CollegeChoose.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CollegeChoose().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}