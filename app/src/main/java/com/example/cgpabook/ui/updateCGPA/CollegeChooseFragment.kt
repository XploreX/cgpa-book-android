package com.example.cgpabook.ui.updateCGPA

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
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
import org.json.JSONObject


class CollegeChoose : Fragment() {
    private lateinit var viewModel: SharedViewModel
    private lateinit var allFields: ArrayList<CollegeChooseModel>
    private var volleyQueue: MySingleton? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Fragment OnCreate
        val v = inflater.inflate(R.layout.fragment_college_choose, container, false)
        viewModel = getViewModel()
        dashBoardButton(v)

        //Init Variables
        initViewModelObservers()
        val ll = v.findViewById<LinearLayout>(R.id.llcollegeselect)
        val domainUrl = HelperStrings.url
        volleyQueue = MySingleton.getInstance(context as Context)
        allFields = ArrayList(
            listOf(
                CollegeChooseModel(
                    "Select College",
                    "$domainUrl/academia/college-list?",
                    HelperStrings.college
                ),
                CollegeChooseModel(
                    "Select Course",
                    "$domainUrl/academia/course-list?",
                    HelperStrings.course
                ),
                CollegeChooseModel(
                    "Select Branch",
                    "$domainUrl/academia/branch-list?",
                    HelperStrings.branch
                ),
                CollegeChooseModel(
                    "Select Semester",
                    "$domainUrl/academia/semester-list?",
                    HelperStrings.semester
                )
            )
        )

        val setupDisplayVariables = ArrayList<String>(
            listOf(
                HelperStrings.college,
                HelperStrings.course,
                HelperStrings.branch,
                HelperStrings.semester
            )
        )

        // Initialize the unlocked Fields variable
        if (viewModel.getVal<Int>(HelperStrings.unlocked) == null) viewModel.setVal(
            HelperStrings.unlocked,
            0
        )


        // Next Button Click
        v.findViewById<ImageView>(R.id.btnnext).setOnClickListener {
            val flag = validateFields()
            if (flag) {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, EnterMarksFragment())
                    .addToBackStack(getString(R.string.menu_update_cgpa)).commit()
            }
        }

        // setup Fields
        for (index in 0 until allFields.size) {

            // init Field
            val field = allFields[index]
            val b = inflater.inflate(R.layout.college_choose_frame, ll, false)
            field.initView(b)

            // This label is used to update the respective fields
            viewModel.getElement<String>(field.label).observe(viewLifecycleOwner,
                Observer { t -> field.editText.setText(t) }
            )

            field.textView.text = field.name

            // add View to Linear Layout
            ll.addView(b)

            field.editText.setOnClickListener {
                field.editText.error = null

                // setup the Request Body
                val requestParams = JSONObject()
                var url = field.url
                for (temp in 0 until index) {

                    // removing the abbreviations in Brackets as the api requires college without them
                    val fieldValueFiltered = allFields[temp].editText.text.split('(')[0].trim()

                    // adding to requestParams
                    if (fieldValueFiltered != "") {
                        requestParams.put(setupDisplayVariables[temp], fieldValueFiltered)
                    }
                }
                requestParams.put("ignorecase", "true")

                // update the url
                url = addparams(url, requestParams)

                // debug: println(url)

                // show the progressbar
                val progressBar = progressBarInit(v)
                val fieldInternetRequest =
                    JsonArrayRequestCached(Request.Method.GET, url, null,
                        Response.Listener {

                            // convert JSONArray to ArrayList
                            val arrayList = ArrayList<String>()
                            for (i in 0 until it.length()) arrayList.add(it.get(i).toString())

                            // debug: println(arrayList)
                            // debug: println(index)

                            // destroy the progressbar
                            progressBarDestroy(v, progressBar)

                            // send the intent
                            val intent = Intent(context, SearchActivity::class.java)
                            intent.putStringArrayListExtra("List", arrayList)
                            startActivityForResult(intent, index)

                        },
                        Response.ErrorListener {
                            errorHandler(it); progressBarDestroy(
                            v,
                            progressBar
                        )
                        }
                    )
                volleyQueue?.addToRequestQueue(fieldInternetRequest)
            }
        }
        return v
    }

    private fun initViewModelObservers() {

        // update the unlocked fields when changed
        viewModel.getElement<Int>(HelperStrings.unlocked)
            .observe(viewLifecycleOwner, Observer { max ->

                for (j in 0 until allFields.size) {
                    allFields[j].editText.isEnabled = (j <= max)
                }
                for (reset in max until allFields.size) {
                    viewModel.setVal(allFields[reset].label, "")
                }

            })

    }

    private fun validateFields(): Boolean {
        var flag = true
        for (index in 0 until allFields.size) {

            val fieldEditText = allFields[index].editText

            // if field unfilled
            if (fieldEditText.text.toString() == "") {
                fieldEditText.error = "Required"; flag = false
            }

            // clear the error flag
            else {
                fieldEditText.error = null
            }

        }
        return flag
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //debug: println(resultCode)
        //debug: println(requestCode)

        // if success
        if (resultCode == Activity.RESULT_OK) {

            // requestCode tells which field was clicked
            if (data != null) {
                val concernedField = allFields[requestCode]
                data.getStringExtra("selected")?.let { recvString ->

                    val filteredRecvString = recvString.split("(")[0].trim()
                    if (concernedField.editText.text.toString() != filteredRecvString) {

                        // update viewmodel
                        viewModel.setVal(concernedField.label, filteredRecvString)

                        //update the unlocked variable
                        viewModel.setVal(HelperStrings.unlocked, requestCode + 1)
                    }
                }
            } else {
                showToast("Some unknown error occurred", Toast.LENGTH_SHORT)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStop() {

        // if progress bar was visible, clear the not touch flags
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        // clear the volley queue
        volleyQueue?.getRequestQueue()?.cancelAll { true }

        super.onStop()
    }
}