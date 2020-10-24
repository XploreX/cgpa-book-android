package com.example.cgpabook.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.cgpabook.utils.HelperStrings
import com.example.cgpabook.utils.MySingleton

class BroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        //TODO("BroadcastReceiver.onReceive() is not implemented")
        val url = HelperStrings.url + "/academia/user/"
        var vol = MySingleton.getInstance(context)
    }

}
