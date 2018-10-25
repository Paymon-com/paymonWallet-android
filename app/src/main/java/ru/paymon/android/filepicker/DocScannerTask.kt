package ru.paymon.android.filepicker

import android.content.ContentResolver
import android.database.Cursor
import android.os.AsyncTask
import android.provider.BaseColumns
import android.provider.MediaStore
import ru.paymon.android.filepicker.models.Document
import ru.paymon.android.filepicker.utils.FileResultCallback
import java.util.ArrayList

class DocScannerTask(val contentResolver: ContentResolver, private val resultCallback: FileResultCallback<Document>?) : AsyncTask<Void, Void, MutableList<Document>>() {

    override fun doInBackground(vararg voids: Void): MutableList<Document> {
        val projection = null
        val uri = MediaStore.Files.getContentUri("external")
        val sortOrder = MediaStore.Files.FileColumns._ID + " DESC"
        var selection = null

        val cursor = contentResolver.query(uri, projection, selection, null, sortOrder)

        if (cursor != null) {
            val data = getDocDirectories(cursor)
            cursor.close()
            return data
        }

        return mutableListOf()
    }

    override fun onPostExecute(result: MutableList<Document>?) {
        super.onPostExecute(result)
        result?.let {
            resultCallback?.onResultCallback(it.toList())
        }
    }

    private fun getDocDirectories(data: Cursor): MutableList<Document> {
        val documents = ArrayList<Document>()

        while (data.moveToNext()) {
            val id = data.getInt(data.getColumnIndexOrThrow(BaseColumns._ID))
            val path = data.getString(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA))
            val fileName = data.getString(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE))
            documents.add(Document(id, fileName, path))
        }

        return documents
    }
}
