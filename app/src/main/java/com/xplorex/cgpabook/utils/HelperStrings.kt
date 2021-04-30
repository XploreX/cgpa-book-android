package com.xplorex.cgpabook.utils

import com.xplorex.cgpabook.BuildConfig

class HelperStrings {
    companion object {

        const val infobar: String = "infobar"
        const val updateProfile: String = "updateProfile"

        // Used while de-registering savedStateHandle
        const val NavigationActivity = "NavigationActivity"

        // Global requirements
        const val url = BuildConfig.API_URL

        // Navigation Activity requirements
        const val unlocked = "unlocked"
        const val cgpa = "cgpa"
        const val college = "college"
        const val name = "name"
        const val email = "email"
        const val photoUrl = "photoUrl"
        const val semester = "semester"
        const val branch = "branch"
        const val course = "course"
        const val semdata = "semesters"
        const val sgpa = "sgpa"
        const val tokenId = "tokenId"
        const val rated = "rated"

        // shared Prefs Requirements
        const val sharedPrefs = "sharedprefs"
        const val synced = "synced"
    }
}
