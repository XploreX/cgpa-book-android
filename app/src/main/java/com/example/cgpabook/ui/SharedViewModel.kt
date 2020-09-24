package com.example.cgpabook.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import org.json.JSONObject
import java.io.*


class SharedViewModel(application: Application, private val state: SavedStateHandle) :
    AndroidViewModel(application) {
    private var uiStuff = JSONObject()//HashMap<String, MutableLiveData<String>>()
    private val filename = getApplication<Application>().filesDir.path + "/data"
    private var backup = JSONObject()

    fun <T> getElement(s: String): MutableLiveData<T> {
        if (!uiStuff.has(s)) {
            when {
                state.contains(s) -> uiStuff.put(s, state.getLiveData<T>(s))
                backup.has(s) -> uiStuff.put(s, MutableLiveData<T>(backup.get(s) as T))
                else -> uiStuff.put(s, MutableLiveData<T>())
            }
        }
        return uiStuff.get(s) as MutableLiveData<T>
    }

    init {
        readFromDisk()
    }

    fun <T> setVal(k: String, s: T) {
        val temp = getElement<T>(k)
        state.set(k, s)
        backup.put(k, s)
        temp.value = s
    }

    fun <T> getVal(s: String): T? {
        return getElement<T>(s).value
    }

    fun writeToDisk() {
        try {
            val fos: FileOutputStream =
                FileOutputStream(File(filename), false)
            if (backup != null) {
                fos.write(backup.toString().toByteArray())
            }
            fos.close()
            true
        } catch (fileNotFound: FileNotFoundException) {
            false
        } catch (ioException: IOException) {
            false
        }
        backup.toString()
    }

    private fun readFromDisk() {
        val file = File(filename)
        if (file.exists()) {
            try {
                val fis: FileInputStream =
                    FileInputStream(file)//getApplication<Application>().openFileInput(filename)
                val isr = InputStreamReader(fis)
                val bufferedReader = BufferedReader(isr)
                val sb = StringBuilder()
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                fis.close()
                backup = JSONObject(sb.toString())
            } catch (fileNotFound: FileNotFoundException) {
                null
            } catch (ioException: IOException) {
                null
            }
        }
        //backup=Gson().fromJson(FileReader(filename),JSONObject::class.java)
    }
}