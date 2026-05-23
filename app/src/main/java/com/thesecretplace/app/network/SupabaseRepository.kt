package com.thesecretplace.app.network

import com.thesecretplace.app.model.Meditation
import com.thesecretplace.app.model.MeditationCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseRepository @Inject constructor() {

    private val projectURL = "https://mkydoqrekhtapiydneag.supabase.co"
    private val anonKey = "sb_publishable_wDrsg2An0K8qOe-8Y4vbmA_5NpuWSHc"
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    var isLoggedIn = false
        private set
    var currentUserEmail: String? = null
        private set
    private var accessToken: String? = null

    // Auth
    suspend fun signIn(email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        val body = Json.encodeToString(JsonObject.serializer(), buildJsonObject {
            put("email", email)
            put("password", password)
        })
        val request = Request.Builder()
            .url("$projectURL/auth/v1/token?grant_type=password")
            .addHeader("apikey", anonKey)
            .addHeader("Content-Type", "application/json")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.code != 200) {
                return@withContext Result.failure(Exception("Sign in failed (${response.code})"))
            }
            val responseBody = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
            val jsonObj = json.parseToJsonElement(responseBody).jsonObject
            accessToken = jsonObj["access_token"]?.jsonPrimitive?.content
            val userObj = jsonObj["user"]?.jsonObject
            currentUserEmail = userObj?.get("email")?.jsonPrimitive?.content
            isLoggedIn = true
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    fun signOut() {
        accessToken = null
        currentUserEmail = null
        isLoggedIn = false
    }

    // Fetch meditations
    suspend fun fetchMeditations(): List<Meditation> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$projectURL/rest/v1/meditations?select=*&order=created_at.asc")
            .addHeader("apikey", anonKey)
            .addHeader("Accept", "application/json")
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.code != 200) return@withContext emptyList()
            val responseBody = response.body?.string() ?: return@withContext emptyList()
            val rows = json.parseToJsonElement(responseBody).jsonArray

            rows.mapNotNull { element ->
                val row = element.jsonObject
                val id = row["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                val title = row["title"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                val duration = row["duration"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                val description = row["description"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                val categoryRaw = row["category"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                val audioURL = row["audio_url"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                val imageName = row["image_name"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                val category = MeditationCategory.fromRawValue(categoryRaw) ?: return@mapNotNull null
                val isNew = row["is_new"]?.jsonPrimitive?.booleanOrNull ?: false
                val secondaryCategory = row["secondary_category"]?.jsonPrimitive?.contentOrNull?.let {
                    MeditationCategory.fromRawValue(it)
                }

                Meditation(
                    id = id,
                    title = title,
                    duration = duration,
                    description = description,
                    imageName = imageName
                        .replace("-", "_")
                        .replace(Regex("([a-z])([A-Z])")) { "${it.groupValues[1]}_${it.groupValues[2]}" }
                        .lowercase(),
                    audioFileName = id,
                    category = category,
                    secondaryCategory = secondaryCategory,
                    isNew = isNew,
                    remoteAudioURL = audioURL
                )
            }
        } catch (e: Exception) {
            println("⚠️ Supabase fetch failed: ${e.message}")
            emptyList()
        }
    }

    // Insert meditation (admin)
    suspend fun insertMeditation(
        id: String, title: String, duration: String, description: String,
        category: MeditationCategory, secondaryCategory: MeditationCategory?,
        audioURL: String, imageName: String, isNew: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val token = accessToken ?: return@withContext Result.failure(Exception("Not logged in"))
        val body = buildJsonObject {
            put("id", id)
            put("title", title)
            put("duration", duration)
            put("description", description)
            put("category", category.displayName)
            put("audio_url", audioURL)
            put("image_name", imageName)
            put("is_new", isNew)
            secondaryCategory?.let { put("secondary_category", it.displayName) }
        }
        val request = Request.Builder()
            .url("$projectURL/rest/v1/meditations")
            .addHeader("apikey", anonKey)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=minimal")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()
        try {
            val response = client.newCall(request).execute()
            if (response.code == 201) Result.success(Unit)
            else Result.failure(Exception("Insert failed (${response.code})"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Upload audio (admin)
    suspend fun uploadAudio(data: ByteArray, filename: String): Result<String> = withContext(Dispatchers.IO) {
        val token = accessToken ?: return@withContext Result.failure(Exception("Not logged in"))
        val request = Request.Builder()
            .url("$projectURL/storage/v1/object/meditation-audio/$filename")
            .addHeader("apikey", anonKey)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "audio/mpeg")
            .post(data.toRequestBody("audio/mpeg".toMediaType()))
            .build()
        try {
            val response = client.newCall(request).execute()
            if (response.code in 200..201) {
                Result.success("$projectURL/storage/v1/object/public/meditation-audio/$filename")
            } else {
                Result.failure(Exception("Upload failed (${response.code})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update meditation (admin)
    suspend fun updateMeditation(
        id: String, title: String, duration: String, description: String,
        category: MeditationCategory, secondaryCategory: MeditationCategory?,
        imageName: String, isNew: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val token = accessToken ?: return@withContext Result.failure(Exception("Not logged in"))
        val body = buildJsonObject {
            put("title", title)
            put("duration", duration)
            put("description", description)
            put("category", category.displayName)
            put("image_name", imageName)
            put("is_new", isNew)
            if (secondaryCategory != null) put("secondary_category", secondaryCategory.displayName)
            else put("secondary_category", JsonNull)
        }
        val request = Request.Builder()
            .url("$projectURL/rest/v1/meditations?id=eq.$id")
            .addHeader("apikey", anonKey)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=minimal")
            .patch(body.toString().toRequestBody("application/json".toMediaType()))
            .build()
        try {
            val response = client.newCall(request).execute()
            if (response.code == 204) Result.success(Unit)
            else Result.failure(Exception("Update failed (${response.code})"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete meditation (admin)
    suspend fun deleteMeditation(id: String, audioFilename: String): Result<Unit> = withContext(Dispatchers.IO) {
        val token = accessToken ?: return@withContext Result.failure(Exception("Not logged in"))
        // Delete DB row
        val dbRequest = Request.Builder()
            .url("$projectURL/rest/v1/meditations?id=eq.$id")
            .addHeader("apikey", anonKey)
            .addHeader("Authorization", "Bearer $token")
            .delete()
            .build()
        try {
            val dbResponse = client.newCall(dbRequest).execute()
            if (dbResponse.code !in listOf(200, 204)) {
                return@withContext Result.failure(Exception("Delete row failed (${dbResponse.code})"))
            }
            // Best-effort delete audio from storage
            val storageRequest = Request.Builder()
                .url("$projectURL/storage/v1/object/meditation-audio/$audioFilename")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $token")
                .delete()
                .build()
            client.newCall(storageRequest).execute()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
