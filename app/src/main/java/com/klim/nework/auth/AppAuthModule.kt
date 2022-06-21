package com.klim.nework.auth

import android.content.Context
import android.content.SharedPreferences
import com.klim.nework.Api.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppAuthModule {

    @Provides
    fun provideAuthPrefs(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideAppAuth(prefs: SharedPreferences, apiService: ApiService): AppAuth =
        AppAuth(prefs, apiService)
}