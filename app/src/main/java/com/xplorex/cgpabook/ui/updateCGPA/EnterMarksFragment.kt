package com.xplorex.cgpabook.ui.updateCGPA

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.Response
import com.xplorex.cgpabook.R
import com.xplorex.cgpabook.classes.SubjectsData
import com.xplorex.cgpabook.receiver.ConnectivityBroadcastReceiver
import com.xplorex.cgpabook.ui.SharedViewModel
import com.xplorex.cgpabook.utils.*
import org.json.JSONObject

class EnterMarksFragment : Fragment() {

    private lateinit var viewModel: SharedViewModel
    private lateinit var gradesSchema: JSONObject

    // arrays are required for functionality of undo button
    private val userCreditsArray = ArrayList<Double>()
    private val totalCreditsArray = ArrayList<Double>()

    // subjects info
    private var subjectsData: ArrayList<SubjectsData> = ArrayList()

    //some other required global variables
    private var sgpa = MutableLiveData<Double>()
    private var index: Int = 0

    // all the live data objects which need not be persistent, hence, not in view model
    private val subjectsLiveData = MutableLiveData<String>()
    private val anim_ltr = MutableLiveData<Boolean>()
    private val creditsLiveData = MutableLiveData<Double>()
    private val subjectsLeftLiveData = MutableLiveData<Int>()

    // broadcast
    private val broadcastReceiver = ConnectivityBroadcastReceiver()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // fragment on create
        val v = inflater.inflate(R.layout.fragment_enter_marks, container, false)
        viewModel = getViewModel()
        dashBoardButton(v.findViewById(R.id.relativeLayoutParent))
        initViewModelObservers()

        // init variables
        val volleyQueue = context?.let { MySingleton.getInstance(it) }
        val pullToRefreshLayout = v.findViewById<SwipeRefreshLayout>(R.id.pulltorefresh)

        // undo button
        v.findViewById<ImageView>(R.id.btn_undo).setOnClickListener {
            if (index > 0) {
                anim_ltr.value = false
                index--
                userCreditsArray.removeAt(index)
                totalCreditsArray.removeAt(index)
                updateSgpa()
                updateSub()
            }
        }

        // setup the semester api query
        var url = HelperStrings.url + "/academia/semester?"
        val requestParams = JSONObject()
        val viewModelRequired = ArrayList(
            listOf(
                HelperStrings.college,
                HelperStrings.course,
                HelperStrings.branch,
                HelperStrings.semester
            )
        )
        for (i in viewModelRequired) {
            requestParams.put(i, viewModel.getVal<String>(i))
        }
        requestParams.put("ignorecase", "true")

        // update the url with requestParams
        url = addparams(url, requestParams)

        // debug: println(url)

        // initiate the request
        val pb = progressBarInit(v)
        val jsonObject = JsonObjectRequestCached(Request.Method.GET, url, null, Response.Listener {

            // get all the subjects data
            if (!it.has(HelperStrings.subjects)) {
                showToast("Subject data not found", Toast.LENGTH_SHORT)
                viewModel.setVal(HelperStrings.semester, null)
                progressBarDestroy(v, pb)
                pullToRefreshLayout.isRefreshing = false
                goToProfile()
            } else {
                val subjectsDataAll = it.getJSONArray(HelperStrings.subjects)

                // convert from json to object
                for (i in 0 until subjectsDataAll.length()) {
                    subjectsData.add(
                        SubjectsData(
                            subjectsDataAll.getJSONObject(i)
                        )
                    )
                }
                updateSub()
                progressBarDestroy(v, pb)
                pullToRefreshLayout.isRefreshing = false
            }

        }, Response.ErrorListener {
            errorHandler(it)
            progressBarDestroy(v, pb)
            pullToRefreshLayout.isRefreshing = false
        })
        volleyQueue?.addToRequestQueue(jsonObject)
        // swipe
        pullToRefreshLayout.setOnRefreshListener {
            userCreditsArray.clear()
            totalCreditsArray.clear()
            subjectsData.clear()
            index = 0
            volleyQueue?.addToRequestQueue(jsonObject)
        }
        initGradeButtons(v)

        return v
    }

    private fun initGradeButtons(v: View) {
        //TODO("use api for grades_scheme")

        // layout dimensions
        val horizontalButtons = 4

        // make a similar grades object which will be returned from api
        gradesSchema = JSONObject()
        val grades: ArrayList<String> = ArrayList(listOf("O", "A+", "A", "B+", "B", "C", "P", "F"))
        for (i in 0 until grades.size) {
            if (grades[i] == "F")
                gradesSchema.put("F", 0)
            else
                gradesSchema.put(grades[i], 10 - i)
        }

        // make the buttons on UI
        val llv: LinearLayout = v.findViewById(R.id.llv_grade_button)
        var llh: LinearLayout? = null
        for (index in 0 until grades.size) {

            // create a horizontal linear layout when (index % horizontalbuttons) == 0
            if (index % horizontalButtons == 0) {
                llh = createllh(llv)
            }

            // add button to horizontal layout
            llh?.let { llh ->

                addButton(llh, grades[index], R.drawable.grade_buttons, R.color.black) { view ->
                    gradesOnClick(
                        view
                    )
                }

            }
        }
    }

    private fun gradesOnClick(v: View) {
        anim_ltr.value = true
        // increment the index
        index++

        // update cgpa simultaneously
        Runnable { calculateSgpa((v as Button).text.toString()) }.run()

        // update subjects data in UI
        updateSub()
    }

    private fun calculateSgpa(grade: String) {
        val gradePoint: Double = gradesSchema[grade].toString().toDouble()

        creditsLiveData.value?.let { credits ->
            userCreditsArray.add(gradePoint * (credits))
            totalCreditsArray.add(credits)
        }
        updateSgpa()
    }

    private fun updateSgpa() {
        var semCreditsSum = 0.0
        var totalCreditsSum = 0.0

        for (i in 0 until userCreditsArray.size) {
            semCreditsSum += userCreditsArray[i]
            totalCreditsSum += totalCreditsArray[i]
        }

        if (totalCreditsSum == 0.0)
            sgpa.value = 0.0
        else
            sgpa.value = semCreditsSum / totalCreditsSum
    }

    private fun initViewModelObservers() {

        subjectsLiveData.observe(viewLifecycleOwner, Observer {
            requireView().findViewById<TextSwitcher>(R.id.txt_subname).setText("Subject Name: $it ")
        })

        creditsLiveData.observe(viewLifecycleOwner, Observer {
            requireView().findViewById<TextView>(R.id.txt_credits).text = "Credits: $it"
        })

        subjectsLeftLiveData.observe(viewLifecycleOwner, Observer {
            requireView().findViewById<TextView>(R.id.txt_subjectsleft).text =
                "Subjects Left: " + (subjectsData.size - index - 1)
        })

        sgpa.observe(viewLifecycleOwner, Observer {
            if (it == 0.0)
                requireView().findViewById<TextView>(R.id.txt_cgpa).text = ""
            else
                requireView().findViewById<TextView>(R.id.txt_cgpa).text =
                    "SGPA: ${String.format("%.2f", it)}"
        })

        anim_ltr.observe(viewLifecycleOwner, Observer {
            val viewIdList = ArrayList(listOf(R.id.txt_subname))
            for (viewId in viewIdList) {
                val view = requireView().findViewById<TextSwitcher>(viewId)
                if (it) {
                    view.inAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_ltr_enter)
                    view.outAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_ltr_exit)
                } else {
                    view.inAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_rtl_enter)
                    view.outAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_rtl_exit)
                }
            }

        })

    }

    private fun updateSub() {
        if (index >= subjectsData.size) {
            noMoreSubs()
        } else {
            // update the value with updated index
            subjectsLiveData.value =
                subjectsData[index].subName + " (${subjectsData[index].subCode})"
            creditsLiveData.value = subjectsData[index].credits
            subjectsLeftLiveData.value = index
        }
    }

    private fun noMoreSubs() {

        // set sync state as false so profile page doesn't download before updating
        context?.let { setSyncState(it, false, viewModel) }

        // create a sem data object
        val semData = JSONObject()

        // put the relevant sem data required
        semData.put(HelperStrings.sgpa, sgpa.value)

        // get the required data from view model
        val semNo = viewModel.getVal<String>(HelperStrings.semester).toString()

        // update sem data all
        val allSemData = viewModel.getVal<JSONObject>(HelperStrings.semdata) ?: JSONObject()
        allSemData.put(semNo, semData.toString())

        // update cgpa as well
        var cgpa: Double = 0.0
        for (currentKey in allSemData.keys()) {
            val semData = JSONObject(allSemData.getString(currentKey))
            cgpa += (semData.get(HelperStrings.sgpa).toString().toDouble())
        }
        cgpa /= allSemData.length()

        viewModel.setVal(HelperStrings.cgpa, cgpa)
        viewModel.setVal(HelperStrings.semdata, allSemData)
        viewModel.writeToDisk()
        requireActivity().supportFragmentManager.popBackStack(
            getString(R.string.menu_select_college),
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, ShowResultSGPA(sgpa.value))
            .addToBackStack(getString(R.string.menu_update_cgpa)).commit()
    }
}