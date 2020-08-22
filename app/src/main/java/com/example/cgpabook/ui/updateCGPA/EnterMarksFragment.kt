package com.example.cgpabook.ui.updateCGPA

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.cgpabook.R
import com.example.cgpabook.utils.dashBoardButton

class EnterMarksFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_enter_marks, container, false)
        dashBoardButton(root)
        return root
    }
}