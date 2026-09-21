package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.SurgeryQuestion
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

class SurgeryRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("surgery_mcq_prefs", Context.MODE_PRIVATE)

    private var cachedQuestions: List<SurgeryQuestion>? = null

    fun getQuestions(): List<SurgeryQuestion> {
        cachedQuestions?.let { return it }

        val list = mutableListOf<SurgeryQuestion>()
        try {
            val inputStream = context.assets.open("surgery_mcqs.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getInt("id")
                val category = obj.getString("category")
                val question = obj.getString("question")
                val optionsArray = obj.getJSONArray("options")
                val options = mutableListOf<String>()
                for (j in 0 until optionsArray.length()) {
                    options.add(optionsArray.getString(j))
                }
                val correctAnswer = obj.getString("correctAnswer")
                val explanation = obj.getString("explanation")

                list.add(
                    SurgeryQuestion(
                        id = id,
                        category = category,
                        question = question,
                        options = options,
                        correctAnswer = correctAnswer,
                        explanation = explanation
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        cachedQuestions = list
        return list
    }

    fun getCategories(): List<String> {
        val questions = getQuestions()
        return listOf("All") + questions.map { it.category }.distinct()
    }

    fun loadUserAnswers(): Map<Int, String> {
        val result = mutableMapOf<Int, String>()
        val allPrefs = prefs.all
        for ((key, value) in allPrefs) {
            if (key.startsWith("ans_") && value is String) {
                val idStr = key.removePrefix("ans_")
                val id = idStr.toIntOrNull()
                if (id != null) {
                    result[id] = value
                }
            }
        }
        return result
    }

    fun saveUserAnswer(questionId: Int, selectedOption: String) {
        prefs.edit().putString("ans_$questionId", selectedOption).apply()
    }

    fun loadBookmarkedIds(): Set<Int> {
        val stringSet = prefs.getStringSet("bookmarked_ids", emptySet()) ?: emptySet()
        return stringSet.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun toggleBookmark(questionId: Int): Set<Int> {
        val current = loadBookmarkedIds().toMutableSet()
        if (current.contains(questionId)) {
            current.remove(questionId)
        } else {
            current.add(questionId)
        }
        prefs.edit()
            .putStringSet("bookmarked_ids", current.map { it.toString() }.toSet())
            .apply()
        return current
    }

    fun resetAllAnswers() {
        val editor = prefs.edit()
        val allKeys = prefs.all.keys
        for (k in allKeys) {
            if (k.startsWith("ans_")) {
                editor.remove(k)
            }
        }
        editor.apply()
    }

    fun saveLastQuestionId(questionId: Int) {
        prefs.edit().putInt("last_question_id", questionId).apply()
    }

    fun getLastQuestionId(): Int {
        return prefs.getInt("last_question_id", 1)
    }
}
