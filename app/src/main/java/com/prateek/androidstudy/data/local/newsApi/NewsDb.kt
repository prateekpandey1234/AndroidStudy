package com.prateek.androidstudy.data.local.newsApi

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.prateek.androidstudy.data.remote.newsApi.Source


@Database(entities = [NewsEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class NewsDb : RoomDatabase(){
    abstract val dao: NewsDAO
}


class Converters {
    @TypeConverter
    fun fromSource(source: Source): String {
        return Gson().toJson(source)
    }


    @TypeConverter
    fun toSource(json:String): Source {
        return Gson().fromJson(json, Source::class.java)
    }


}