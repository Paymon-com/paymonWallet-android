package ru.paymon.android.filepicker;

import android.content.ContentResolver
import android.database.Cursor
import android.os.AsyncTask
import android.provider.BaseColumns._ID
import android.provider.MediaStore

import java.util.ArrayList

import android.provider.MediaStore.MediaColumns.DATA
import ru.paymon.android.filepicker.utils.FileResultCallback

/**
 * Created by droidNinja on 01/08/16.
 */
class PhotoScannerTask(private val contentResolver: ContentResolver, private val resultCallback: FileResultCallback<FileDirectory>?) : AsyncTask<Void, Void, MutableList<FileDirectory>>() {

    override fun doInBackground(vararg voids: Void): MutableList<FileDirectory> {
        val projection = null
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val sortOrder = MediaStore.Images.Media._ID + " DESC"
        val selection = null

        val cursor = contentResolver.query(uri, projection, selection, null, sortOrder)

        if (cursor != null) {
            val data = getPhotoDirectories(cursor)
            cursor.close()
            return data
        }

        return mutableListOf()
    }

    override fun onPostExecute(result: MutableList<FileDirectory>?) {
        super.onPostExecute(result)
        result?.let {
            resultCallback?.onResultCallback(it.toList())
        }
    }

    private fun getPhotoDirectories(data: Cursor): MutableList<FileDirectory> {
        val directories = ArrayList<FileDirectory>()

        while (data.moveToNext()) {
            val imageId = data.getInt(data.getColumnIndexOrThrow(_ID))
            val path = data.getString(data.getColumnIndexOrThrow(DATA))
            val fileName = data.getString(data.getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE))

            val photoDirectory = FileDirectory()

            if (!directories.contains(photoDirectory)) {
                photoDirectory.addPhoto(imageId, fileName, path, MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
                directories.add(photoDirectory)
            } else {
                directories[directories.indexOf(photoDirectory)].addPhoto(imageId, fileName, path, MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
            }
        }

        return directories
    }
}
