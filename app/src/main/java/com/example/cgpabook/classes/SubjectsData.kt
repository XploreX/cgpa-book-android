package com.example.cgpabook.classes

import org.json.JSONObject

data class SubjectsData(val jsonObject: JSONObject) {
    var subName: String = jsonObject.getString("subject")
    var subCode: String = jsonObject.getString("subjectCode")
    var credits = jsonObject.getDouble("credits")
}
