package com.example.cgpabook.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.json.JSONObject

class SharedViewModel : ViewModel() {
    private val uiStuff = JSONObject()//HashMap<String, MutableLiveData<String>>()

    fun <T> getElement(s: String): MutableLiveData<T> {
        if (!uiStuff.has(s))
            uiStuff.put(s, MutableLiveData<T>())
        return uiStuff.get(s) as MutableLiveData<T>
    }

    fun <T> setVal(k: String, s: T) {
        val temp = getElement<T>(k)
        temp.value = s
    }

    fun <T> getVal(s: String): T? {
        return getElement<T>(s).value
    }

}