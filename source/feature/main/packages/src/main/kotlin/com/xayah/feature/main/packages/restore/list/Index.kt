package com.xayah.feature.main.packages.restore.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xayah.core.ui.component.ChipRow
import com.xayah.core.ui.component.Divider
import com.xayah.core.ui.component.FilterChip
import com.xayah.core.ui.component.IconButton
import com.xayah.core.ui.component.InnerBottomSpacer
import com.xayah.core.ui.component.LocalSlotScope
import com.xayah.core.ui.component.MultipleSelectionFilterChip
import com.xayah.core.ui.component.PackageItem
import com.xayah.core.ui.component.SearchBar
import com.xayah.core.ui.component.SortChip
import com.xayah.core.ui.component.confirm
import com.xayah.core.ui.component.paddingHorizontal
import com.xayah.core.ui.component.paddingTop
import com.xayah.core.ui.component.paddingVertical
import com.xayah.core.ui.token.SizeTokens
import com.xayah.core.ui.util.LocalNavController
import com.xayah.feature.main.packages.DotLottieView
import com.xayah.feature.main.packages.ListScaffold
import com.xayah.feature.main.packages.R
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@ExperimentalLayoutApi
@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@Composable
fun PagePackagesRestoreList() {
    val context = LocalContext.current
    val navController = LocalNavController.current!!
    val dialogState = LocalSlotScope.current!!.dialogSlot
    val viewModel = hiltViewModel<IndexViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val packagesState by viewModel.packagesState.collectAsStateWithLifecycle()
    val packagesSelectedState by viewModel.packagesSelectedState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(canScroll = { false })
    val scrollState = rememberLazyListState()
    val srcPackagesEmptyState by viewModel.srcPackagesEmptyState.collectAsStateWithLifecycle()
    var fabHeight: Float by remember { mutableFloatStateOf(0F) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(null) {
        viewModel.emitIntentOnIO(IndexUiIntent.OnRefresh)
        viewModel.emitIntentOnIO(IndexUiIntent.GetUserIds)
    }

    ListScaffold(
        scrollBehavior = scrollBehavior,
        progress = if (uiState.isLoading) -1F else null,
        title = stringResource(id = R.string.backed_up_apps),
        subtitle = if (packagesSelectedState != 0) "(${packagesSelectedState}/${packagesState.size})" else null,
        actions = {
            if (srcPackagesEmptyState.not()) {
                IconButton(enabled = packagesSelectedState != 0, icon = Icons.Outlined.Delete) {
                    viewModel.launchOnIO {
                        if (dialogState.confirm(title = context.getString(R.string.prompt), text = context.getString(R.string.confirm_delete))) {
                            viewModel.emitIntent(IndexUiIntent.DeleteSelected)
                        }
                    }
                }
                IconButton(icon = Icons.Outlined.Checklist) {
                    viewModel.emitIntentOnIO(IndexUiIntent.SelectAll(uiState.selectAll.not()))
                    viewModel.emitStateOnMain(uiState.copy(selectAll = uiState.selectAll.not()))
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            AnimatedVisibility(visible = packagesSelectedState != 0, enter = scaleIn(), exit = scaleOut()) {
                FloatingActionButton(
                    modifier = Modifier.onSizeChanged { fabHeight = it.height * 1.5f },
                    onClick = {
                        viewModel.emitIntentOnIO(IndexUiIntent.ToPageSetup(navController))
                    },
                ) {
                    Icon(Icons.Filled.ChevronRight, null)
                }
            }
        }
    ) {
        if (srcPackagesEmptyState) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    Column(modifier = Modifier.paddingHorizontal(SizeTokens.Level16), horizontalAlignment = Alignment.CenterHorizontally) {
                        DotLottieView(uiState.isLoading)
                    }
                }
                InnerBottomSpacer(innerPadding = it)
            }
        } else {
            Column {
                val flagIndexState by viewModel.flagIndexState.collectAsStateWithLifecycle()
                val userIdListState by viewModel.userIdListState.collectAsStateWithLifecycle()
                val userIdIndexListState by viewModel.userIdIndexListState.collectAsStateWithLifecycle()
                val sortIndexState by viewModel.sortIndexState.collectAsStateWithLifecycle()
                val sortTypeState by viewModel.sortTypeState.collectAsStateWithLifecycle()

                Column {
                    SearchBar(
                        modifier = Modifier
                            .paddingHorizontal(SizeTokens.Level16)
                            .paddingVertical(SizeTokens.Level8),
                        enabled = true,
                        placeholder = stringResource(id = R.string.search_bar_hint_packages),
                        onTextChange = {
                            viewModel.emitIntentOnIO(IndexUiIntent.FilterByKey(key = it))
                        }
                    )

                    ChipRow(horizontalSpace = SizeTokens.Level16) {
                        SortChip(
                            enabled = true,
                            dismissOnSelected = true,
                            leadingIcon = Icons.Rounded.Sort,
                            selectedIndex = sortIndexState,
                            type = sortTypeState,
                            list = stringArrayResource(id = R.array.backup_sort_type_items_apps).toList(),
                            onSelected = { index, _ ->
                                scope.launch {
                                    scrollState.scrollToItem(0)
                                    viewModel.emitIntentOnIO(IndexUiIntent.Sort(index = index, type = sortTypeState))
                                }
                            },
                            onClick = {}
                        )

                        if (userIdListState.size > 1)
                            MultipleSelectionFilterChip(
                                enabled = true,
                                dismissOnSelected = true,
                                leadingIcon = ImageVector.vectorResource(id = R.drawable.ic_rounded_person),
                                label = stringResource(id = R.string.user),
                                selectedIndexList = userIdIndexListState,
                                list = userIdListState.map { it.toString() },
                                onSelected = { indexList ->
                                    if (indexList.isNotEmpty()) {
                                        viewModel.emitIntentOnIO(IndexUiIntent.SetUserIdIndexList(indexList))
                                    }
                                },
                                onClick = {
                                    viewModel.emitIntentOnIO(IndexUiIntent.GetUserIds)
                                }
                            )

                        FilterChip(
                            enabled = true,
                            dismissOnSelected = true,
                            leadingIcon = ImageVector.vectorResource(id = R.drawable.ic_rounded_deployed_code),
                            selectedIndex = flagIndexState,
                            list = stringArrayResource(id = R.array.flag_type_items).toList(),
                            onSelected = { index, _ ->
                                viewModel.emitIntentOnIO(IndexUiIntent.FilterByFlag(index = index))
                            },
                            onClick = {}
                        )
                    }

                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .paddingTop(SizeTokens.Level8)
                    )
                }

                LazyColumn(modifier = Modifier.fillMaxSize(), state = scrollState) {
                    item(key = "-1") {
                        Spacer(modifier = Modifier.size(SizeTokens.Level1))
                    }

                    items(items = packagesState, key = { "${uiState.uuid}-${it.id}" }) { item ->
                        Row(modifier = Modifier.animateItemPlacement()) {
                            PackageItem(
                                item = item,
                                onCheckedChange = { viewModel.emitIntentOnIO(IndexUiIntent.Select(item)) },
                                onClick = {
                                    viewModel.emitIntentOnIO(IndexUiIntent.ToPageDetail(navController, item))
                                }
                            )
                        }
                    }

                    if (packagesSelectedState != 0)
                        item {
                            with(LocalDensity.current) {
                                Column {
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(fabHeight.toDp())
                                    )
                                }
                            }
                        }

                    item {
                        InnerBottomSpacer(innerPadding = it)
                    }
                }
            }
        }
    }
}
