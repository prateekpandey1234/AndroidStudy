package com.prateek.androidstudy.downloadManager.worker

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.prateek.androidstudy.downloadManager.other.Constants
import com.prateek.androidstudy.downloadManager.other.DownloadResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MediaDownloadManager(context: Context, workerParams: WorkerParameters) : CoroutineWorker(
    context,
    workerParams
) {
    override suspend fun doWork(): Result {
        val fileName = inputData.getString("fileName");
        val fileUrl = inputData.getString("fileUrl");
        val fileType = inputData.getString("fileType");
        val path = inputData.getString("filePath");
        val headerString = inputData.getStringArray("headers");
        val headers = HashMap<String, String>()
        headerString?.forEach { header ->
            val key = header.split(":")[0]
            val value = header.split(":")[1]
            if (key.isNotEmpty() && key.isNotBlank() && value.isNotEmpty() && value.isNotBlank()) {
                headers[key] = value
            }
        }
        if (fileUrl == null || fileName == null || fileType == null || path == null || fileUrl.isEmpty() || fileName.isEmpty() || fileType.isEmpty() || path.isEmpty()) {
            return Result.failure(Data.Builder().putString("error", "Invalid input").build())
        }

        val response =
            getSavedFileUri(fileName, fileType, fileUrl, path, headers, applicationContext)
        when (response) {
            is DownloadResource.Success -> {
                return Result.success(
                    Data.Builder().putString(Constants.WORKER_STATUS_KEY, response.data?.path)
                        .build()
                )
            }

            else -> {
                return Result.failure(
                    Data.Builder().putString(Constants.WORKER_STATUS_KEY, response.message).build()
                )
            }
        }
    }

    suspend fun getSavedFileUri(
        fileName: String,
        fileType: String,
        fileUrl: String,
        path: String,
        headers: Map<String, String>?,
        context: Context
    ): DownloadResource<Uri?> {
        var savedUri: Uri? = null
        val mimeType = when (fileType) {
            "PDF" -> "application/pdf"
            "PNG" -> "image/png"
            "MP4" -> "video/mp4"
            else -> ""
        }

        if (mimeType.isEmpty()) {
            return DownloadResource.Error("Unknow or wrong file type , file type -> $fileType")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/$path")
            }

            val resolver = context.contentResolver

            savedUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            if (savedUri != null) {
                val response = downloadUriWithHeaders(fileUrl, savedUri, resolver, headers)
                when (response) {
                    is DownloadResource.Success -> {
                        return DownloadResource.Success(savedUri)

                    }

                    else -> return DownloadResource.Error("Something went wrong", data1 = null)
                }

            } else return DownloadResource.Error("Something went wrong", data1 = null)
        } else {
            val target = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )
            val response = downloadFileWithHeaders(fileUrl, target, headers)

            when (response) {
                is DownloadResource.Success -> {
                    return DownloadResource.Success(target.toUri())
                }

                else -> return DownloadResource.Error("Something went wrong", data1 = null)

            }

        }


    }

    suspend fun downloadFileWithHeaders(
        fileUrl: String,
        file: File,
        headers: Map<String, String>? = emptyMap()
    ): DownloadResource<String> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null

        try {
            val url = URL(fileUrl)
            connection = url.openConnection() as HttpsURLConnection

            // Configure connection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000 // 15 seconds
            connection.readTimeout = 15000
            connection.doInput = true

            headers?.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }


            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode != HttpsURLConnection.HTTP_OK) {
                throw IOException("Server returned HTTPS $responseCode: ${connection.responseMessage}")
            }

            connection.inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output, DEFAULT_BUFFER_SIZE)
                }
            }

            Log.d("Download", "File downloaded successfully to ${file.absolutePath}")
            return@withContext DownloadResource.Success("")

        } catch (e: Exception) {
            Log.e("Download", "Error downloading file", e)
            e.printStackTrace()
            return@withContext DownloadResource.Error("Error downloading file")

        } finally {
            connection?.disconnect()
            return@withContext DownloadResource.Error("Error downloading file")

        }
    }


    suspend fun downloadUriWithHeaders(
        fileUrl: String,
        uri: Uri,
        resolver: ContentResolver,
        headers: Map<String, String>? = emptyMap()
    ): DownloadResource<String> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null

        try {
            val url = URL(fileUrl)
            connection = url.openConnection() as HttpsURLConnection

            // Configure connection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000 // 15 seconds
            connection.readTimeout = 15000
            connection.doInput = true

            headers?.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }


            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode != HttpsURLConnection.HTTP_OK) {
                throw IOException("Server returned HTTPS $responseCode: ${connection.responseMessage}")
            }

            connection.inputStream.use { input ->
                resolver.openOutputStream(uri)?.use { output ->
                    input.copyTo(output, DEFAULT_BUFFER_SIZE)
                } ?: throw IOException("Cannot open output stream")
            }

            Log.d("Download", "File downloaded successfully to $uri")
            return@withContext DownloadResource.Success("")

        } catch (e: Exception) {
            Log.e("Download", "Error downloading file", e)
            e.printStackTrace()
            return@withContext DownloadResource.Error("Error downloading file")

        } finally {
            connection?.disconnect()
            return@withContext DownloadResource.Error("Error downloading file")

        }
    }
}