package com.example.qr_prueba_gaby.di

import android.content.Context
import com.example.qr_prueba_gaby.data.pref.UserDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideUserDataStore(@ApplicationContext context: Context): UserDataStore {
        return UserDataStore(context)
    }

    @Provides
    @Singleton
    fun provideGateRepository(@ApplicationContext context: Context): com.example.qr_prueba_gaby.data.repository.GateRepository {
        return com.example.qr_prueba_gaby.data.repository.GateRepository(context)
    }
}
