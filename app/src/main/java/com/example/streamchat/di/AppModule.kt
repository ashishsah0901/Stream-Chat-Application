package com.example.streamchat.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.example.streamchat.util.Util
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.getstream.chat.android.client.ChatClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideChatClient(@ApplicationContext context: Context) =
        ChatClient.Builder(Util.API_KEY, context).build()

    @Provides
    @Singleton
    fun provideSharedPref(@ApplicationContext context: Context) =
        context.getSharedPreferences("StreamPreference", MODE_PRIVATE)
}