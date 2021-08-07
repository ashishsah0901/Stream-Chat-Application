package com.example.streamchat.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streamchat.util.Util
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val chatClient: ChatClient
) : ViewModel() {

    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    val loginEvent = _loginEvent.asSharedFlow()

    private fun isValidUserName(userName: String) =
        userName.length >= Util.MINIMUM_USERNAME_LENGTH

    fun connectUser(userName: String) {
        val trimmedUsername = userName.trim()
        viewModelScope.launch {
            if(getCurrentUser() != null) {
                _loginEvent.emit(LoginEvent.Success)
            }
            if(isValidUserName(trimmedUsername)) {
                val result = chatClient.connectGuestUser(trimmedUsername, trimmedUsername).await()
                if(result.isError) {
                    _loginEvent.emit(LoginEvent.ErrorLogin(result.error().message
                            ?: "Unknown Error"))
                    return@launch
                }
                _loginEvent.emit(LoginEvent.Success)
            } else {
                _loginEvent.emit(LoginEvent.ErrorInputTooShort)
            }
        }
    }
    fun getCurrentUser() : User? {
        return chatClient.getCurrentUser()
    }
    sealed class LoginEvent {
        object ErrorInputTooShort : LoginEvent()
        data class ErrorLogin(val error: String) : LoginEvent()
        object Success : LoginEvent()
    }
}