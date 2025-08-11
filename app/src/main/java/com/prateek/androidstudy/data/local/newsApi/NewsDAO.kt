package com.prateek.androidstudy.data.local.newsApi

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert


@Dao
interface  NewsDAO {
    @Upsert
    suspend fun upsertAll(news: List<NewsEntity>)

    @Query("SELECT * FROM news_db")
    suspend fun getAllNews(): List<NewsEntity>

    @Query("DELETE FROM news_db WHERE expiredOn < :time")
    suspend fun clearCache(time:Long)

    @Query("DELETE FROM news_db")
    suspend fun clearAll()

}