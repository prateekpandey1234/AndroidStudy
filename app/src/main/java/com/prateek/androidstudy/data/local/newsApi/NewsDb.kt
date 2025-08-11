package com.prateek.androidstudy.data.local.newsApi

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [NewsEntity::class], version = 1)
abstract class NewsDb : RoomDatabase(){
    abstract val dao: NewsDAO
}