package com.realityexpander.ktornoteapp.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.realityexpander.ktornoteapp.common.Constants.BASE_URL
import com.realityexpander.ktornoteapp.common.Constants.DATABASE_NAME
import com.realityexpander.ktornoteapp.common.Constants.ENCRYPTED_SHARED_PREF_NAME
import com.realityexpander.ktornoteapp.data.local.NotesDao
import com.realityexpander.ktornoteapp.data.local.NotesDatabase
import com.realityexpander.ktornoteapp.data.remote.BasicAuthInterceptor
import com.realityexpander.ktornoteapp.data.remote.NotesApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)  // Lifetime of this module is the same as the app (ie: a Singleton), so put it into the SingletonComponent
object AppModule {

    /// DATABASE ///

    @Singleton
    @Provides
    fun provideNotesDatabase(
        @ApplicationContext context: Context
    ): NotesDatabase = Room.databaseBuilder(
        context,
        NotesDatabase::class.java,
        DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideNotesDao(db: NotesDatabase): NotesDao = db.notesDao()


    /// RETROFIT ///

    @Singleton
    @Provides
    fun provideBasicAuthInterceptor(): BasicAuthInterceptor =
        BasicAuthInterceptor()

    @Singleton
    @Provides
    fun provideNotesApi(basicAuthInterceptor: BasicAuthInterceptor): NotesApi {
        val client = OkHttpClient.Builder()
            .addInterceptor(basicAuthInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Convert the JSON response to a POJO
            .client(client) // Add the client with our Basic Authentication interceptor
            .build()
            .create(NotesApi::class.java)
    }

    /// ENCRYPTED SHARED PREFERENCES ///

    @Singleton
    @Provides
    fun provideEncryptedSharedPreferences( // creates a encrypted shared prefs that can be accessed offline, normal shared prefs can be viewed by root users
        @ApplicationContext context: Context
    ): SharedPreferences  {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

        return EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_SHARED_PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}