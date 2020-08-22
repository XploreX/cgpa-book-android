package com.example.cgpabook.utils

import com.android.volley.Cache
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonArrayRequest
import org.json.JSONArray
import java.io.UnsupportedEncodingException

class JSONArrayRequestCached(
    method: Int,
    url: String?,
    jsonRequest: JSONArray?,
    listener: Response.Listener<JSONArray>?,
    errorListener: Response.ErrorListener?
) : JsonArrayRequest(method, url, jsonRequest, listener, errorListener) {
    override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONArray> {
        try {
            var cacheEntry =
                HttpHeaderParser.parseCacheHeaders(response)
            if (cacheEntry == null) {
                cacheEntry = Cache.Entry()
            }
            val cacheHitButRefreshed =
                3 * 60 * 1000.toLong() // in 3 minutes cache will be hit, but also refreshed on background
            val cacheExpired =
                24 * 60 * 60 * 1000.toLong() // in 24 hours this cache entry expires completely
            val now = System.currentTimeMillis()
            val softExpire = now + cacheHitButRefreshed
            val ttl = now + cacheExpired
            cacheEntry.data = response!!.data
            cacheEntry.softTtl = softExpire
            cacheEntry.ttl = ttl
            var headerValue: String? = response.headers["Date"]
            if (headerValue != null) {
                cacheEntry.serverDate =
                    HttpHeaderParser.parseDateAsEpoch(headerValue)
            }
            headerValue = response.headers["Last-Modified"]
            if (headerValue != null) {
                cacheEntry.lastModified =
                    HttpHeaderParser.parseDateAsEpoch(headerValue)
            }
            cacheEntry.responseHeaders = response.headers
            val jsonString = JSONArray(
                String(
                    response.data
                )
            )
            println(jsonString)
            return Response.success<JSONArray>(
                jsonString,
                cacheEntry
            )
        } catch (e: UnsupportedEncodingException) {
            return Response.error<JSONArray>(ParseError(e))
        }
    }
}
