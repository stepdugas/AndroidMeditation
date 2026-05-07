package com.thesecretplace.app.network

import com.thesecretplace.app.data.PreferencesManager
import com.thesecretplace.app.model.MeditationCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnthropicRepository @Inject constructor(
    private val prefs: PreferencesManager
) {
    private val workerURL = "https://the-secret-place-lessons.the-secret-place-lessons.workers.dev/lesson"
    private val appSecret = "tspsecret2026"
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    var isLoading = false
        private set
    var errorMessage: String? = null
        private set

    private fun todayString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    private fun dailyInsightKey() = "daily_insight_${todayString()}"
    private fun dailyPrayerKey() = "daily_prayer_${todayString()}"

    fun cachedDailyInsight(): String? = prefs.getCachedString(dailyInsightKey())
    fun cachedDailyPrayer(): String? = prefs.getCachedString(dailyPrayerKey())

    // Auto-fetch: returns today's cached insight if it exists, otherwise generates one
    suspend fun todaysDailyInsight(): String? {
        cachedDailyInsight()?.let { return it }

        isLoading = true
        errorMessage = null

        try {
            val calendar = Calendar.getInstance()
            val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            val categories = MeditationCategory.entries
            val todaysCategory = categories[dayOfYear % categories.size]
            val prompt = buildPrompt(todaysCategory)

            val result = fetchFromWorker(categoryKey(todaysCategory), prompt)
            result?.let { (lesson, prayer) ->
                prefs.setCachedString(dailyInsightKey(), lesson)
                prayer?.let { prefs.setCachedString(dailyPrayerKey(), it) }
                return lesson
            }
            return null
        } finally {
            isLoading = false
        }
    }

    // Per-category lesson (used by Learn tab)
    fun cachedLesson(category: MeditationCategory): String? {
        return prefs.getCachedString("microlesson_text_${categoryKey(category)}_${todayString()}")
    }

    suspend fun todaysLesson(category: MeditationCategory): String? {
        cachedLesson(category)?.let { return it }
        isLoading = true
        errorMessage = null
        try {
            val result = fetchFromWorker(categoryKey(category), buildPrompt(category))
            result?.let { (lesson, _) ->
                prefs.setCachedString("microlesson_text_${categoryKey(category)}_${todayString()}", lesson)
                return lesson
            }
            return null
        } finally {
            isLoading = false
        }
    }

    private suspend fun fetchFromWorker(category: String, prompt: String): Pair<String, String?>? =
        withContext(Dispatchers.IO) {
            val body = buildJsonObject {
                put("category", category)
                put("prompt", prompt)
            }
            val request = Request.Builder()
                .url(workerURL)
                .addHeader("Content-Type", "application/json")
                .addHeader("x-app-secret", appSecret)
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .build()

            try {
                val response = client.newCall(request).execute()
                when {
                    response.code == 401 -> {
                        errorMessage = "App secret mismatch."
                        null
                    }
                    response.code != 200 -> {
                        errorMessage = "Server error (${response.code})."
                        null
                    }
                    else -> {
                        val responseBody = response.body?.string() ?: return@withContext null
                        val jsonObj = json.parseToJsonElement(responseBody).jsonObject
                        val lesson = jsonObj["lesson"]?.jsonPrimitive?.contentOrNull
                        val prayer = jsonObj["prayer"]?.jsonPrimitive?.contentOrNull
                        if (lesson.isNullOrEmpty()) {
                            errorMessage = "Unexpected response format."
                            null
                        } else {
                            Pair(lesson, prayer)
                        }
                    }
                }
            } catch (e: Exception) {
                errorMessage = e.message
                null
            }
        }

    private fun categoryKey(category: MeditationCategory): String {
        return category.displayName.replace(" ", "")
    }

    private fun buildPrompt(category: MeditationCategory): String {
        val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.US)
        val dateString = sdf.format(Date())

        return when (category) {
            MeditationCategory.SLEEP ->
                "Today is $dateString. Write one specific, science-backed insight (2-3 sentences) about sleep and meditation. Reference a real mechanism, study finding, or named technique — for example, how slow breathing activates the parasympathetic nervous system, or how body scan meditation reduces sleep onset latency. Be warm but substantive. No vague affirmations."
            MeditationCategory.MORNING ->
                "Today is $dateString. Write one specific, science-backed insight (2-3 sentences) about morning meditation or mindful intention-setting. Reference a real mechanism or research finding — for example, cortisol awakening response, implementation intentions, or attentional priming. Be energizing but grounded. No vague affirmations."
            MeditationCategory.STRESS_RELIEF ->
                "Today is $dateString. Write one specific, science-backed insight (2-3 sentences) about stress relief through meditation. Reference a real technique or mechanism — for example, HRV coherence, vagal tone, NSDR, or the physiological sigh. Be practical and reassuring. No vague affirmations."
            MeditationCategory.BREATHWORK ->
                "Today is $dateString. Write one specific, science-backed insight (2-3 sentences) about breathwork. Reference a real technique or physiological mechanism — for example, the physiological sigh, box breathing's effect on the autonomic nervous system, or how nasal breathing increases nitric oxide. Be practical and precise. No vague affirmations."
            MeditationCategory.SOUNDSCAPES ->
                "Today is $dateString. Write one specific, science-backed insight (2-3 sentences) about how ambient sound or nature soundscapes affect the mind and nervous system. Reference a real mechanism or study — for example, pink noise and sleep quality, nature sounds reducing cortisol, or auditory masking for focus. Be calm and substantive. No vague affirmations."
            MeditationCategory.MEDITATIO ->
                "Today is $dateString. Write one specific insight (2-3 sentences) about meditatio — the ancient Christian practice of prayerfully meditating on Scripture, repeating God's words, and letting truth sink deep into the heart. Reference a real concept, practice, or scriptural principle — for example, lectio divina, Psalm 1's imagery of meditating day and night, or how repetition of Scripture rewires thought patterns. Be reverent, warm, and grounded. No vague affirmations."
            MeditationCategory.MENTAL_TRAINING ->
                "Today is $dateString. Write one specific, science-backed insight (2-3 sentences) about mental training through meditation — focus, cognition, or neuroplasticity. Reference a real mechanism or study — for example, default mode network deactivation, attention blink reduction, or how MBSR affects prefrontal cortex thickness. Be sharp and substantive. No vague affirmations."
        }
    }
}
