package com.neptools.app.core.util

import android.content.Context
import com.neptools.app.core.data.license.LicenseCategory
import com.neptools.app.core.data.license.MockTestResult
import org.json.JSONArray
import org.json.JSONObject

object LicenseStorageManager {

    private const val PREF_NAME = "driving_license_prefs"
    private const val KEY_BOOKMARKS = "bookmarked_question_ids"
    private const val KEY_MISTAKES = "mistake_question_ids"
    private const val KEY_TEST_HISTORY = "mock_test_history"

    fun getBookmarks(context: Context): Set<Int> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val setStr = prefs.getStringSet(KEY_BOOKMARKS, emptySet()) ?: emptySet()
        return setStr.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun toggleBookmark(context: Context, questionId: Int): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val current = getBookmarks(context).toMutableSet()
        val isNowBookmarked = if (current.contains(questionId)) {
            current.remove(questionId)
            false
        } else {
            current.add(questionId)
            true
        }
        prefs.edit().putStringSet(KEY_BOOKMARKS, current.map { it.toString() }.toSet()).apply()
        return isNowBookmarked
    }

    fun getMistakes(context: Context): Set<Int> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val setStr = prefs.getStringSet(KEY_MISTAKES, emptySet()) ?: emptySet()
        return setStr.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun recordMistake(context: Context, questionId: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val current = getMistakes(context).toMutableSet()
        current.add(questionId)
        prefs.edit().putStringSet(KEY_MISTAKES, current.map { it.toString() }.toSet()).apply()
    }

    fun removeMistake(context: Context, questionId: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val current = getMistakes(context).toMutableSet()
        current.remove(questionId)
        prefs.edit().putStringSet(KEY_MISTAKES, current.map { it.toString() }.toSet()).apply()
    }

    fun saveTestResult(context: Context, result: MockTestResult) {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val existingJson = prefs.getString(KEY_TEST_HISTORY, "[]") ?: "[]"
            val arr = JSONArray(existingJson)

            val obj = JSONObject().apply {
                put("timestamp", result.timestamp)
                put("category", result.category.code)
                put("totalQuestions", result.totalQuestions)
                put("correctAnswers", result.correctAnswers)
                put("wrongAnswers", result.wrongAnswers)
                put("timeTakenSeconds", result.timeTakenSeconds)
                put("isPassed", result.isPassed)
            }
            arr.put(obj)
            prefs.edit().putString(KEY_TEST_HISTORY, arr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getTestHistory(context: Context): List<MockTestResult> {
        return try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val jsonStr = prefs.getString(KEY_TEST_HISTORY, "[]") ?: "[]"
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<MockTestResult>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    MockTestResult(
                        timestamp = obj.getLong("timestamp"),
                        category = if (obj.getString("category") == "B") LicenseCategory.CATEGORY_B else LicenseCategory.CATEGORY_A,
                        totalQuestions = obj.getInt("totalQuestions"),
                        correctAnswers = obj.getInt("correctAnswers"),
                        wrongAnswers = obj.getInt("wrongAnswers"),
                        timeTakenSeconds = obj.getInt("timeTakenSeconds"),
                        isPassed = obj.getBoolean("isPassed")
                    )
                )
            }
            list.reversed()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
