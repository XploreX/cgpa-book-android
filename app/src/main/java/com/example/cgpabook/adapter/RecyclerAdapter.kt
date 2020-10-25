package com.example.cgpabook.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cgpabook.R
import com.example.cgpabook.utils.inflate

class RecyclerAdapter(var array: ArrayList<String>, var context: Context) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = parent.inflate(R.layout.search_item, false)
        return ViewHolder(inflatedView)
    }

    override fun getItemCount(): Int {
        return array.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemname.text = array[position]
        holder.itemView.setOnClickListener {
            Log.d("RecyclerView", "CLICK!")
            val textView: TextView = holder.itemname
            val intent = Intent()
            intent.putExtra("selected", textView.text)
            (context as Activity).setResult(Activity.RESULT_OK, intent)
            (context as Activity).finish()
        }
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val itemname: TextView = v.findViewById(R.id.recycle_text)
    }

}