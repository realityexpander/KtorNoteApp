package com.realityexpander.ktornoteapp.di

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.realityexpander.ktornoteapp.common.Constants.SERVER_URL
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
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.inject.Named
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

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
    )
        .fallbackToDestructiveMigration()
        .build()

    @Singleton
    @Provides
    fun provideNotesDao(db: NotesDatabase): NotesDao = db.notesDao()


    /// RETROFIT ///

    @Singleton
    @Provides
    fun provideGson(): Gson = Gson()

    @Singleton
    @Provides
    fun provideBasicAuthInterceptor(): BasicAuthInterceptor =
        BasicAuthInterceptor()

    @Singleton
    @Provides
    // Creates a OkHttpClient.Builder that will TRUST and ACCEPT *ALL* certificates. (for testing only)
    fun provideOkHttpClientBuilder(): OkHttpClient.Builder {
        val trustAllCertificates: Array<TrustManager> = arrayOf(

            @SuppressLint("CustomX509TrustManager")
            object : X509TrustManager {

                @SuppressLint("TrustAllX509TrustManager")
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                    // NO OP
                }

                @SuppressLint("TrustAllX509TrustManager")
                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                    // NO OP
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()  // this will allow all certificates to be trusted
                }
            }
        )

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCertificates, SecureRandom())

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCertificates[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
    }

    @Singleton
    @Provides
    @Named("NotesApi_accept_all_certs_for_development")
    // This NotesApi implementation accepts *ALL* certificates. (for testing only)
    fun provideNotesApiDev(okHttpClientBuilder: OkHttpClient.Builder,
                        basicAuthInterceptor: BasicAuthInterceptor
    ): NotesApi {

        val okHttpClient = okHttpClientBuilder
            .addInterceptor(basicAuthInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Convert the JSON response to a POJO
            .client(okHttpClient) // Add the client with our Basic Authentication interceptor
            .build()
            .create(NotesApi::class.java)
    }

    @Singleton
    @Provides
    @Named("NotesApi_production")
    // This NotesApi is for production. (for production only, requires real certified SSL certificate)
    fun provideNotesApiProd(basicAuthInterceptor: BasicAuthInterceptor): NotesApi {

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(basicAuthInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Convert the JSON response to a POJO
            .client(okHttpClient) // Add the client with our Basic Authentication interceptor
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