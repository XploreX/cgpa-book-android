package com.example.cgpabook.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.example.cgpabook.utils.readFromDisk
import org.json.JSONObject

// AndroidViewModel is extended for application context in SharedViewModel
class SharedViewModel(application: Application, private val state: SavedStateHandle) :
    AndroidViewModel(application) {
    private var uiStuff = JSONObject()
    private val filename = getApplication<Application>().filesDir.path + "/data"
    private var backup = JSONObject()

    // getElement is used to get the mutableLiveData object(used when you want to observe stuff)
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

    // setVal is used to update the value of MutableLiveData object
    fun <T> setVal(k: String, s: T) {
        val temp = getElement<T>(k)
        try {
            state.set(k, s)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        backup.put(k, s)
        temp.value = s
    }

    // getVal is used to get the value of MutableLiveData
    fun <T> getVal(s: String): T? {
        return getElement<T>(s).value
    }

    // for persistence, call this function
    // only things set with setVal will be persisted
    fun writeToDisk() {
        com.example.cgpabook.utils.writeToDisk(filename, backup.toString())
    }

}