package com.example.cgpabook.utils

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class MySingleton private constructor(private val ctx: Context) {
    private var requestQueue: RequestQueue?

    fun getRequestQueue(): RequestQueue? {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx.applicationContext)
        }
        return requestQueue
    }

    fun <T> addToRequestQueue(req: Request<T>?) {
        getRequestQueue()!!.add(req)
    }

    companion object {
        private var instance: MySingleton? = null

        @Synchronized
        fun getInstance(context: Context): MySingleton? {
            if (instance == null) {
                instance =
                    MySingleton(context)
            }
            return instance
        }
    }

    init {
        requestQueue = getRequestQueue()
    }
}
