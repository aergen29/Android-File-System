package com.example.filedemo.utils

import android.content.Context
import android.os.Build
import android.os.storage.StorageManager
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.File
import java.io.Serializable

class StorageUtils(val context: Context) {

    val storageManager: StorageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
    var excHandler: (Exception)->Unit = { e ->  Log.e("StorageUtils","An undhandled error occured",e) }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getAppSpecificCacheLimit(): Long {
        try {
            val cacheUuid = storageManager.getUuidForPath(context.cacheDir)
            val allocatableCacheBytes = storageManager.getAllocatableBytes(cacheUuid)
            return allocatableCacheBytes
        } catch (e: Exception) {
            excHandler(e)
            return 0
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getAppSpecificPersistentLimit(): Long {
        try {
            val persistentUuid = storageManager.getUuidForPath(context.filesDir)
            val allocatablePersistentBytes = storageManager.getAllocatableBytes(persistentUuid)
            return allocatablePersistentBytes
        } catch (e: Exception) {
            excHandler(e)
            return 0
        }
    }

    fun getAppSpecificPersistentFiles(isExternal: Boolean = false): List<File> {
        val files = if (isExternal) context.getExternalFilesDir(null)?.listFiles() else context.filesDir?.listFiles()
        if (files == null) {
            excHandler(Exception("Cannot list files!"))
            return listOf()
        }
        return files.asList()
    }

    fun getAppSpecificCacheFiles(isExternal: Boolean): List<File> {
        val files = if (isExternal) context.externalCacheDir?.listFiles() else context.cacheDir?.listFiles()

        if (files == null) {
            excHandler(Exception("Cannot list cache files!"))
            return listOf()
        }
        return files.asList()
    }

    fun writePersistentFile(name: String, content: String, isExternal: Boolean) {
        val file: File = File(
            if (isExternal) context.getExternalFilesDir(null) else context.filesDir,
            name)
        file.writeText(content)
    }

    fun writeCacheFile(name: String, content: String, isExternal: Boolean) {
        val cacheFile: File = File(
            if (isExternal) context.externalCacheDir else context.cacheDir,
            name)
        cacheFile.writeText(content)
    }
}