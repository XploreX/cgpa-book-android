package com.example.cgpabook.utils

import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonRequest
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class JsonObjectRequestCached(
    method: Int,
    url: String?,
    jsonRequest: JSONObject?,
    listener: Response.Listener<JSONObject>?,
    errorListener: Response.ErrorListener?
) : JsonRequest<JSONObject>(
    method,
    url,
    jsonRequest.toString(),
    listener,
    errorListener
) {
    override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
        try {
            var cacheEntry =
                HttpHeaderParser.parseCacheHeaders(response)
            if (cacheEntry == null) {
                cacheEntry = Cache.Entry()
                cacheEntry.data = response!!.data
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
            }
            val cacheHitButRefreshed =
                3 * 60 * 1000.toLong() // in 3 minutes cache will be hit, but also refreshed on background
            val cacheExpired =
                30 * 24 * 60 * 60 * 1000.toLong() // in 30*24 hours this cache entry expires completely
            val now = System.currentTimeMillis()
            val softExpire = now + cacheHitButRefreshed
            val ttl = now + cacheExpired
            cacheEntry.softTtl = softExpire
            cacheEntry.ttl = ttl

            val jsonString = JSONObject(
                String(
                    response!!.data,
                    Charset.forName(HttpHeaderParser.parseCharset(response.headers))
                )
            )
            return Response.success(
                jsonString,
                cacheEntry
            )
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
