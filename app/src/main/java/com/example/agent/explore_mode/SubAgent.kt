package com.example.agent.explore_mode

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.agent.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class SubAgent {
    private val prompt = AgentPrompt()
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.apiKey,
//        systemInstruction = content { prompt.getSystemPrompt() }
    )

    private fun extractJson(response: String): JSONObject {
        return try {
            val regex = Regex("```json(.*?)```", RegexOption.DOT_MATCHES_ALL)
            val matchResult = regex.find(response)
            val jsonString = matchResult?.groups?.get(1)?.value?.trim() ?: return JSONObject()
            JSONObject(jsonString)
        } catch (e: Exception) {
            Log.e("SubAgent", "Error parsing JSON", e)
            JSONObject()
        }
    }

    fun getResponse(
        subgoal: String,
        visualTree: String,
        screenshot: Bitmap
    ): JSONObject? {
//        _uiState.value = UiState.Loading

        // Filter out unnecessary lines
        var output = JSONObject()
        val filteredLines = visualTree.lines()
            .filter { line ->
                val parts = line.split(",")
                parts.size >= 3 && (parts[1].trim().isNotEmpty() && parts[1] != "null" ||
                        parts[2].trim().isNotEmpty() && parts[2] != "null")
            }
        val filteredVisualTree = filteredLines.joinToString("\n")

        Log.d("SubAgent", "Filtered Visual Tree:\n$filteredVisualTree")

        CoroutineScope(Dispatchers.IO).launch() {
            try {
                val response = generativeModel.generateContent(
                    content {
                        text(prompt.getSystemPrompt())
                        image(screenshot)
                        text(prompt.getUserPrompt(subgoal, filteredVisualTree))
                    }
                )
                response.text?.let { outputContent ->
                    Log.d("SubAgent", "AI Response: $outputContent")
                    output = extractJson(outputContent)
                }
            } catch (e: Exception) {
                Log.e("SubAgent", "Error call response", e)
            }
        }

        return output
    }
}