package com.prateek.androidstudy.downloadManager.other



sealed class DownloadResource<T>(
    val data:T?=null,
    val message:String?=null
) {
    //only loading the data when there is successful api calls
    class Success<T>(data1 :T?):DownloadResource<T>(data1)

    class Error<T>(message:String,data1:T?=null):DownloadResource<T>(data1,message)

    class Loading<T> : DownloadResource<T>()

}