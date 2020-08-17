package com.example.cgpabook.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cgpabook.R
import com.example.cgpabook.adapter.RecyclerAdapter
import java.util.regex.Pattern

class SearchActivity : AppCompatActivity() {
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView
    lateinit var arrayList: ArrayList<String>
    lateinit var changedArrayList: ArrayList<String>
    lateinit var recyclerAdapter: RecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        if (intent != null) {
            arrayList = intent.getStringArrayListExtra("List") as ArrayList<String>
        }
        println(arrayList)
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView = findViewById(R.id.recyclerview)
        recyclerView.layoutManager = linearLayoutManager
        changedArrayList = arrayList
        recyclerAdapter = RecyclerAdapter(changedArrayList, this)
        recyclerView.adapter = recyclerAdapter
        findViewById<EditText>(R.id.search_bar).addTextChangedListener(
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val regex: String = ".*" + s.toString() + ".*"
                    val pattern: Pattern = Pattern.compile(regex)
                    changedArrayList = arrayList.filter { str ->
                        pattern.matcher(str).matches()
                    } as ArrayList<String>
                    recyclerAdapter.array = changedArrayList
                    recyclerAdapter.notifyDataSetChanged()
                }

            }

        )
    }
}