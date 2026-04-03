package com.prateek.androidstudy.di

import android.content.Context
import androidx.room.Room
import androidx.transition.Visibility.Mode
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.prateek.androidstudy.data.local.newsApi.NewsDAO
import com.prateek.androidstudy.data.local.newsApi.NewsDb
import com.prateek.androidstudy.data.remote.newsApi.NewsApiService
import com.prateek.androidstudy.data.remote.websocket.WebSocketService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule {


    private val authInterceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
            requestBuilder.addHeader("Authorization", "lolz")
            requestBuilder.addHeader("Content-Type", "application/json")
            val newRequest = requestBuilder.build()
            return chain.proceed(newRequest)
        }
    }

    @Provides
    fun provideGson(): Gson {
        return GsonBuilder()
            .create()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(3, TimeUnit.SECONDS)    // Shorter than ping interval
            .writeTimeout(5, TimeUnit.SECONDS)
            .callTimeout(15, TimeUnit.SECONDS)   // Longer for complete operations
            .pingInterval(5, TimeUnit.SECONDS)   // More reasonable than 1 second
          // mostly used for web sockets , it helps to try catch any connectivity issue
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor(authInterceptor
            ).build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("i.pi.")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideNewsDatabase(@ApplicationContext context: Context): NewsDb {
        return Room.databaseBuilder(
            context,
            NewsDb::class.java,
            "news_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideNewsDao(newsDb: NewsDb):NewsDAO = newsDb.dao

    @Provides
    @Singleton
    fun providesNewsInterface(retrofit: Retrofit):NewsApiService = retrofit.create(NewsApiService::class.java)



    //remove authorisation from http client header for web socket
    @Provides
    @Singleton
    fun provideWebSocket(okHttpClient: OkHttpClient): WebSocketService {
        return WebSocketService(okHttpClient)
    }



}