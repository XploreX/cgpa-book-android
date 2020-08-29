package com.example.cgpabook.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    val Name = MutableLiveData<String>()
    val cgpa = MutableLiveData<Float>()
    val uistuff = HashMap<Int, MutableLiveData<String>>()
    var index = 0

    init {
        cgpa.value = 0.0F
    }

    fun newUi(s: String): Int {
        if (uistuff[index] != null)
            return -1
        uistuff[index] = (MutableLiveData(s))
        index++
        return uistuff.size - 1
    }
}