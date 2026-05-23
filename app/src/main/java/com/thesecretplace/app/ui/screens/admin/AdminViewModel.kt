package com.thesecretplace.app.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thesecretplace.app.model.Meditation
import com.thesecretplace.app.model.MeditationCategory
import com.thesecretplace.app.network.SupabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val supabase: SupabaseRepository
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(supabase.isLoggedIn)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _uploadResult = MutableStateFlow<String?>(null)
    val uploadResult: StateFlow<String?> = _uploadResult.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _cloudMeditations = MutableStateFlow<List<Meditation>>(emptyList())
    val cloudMeditations: StateFlow<List<Meditation>> = _cloudMeditations.asStateFlow()

    private val _editResult = MutableStateFlow<String?>(null)
    val editResult: StateFlow<String?> = _editResult.asStateFlow()

    private val _deleteResult = MutableStateFlow<String?>(null)
    val deleteResult: StateFlow<String?> = _deleteResult.asStateFlow()

    val currentUserEmail: String? get() = supabase.currentUserEmail

    init {
        refreshCatalog()
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _loginError.value = null
            val result = supabase.signIn(email, password)
            if (result.isSuccess) {
                _isLoggedIn.value = true
                refreshCatalog()
                println("✅ Admin signed in: $email")
            } else {
                _loginError.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun signOut() {
        supabase.signOut()
        _isLoggedIn.value = false
    }

    fun uploadMeditation(
        title: String, description: String, duration: String,
        category: MeditationCategory, secondaryCategory: MeditationCategory?,
        imageName: String, isNew: Boolean, audioData: ByteArray
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            _uploadResult.value = null

            val baseId = title.lowercase()
                .replace(Regex("[^a-z0-9\\s-]"), "")
                .trim()
                .replace(Regex("\\s+"), "-")
            val filename = "$baseId.mp3"

            try {
                val audioURL = supabase.uploadAudio(audioData, filename).getOrThrow()
                println("✅ Audio uploaded: $audioURL")

                supabase.insertMeditation(
                    id = baseId, title = title, duration = duration,
                    description = description, category = category,
                    secondaryCategory = secondaryCategory,
                    audioURL = audioURL, imageName = imageName, isNew = isNew
                ).getOrThrow()
                println("✅ Meditation record inserted: $title")

                _uploadResult.value = "✓ \"$title\" is now live!"
                refreshCatalog()
            } catch (e: Exception) {
                val msg = e.message ?: "Unknown error"
                _uploadResult.value = when {
                    msg.contains("409") || msg.contains("duplicate", true) -> "A meditation with this name already exists."
                    msg.contains("401") || msg.contains("jwt", true) || msg.contains("expired", true) -> "Session expired. Sign out and sign back in."
                    else -> "Upload failed: $msg"
                }
                println("❌ Upload error: $msg")
            }

            _isUploading.value = false
        }
    }

    fun updateMeditation(
        id: String, title: String, description: String, duration: String,
        category: MeditationCategory, secondaryCategory: MeditationCategory?,
        imageName: String, isNew: Boolean
    ) {
        viewModelScope.launch {
            _editResult.value = null
            try {
                supabase.updateMeditation(
                    id = id, title = title, duration = duration,
                    description = description, category = category,
                    secondaryCategory = secondaryCategory,
                    imageName = imageName, isNew = isNew
                ).getOrThrow()
                println("✅ Meditation updated: $title")
                _editResult.value = "✓ Changes saved!"
                refreshCatalog()
            } catch (e: Exception) {
                val msg = e.message ?: "Unknown error"
                _editResult.value = when {
                    msg.contains("401") || msg.contains("jwt", true) -> "Session expired. Sign out and sign back in."
                    else -> "Update failed: $msg"
                }
                println("❌ Edit error: $msg")
            }
        }
    }

    fun deleteMeditation(id: String) {
        viewModelScope.launch {
            _deleteResult.value = null
            try {
                supabase.deleteMeditation(id, "$id.mp3").getOrThrow()
                println("✅ Meditation deleted: $id")
                _deleteResult.value = "✓ Meditation deleted."
                refreshCatalog()
            } catch (e: Exception) {
                val msg = e.message ?: "Unknown error"
                _deleteResult.value = when {
                    msg.contains("401") || msg.contains("jwt", true) -> "Session expired. Sign out and sign back in."
                    else -> "Delete failed: $msg"
                }
                println("❌ Delete error: $msg")
            }
        }
    }

    fun clearEditResult() {
        _editResult.value = null
    }

    private fun refreshCatalog() {
        viewModelScope.launch {
            try {
                val all = supabase.fetchMeditations()
                // Only show cloud-uploaded meditations (ones with a remoteAudioURL)
                _cloudMeditations.value = all.filter { it.remoteAudioURL != null }
            } catch (_: Exception) {}
        }
    }
}
