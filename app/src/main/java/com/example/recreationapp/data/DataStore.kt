package com.example.recreationapp.data

import android.content.Context
import com.example.recreationapp.model.User
import com.example.recreationapp.model.ActivityRecord
import com.google.gson.Gson
import java.io.File

object DataStore {
    private const val PREFS_NAME = "RecreationPrefs"
    private const val USER_KEY = "current_user"
    private const val ACTIVITIES_FILE = "activities.json"

    fun saveUser(context: Context, user: User) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(USER_KEY, Gson().toJson(user)).apply()
    }

    fun getUser(context: Context): User? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(USER_KEY, null) ?: return null
        return Gson().fromJson(json, User::class.java)
    }

    fun saveActivities(context: Context, activities: List<ActivityRecord>) {
        val file = File(context.filesDir, ACTIVITIES_FILE)
        file.writeText(Gson().toJson(activities))
    }

    fun getActivities(context: Context): List<ActivityRecord> {
        val file = File(context.filesDir, ACTIVITIES_FILE)
        if (!file.exists()) return emptyList()
        val json = file.readText()
        return Gson().fromJson(json, Array<ActivityRecord>::class.java).toList()
    }
}