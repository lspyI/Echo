package com.example.echojournal.di

import android.content.Context
import com.example.echojournal.data.ai.FaceDetectionManager
import com.example.echojournal.data.audio.AudioPlayerManager
import com.example.echojournal.data.audio.AudioRecorder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAudioRecorder(@ApplicationContext context: Context): AudioRecorder {
        return AudioRecorder(context)
    }

    @Provides
    @Singleton
    fun provideAudioPlayer(@ApplicationContext context: Context): AudioPlayerManager {
        return AudioPlayerManager(context)
    }

    @Provides
    @Singleton
    fun provideFaceDetectionManager(@ApplicationContext context: Context): FaceDetectionManager {
        return FaceDetectionManager(context)
    }
}
