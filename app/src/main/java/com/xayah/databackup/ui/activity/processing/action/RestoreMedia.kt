package com.xayah.databackup.ui.activity.processing.action

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.xayah.databackup.R
import com.xayah.databackup.data.CompressionType
import com.xayah.databackup.data.DataType
import com.xayah.databackup.data.LoadingState
import com.xayah.databackup.data.TaskState
import com.xayah.databackup.ui.activity.processing.ProcessingViewModel
import com.xayah.databackup.ui.activity.processing.components.ProcessObjectItem
import com.xayah.databackup.ui.activity.processing.components.ProcessingTask
import com.xayah.databackup.ui.activity.processing.components.onInfoUpdate
import com.xayah.databackup.util.*
import com.xayah.databackup.util.command.Command
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun onRestoreMediaProcessing(viewModel: ProcessingViewModel, context: Context, globalObject: GlobalObject, retry: Boolean = false) {
    if (viewModel.isFirst.value) {
        viewModel.isFirst.value = false
        CoroutineScope(Dispatchers.IO).launch {
            val tag = "# RestoreMedia #"
            Logcat.getInstance().actionLogAddLine(tag, "===========${tag}===========")

            val loadingState = viewModel.loadingState
            val progress = viewModel.progress
            val topBarTitle = viewModel.topBarTitle
            val taskList = viewModel.taskList.value
            val objectList = viewModel.objectList.value.apply {
                clear()
                add(ProcessObjectItem(type = DataType.DATA))
            }
            val allDone = viewModel.allDone

            // Check global map
            if (globalObject.mediaInfoRestoreMap.value.isEmpty()) {
                globalObject.mediaInfoRestoreMap.emit(Command.getMediaInfoRestoreMap())
            }
            Logcat.getInstance().actionLogAddLine(tag, "Global map check finished.")

            if (retry.not()) {
                // Add processing tasks
                taskList.addAll(
                    globalObject.mediaInfoRestoreMap.value.values.toList()
                        .filter { if (it.detailRestoreList.isNotEmpty()) it.selectData.value else false }
                        .map {
                            ProcessingTask(
                                appName = it.name,
                                packageName = it.path.ifEmpty { it.name },
                                appIcon = it.icon,
                                selectApp = false,
                                selectData = true,
                                objectList = listOf()
                            )
                        })
            } else {
                Logcat.getInstance().actionLogAddLine(tag, "Retrying.")
            }

            Logcat.getInstance().actionLogAddLine(tag, "Task added, size: ${taskList.size}.")

            // Early stage finished
            loadingState.value = LoadingState.Success
            topBarTitle.value = "${context.getString(R.string.restoring)}(${progress.value}/${taskList.size})"
            for ((index, i) in taskList.withIndex()) {
                // Skip while retrying not-failed task
                if (retry && i.taskState.value != TaskState.Failed) continue

                // Reset object list
                objectList[0].apply {
                    state.value = TaskState.Waiting
                    title.value = GlobalString.ready
                    visible.value = false
                    subtitle.value = GlobalString.pleaseWait
                }

                // Enter processing state
                i.taskState.value = TaskState.Processing
                val mediaInfoRestore = globalObject.mediaInfoRestoreMap.value[i.appName]!!

                // Scroll to processing task
                if (viewModel.listStateIsInitialized) {
                    if (viewModel.scopeIsInitialized) {
                        viewModel.scope.launch {
                            viewModel.listState.animateScrollToItem(index)
                        }
                    }
                }

                var isSuccess = true
                val inPath = "${Path.getBackupMediaSavePath()}/${i.appName}/${mediaInfoRestore.date}"

                Logcat.getInstance().actionLogAddLine(tag, "Name: ${i.appName}.")
                Logcat.getInstance().actionLogAddLine(tag, "Path: ${i.packageName}.")

                if (i.selectData) {
                    objectList[0].visible.value = true
                }
                for (j in objectList) {
                    if (viewModel.isCancel.value) break
                    if (j.visible.value) {
                        j.state.value = TaskState.Processing
                        when (j.type) {
                            DataType.DATA -> {
                                val inputPath = "${inPath}/${i.appName}.tar"
                                Command.decompress(CompressionType.TAR, DataType.MEDIA, inputPath, i.appName, i.packageName.replace("/${i.appName}", ""))
                                { type, line ->
                                    onInfoUpdate(type, line ?: "", j)
                                }.apply {
                                    if (!this) isSuccess = false
                                }
                            }
                            else -> {
                                isSuccess = false
                            }
                        }
                    }
                }
                if (viewModel.isCancel.value) break

                i.apply {
                    if (isSuccess) {
                        if (context.readIsResetRestoreList()) {
                            if (mediaInfoRestore.selectData.value) mediaInfoRestore.selectData.value = false
                        }
                        this.taskState.value = TaskState.Success
                    } else {
                        this.taskState.value = TaskState.Failed
                    }
                    val list = mutableListOf<ProcessObjectItem>()
                    for (j in objectList) {
                        list.add(
                            ProcessObjectItem(
                                state = mutableStateOf(j.state.value),
                                visible = mutableStateOf(j.visible.value),
                                title = mutableStateOf(j.title.value),
                                subtitle = mutableStateOf(j.subtitle.value),
                                type = j.type,
                            )
                        )
                    }
                    this.objectList = list.toList()
                }

                progress.value += 1
                topBarTitle.value = "${context.getString(R.string.restoring)}(${progress.value}/${taskList.size})"
            }

            topBarTitle.value = "${context.getString(R.string.restore_finished)}!"
            allDone.targetState = true
            Logcat.getInstance().actionLogAddLine(tag, "===========${tag}===========")
        }
    }
}
