package com.example.streamchat.ui.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ChannelViewModel @Inject constructor(
    private val chatClient: ChatClient,
    private val sharedPref: SharedPreferences
) : ViewModel() {

    private val _createChannelEvent = MutableSharedFlow<CreateChannelEvent>()
    val createChannelEvent = _createChannelEvent.asSharedFlow()

    fun logout() {
        sharedPref.edit().putString("Name", "").apply()
        chatClient.disconnect()
    }
    fun getUser() : User? {
        sharedPref.edit().putString("Name", chatClient.getCurrentUser()?.id).apply()
        return chatClient.getCurrentUser()
    }
    fun createChannel(channelName: String) {
        val trimmedChannelName = channelName.trim()
        viewModelScope.launch {
            if(trimmedChannelName.isEmpty()) {
                _createChannelEvent.emit(CreateChannelEvent.Error("Channel name too short"))
                return@launch
            }
            val result = chatClient.createChannel("messaging",trimmedChannelName.hashCode().toString(), mapOf("name" to trimmedChannelName)).await()
            if(result.isError) {
                _createChannelEvent.emit(CreateChannelEvent.Error(result.error().message ?: "Unknown Error"))
                return@launch
            }
            _createChannelEvent.emit(CreateChannelEvent.Success)
        }
    }
    sealed class CreateChannelEvent {
        data class Error(val error: String): CreateChannelEvent()
        object Success: CreateChannelEvent()
    }
}