package com.thesecretplace.app.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    val currentUserEmail: String? get() = supabase.currentUserEmail

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _loginError.value = null
            val result = supabase.signIn(email, password)
            if (result.isSuccess) {
                _isLoggedIn.value = true
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
}
