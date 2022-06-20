package com.xayah.databackup.data

data class Release(
    val html_url: String,
    val name: String,
    val assets: List<Asset>,
    val body: String
)

data class Asset(
    val browser_download_url: String
)

data class Issue(
    val html_url: String,
    val title: String,
    val body: String
)

data class AppInfo(
    val appName: String,
    val packageName: String,
    val version: String,
    val versionCode: String,
    val apkSize: String,
    val userSize: String,
    val dataSize: String,
    val obbSize: String
)

data class BackupInfo(
    val version: String
)

data class MediaInfo(
    val name: String,
    val path: String
)