package com.marcolodeiro.gamelog.di

import com.google.ai.client.generativeai.GenerativeModel
import com.marcolodeiro.gamelog.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatModule {

    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel {
        return GenerativeModel(
            modelName = "gemini-2.5-flash", //
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }
}