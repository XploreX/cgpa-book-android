package com.example.cgpabook.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.example.cgpabook.utils.readFromDisk
import org.json.JSONObject


class SharedViewModel(application: Application, val state: SavedStateHandle) :
    AndroidViewModel(application) {
    var uiStuff = JSONObject()//HashMap<String, MutableLiveData<String>>()
    val filename = getApplication<Application>().filesDir.path + "/data"
    var backup = JSONObject()

    fun <T> getElement(s: String): MutableLiveData<T> {
        if (!uiStuff.has(s)) {
            when {
                state.contains(s) -> uiStuff.put(s, state.getLiveData<T>(s))
                backup.has(s) -> {
                    try {
                        uiStuff.put(s, MutableLiveData(backup.get(s) as T))
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        uiStuff.put(s, MutableLiveData<T>())
                    }
                }
                else -> uiStuff.put(s, MutableLiveData<T>())
            }
        }
        return uiStuff.get(s) as MutableLiveData<T>
    }

    init {
        try {
            readFromDisk(filename).let {
                backup = if (it != null) JSONObject(it) else JSONObject()
            }
        } catch (e: org.json.JSONException) {
            e.printStackTrace()
            backup = JSONObject()
        }
    }

    fun <T> setVal(k: String, s: T) {
        val temp = getElement<T>(k)
        state.set(k, s.toString())
        backup.put(k, s)
        temp.value = s
    }

    fun <T> getVal(s: String): T? {
        return getElement<T>(s).value
    }

    fun writeToDisk() {
        com.example.cgpabook.utils.writeToDisk(filename, backup.toString())
    }

}