package com.example.cgpabook.utils

import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonRequest
import org.json.JSONArray
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset


class JsonArrayRequestCached(
    method: Int, url: String?, jsonRequest: JSONObject?,
    listener: Response.Listener<JSONArray>?, errorListener: Response.ErrorListener?
) : JsonRequest<JSONArray>(method, url, jsonRequest.toString(), listener, errorListener) {

    override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONArray> {
        try {
            var cacheEntry = HttpHeaderParser.parseCacheHeaders(response)
            if (cacheEntry == null) {

                // setup cache entry and save response
                cacheEntry = Cache.Entry()
                cacheEntry.data = response!!.data

                // add date
                var headerValue: String? = response.headers["Date"]
                if (headerValue != null) {
                    cacheEntry.serverDate =
                        HttpHeaderParser.parseDateAsEpoch(headerValue)
                }

                // add last modified
                headerValue = response.headers["Last-Modified"]
                if (headerValue != null) {
                    cacheEntry.lastModified =
                        HttpHeaderParser.parseDateAsEpoch(headerValue)
                }
                cacheEntry.responseHeaders = response.headers
            }

            // in 3 minutes cache will be hit, but also refreshed on background
            // in 30*24 hours this cache entry expires completely
            val cacheHitButRefreshed = 3 * 60 * 1000.toLong()
            val cacheExpired = 30 * 24 * 60 * 60 * 1000.toLong()

            val now = System.currentTimeMillis()
            val softExpire = now + cacheHitButRefreshed
            val ttl = now + cacheExpired
            cacheEntry.softTtl = softExpire
            cacheEntry.ttl = ttl

            val jsonString = JSONArray(
                String(
                    response!!.data,
                    Charset.forName(HttpHeaderParser.parseCharset(response.headers))
                )
            )
            return Response.success(jsonString, cacheEntry)
        } catch (e: UnsupportedEncodingException) {
            return Response.error(ParseError(e))
        }
    }

    override fun deliverError(error: VolleyError?) {
        if (error is NoConnectionError) {
            val entry = this.cacheEntry
            if (entry != null) {
                return
            }
        }
        super.deliverError(error)
    }
}
