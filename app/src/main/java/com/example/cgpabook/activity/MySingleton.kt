package com.example.cgpabook.activity

import android.content.Context
import android.graphics.Bitmap
import androidx.collection.LruCache
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.Volley

class MySingleton private constructor(private val ctx: Context) {
    private var requestQueue: RequestQueue?
    val imageLoader: ImageLoader

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
                instance = MySingleton(context)
            }
            return instance
        }
    }

    init {
        requestQueue = getRequestQueue()
        imageLoader = ImageLoader(requestQueue,
            object : ImageLoader.ImageCache {
                private val cache =
                    LruCache<String, Bitmap>(100)

                override fun getBitmap(url: String): Bitmap {
                    return cache[url]!!
                }

                override fun putBitmap(url: String, bitmap: Bitmap) {
                    if (getBitmap(url) == null) cache.put(url, bitmap)
                }
            })
    }
}
