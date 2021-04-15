package com.xplorex.cgpabook.classes

import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.xplorex.cgpabook.R

data class CollegeChooseModel(
    var name: String, var url: String,
    var label: String
) {
    private lateinit var pv: View
    lateinit var editText: EditText
    lateinit var textView: TextView
    fun initView(v: View) {
        pv = v
        textView = v.findViewById(R.id.txtcollege)
        editText = v.findViewById(R.id.college_choose)
    }
}