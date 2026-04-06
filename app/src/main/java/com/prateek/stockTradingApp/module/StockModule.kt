package com.prateek.stockTradingApp.module

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.prateek.stockTradingApp.data.remote.StockWSSInterface
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

//qualifier is a meta which allows multiple binding of same return type by defining our own
// annotaions
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class StockQualifier


@Module
@InstallIn(SingletonComponent::class)
class StockModule {

    @StockQualifier
    @Provides
    fun provideGson(): Gson {
        return GsonBuilder()
            .create()
    }


    @StockQualifier
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
            .build()
    }


    @StockQualifier
    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("i.pi.")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
    }



    @StockQualifier



}