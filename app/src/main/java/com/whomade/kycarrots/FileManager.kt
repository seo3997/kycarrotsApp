// FileManager.kt
package com.whomade.kycarrots

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log

class FileManager private constructor(private val mContext: Context) {

    var photoUri: Uri? = null

    companion object {
        private const val LOG_TAG = "temp"

        @Volatile private var sFileManager: FileManager? = null

        fun get(context: Context): FileManager {
            return sFileManager ?: synchronized(this) {
                sFileManager ?: FileManager(context.applicationContext).also { sFileManager = it }
            }
        }
    }

    fun getRealPathFromURI(context: Context, contentUri: Uri): String {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            if (cursor == null) return ""
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex) ?: ""
        } catch (e: Exception) {
            Log.e(LOG_TAG, "getRealPathFromURI Exception : $e")
            ""
        } finally {
            cursor?.close()
        }
    }
}
