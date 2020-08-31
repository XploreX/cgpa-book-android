package com.example.cgpabook.classes

import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.example.cgpabook.R

data class CollegeChooseModel(
    var name: String, var url: String,
    var id: String
) {
    private lateinit var pv: View
    lateinit var et: EditText
    lateinit var tv: TextView
    fun setParv(v: View) {
        pv = v
        tv = v.findViewById(R.id.txtcollege)
        et = v.findViewById(R.id.college_choose)
    }
}