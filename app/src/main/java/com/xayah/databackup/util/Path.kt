package com.xayah.databackup.util

import android.content.Context

class Path {
    companion object {
        fun getFilesDir(context: Context): String {
            return context.filesDir.path
        }

        fun getExternalFilesDir(context: Context): String? {
            return context.getExternalFilesDir(null)?.path
        }
    }
}