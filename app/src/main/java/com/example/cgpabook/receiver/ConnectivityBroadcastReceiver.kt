package com.example.cgpabook.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.example.cgpabook.utils.*
import org.json.JSONObject

// This method is called when the ConnectivityBroadcastReceiver is receiving an Intent broadcast.
class ConnectivityBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        // Since SharedViewModel can't be accessed, We are importing it manually for our required data
        var viewModelData: JSONObject = JSONObject()
        val filename = context.applicationContext.filesDir.path + "/data"
        readFromDisk(filename)?.let { it ->
            viewModelData = JSONObject(it)
        }

        // Get the sync state
        val synced =
            if (viewModelData.has(HelperStrings.synced)) viewModelData.getBoolean(HelperStrings.synced) else false

        // check if network connectivity is there and it is not synced before
        if (isNetworkConnected(context) && !synced) {

            // These are the strings that we need to send to the server
            val dataToSend = ArrayList<String>(
                listOf(
                    HelperStrings.college,
                    HelperStrings.branch,
                    HelperStrings.course,
                    HelperStrings.semdata,
                    HelperStrings.unlocked
                )
            )

            if (!viewModelData.has(HelperStrings.tokenId)) {
                // Token is not generated maybe, can be rectified by logging again
                Toast.makeText(context, "Please Login Again", Toast.LENGTH_SHORT).show()
            }

            // Generate the object to send
            val sendObj = JSONObject()
            for (key in dataToSend) {
                if (viewModelData.has(key))
                    sendObj.put(key, viewModelData.get(key))
            }
            //TODO: update url if required
            val url = HelperStrings.url + "/user/gpa-data"

            // Debug
            println("sendObj: $sendObj")

            // Init volley queue and request
            val volleyQueue = MySingleton.getInstance(context)
            val request =
                object : JsonObjectRequest(Request.Method.POST, url, sendObj, Response.Listener {

                    // if receive success, update it in viewmodel
                    if (it.has("success") && it.getString("success") == "true")
                        viewModelData.put(HelperStrings.synced, true)
                    else
                        viewModelData.put(HelperStrings.synced, false)
                    writeToDisk(filename, viewModelData.toString())

                }, Response.ErrorListener {

                    viewModelData.put(HelperStrings.synced, false)
                    writeToDisk(filename, viewModelData.toString())

                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val params: MutableMap<String, String> = HashMap()
                        params["Authorization"] =
                            "Bearer ${viewModelData.getString(HelperStrings.tokenId)}"
                        return params
                    }
                }

            volleyQueue?.addToRequestQueue(request)
        }
    }


}

