package com.example.cgpabook.classes

import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.example.cgpabook.R

data class CollegeChooseModel(var name: String, var url: String) {
    lateinit var pv: View
    lateinit var et: EditText
    lateinit var tv: TextView
    fun setParv(v: View) {
        pv = v
        et = v.findViewById(R.id.txtcollege)
        tv = v.findViewById(R.id.college_choose)
    }
}