package com.xplorex.cgpabook.utils

import com.xplorex.cgpabook.BuildConfig

class HelperStrings {
    companion object {

        const val infobar = "infobar"
        const val updateProfile = "updateProfile"

        // Used while de-registering savedStateHandle
        const val NavigationActivity = "NavigationActivity"

        // Global requirements
        const val url = BuildConfig.API_URL

        // Navigation Activity requirements //
        const val unlocked = "unlocked"

        // user details
        const val name = "name"
        const val email = "email"
        const val photoUrl = "photoUrl"
        const val sgpa = "sgpa"
        const val cgpa = "cgpa"
        const val tokenId = "tokenId"
        const val rated = "rated"

        // sub details
        const val college = "college"
        const val semester = "semester"
        const val branch = "branch"
        const val course = "course"
        const val subjects = "subjects"
        const val semdata = "semesters"

        // shared Prefs Requirements
        const val sharedPrefs = "sharedprefs"
        const val synced = "synced"
    }
}
