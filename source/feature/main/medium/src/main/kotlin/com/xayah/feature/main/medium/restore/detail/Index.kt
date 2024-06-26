package com.xayah.feature.main.medium.restore.detail

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xayah.core.model.DataType
import com.xayah.core.model.util.formatSize
import com.xayah.core.ui.component.BodyMediumText
import com.xayah.core.ui.component.FilledIconButton
import com.xayah.core.ui.component.LocalSlotScope
import com.xayah.core.ui.component.MediaIconImage
import com.xayah.core.ui.component.Switchable
import com.xayah.core.ui.component.Title
import com.xayah.core.ui.component.TitleLargeText
import com.xayah.core.ui.component.confirm
import com.xayah.core.ui.component.paddingBottom
import com.xayah.core.ui.component.paddingHorizontal
import com.xayah.core.ui.component.paddingStart
import com.xayah.core.ui.component.paddingTop
import com.xayah.core.ui.material3.toColor
import com.xayah.core.ui.material3.tokens.ColorSchemeKeyTokens
import com.xayah.core.ui.model.ImageVectorToken
import com.xayah.core.ui.model.StringResourceToken
import com.xayah.core.ui.token.SizeTokens
import com.xayah.core.ui.util.LocalNavController
import com.xayah.core.ui.util.fromDrawable
import com.xayah.core.ui.util.fromString
import com.xayah.core.ui.util.fromStringArgs
import com.xayah.core.ui.util.fromStringId
import com.xayah.core.ui.util.fromVector
import com.xayah.core.ui.util.value
import com.xayah.core.util.SymbolUtil
import com.xayah.core.util.withMainContext
import com.xayah.feature.main.medium.ListScaffold
import com.xayah.feature.main.medium.R

@ExperimentalFoundationApi
@ExperimentalLayoutApi
@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@Composable
fun PageMediumRestoreDetail() {
    val context = LocalContext.current
    val navController = LocalNavController.current!!
    val dialogState = LocalSlotScope.current!!.dialogSlot
    val viewModel = hiltViewModel<IndexViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mediaState by viewModel.mediaState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(null) {
        viewModel.emitIntentOnIO(IndexUiIntent.OnRefresh)
    }

    ListScaffold(
        scrollBehavior = scrollBehavior,
        snackbarHostState = viewModel.snackbarHostState,
        title = StringResourceToken.fromStringId(R.string.details),
        actions = {}
    ) {
        mediaState?.also { media ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                Row(
                    modifier = Modifier
                        .paddingHorizontal(SizeTokens.Level24)
                        .paddingTop(SizeTokens.Level12),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level24)
                ) {
                    MediaIconImage(name = uiState.name.firstOrNull()?.toString() ?: "", textStyle = MaterialTheme.typography.titleLarge, size = SizeTokens.Level64)
                    Column(modifier = Modifier.weight(1f)) {
                        TitleLargeText(text = media.name, color = ColorSchemeKeyTokens.OnSurface.toColor())
                        BodyMediumText(
                            text = (if (media.path.isEmpty()) StringResourceToken.fromStringId(R.string.specify_a_path) else StringResourceToken.fromString(media.path)).value,
                            color = (if (media.path.isEmpty()) ColorSchemeKeyTokens.Error else ColorSchemeKeyTokens.OnSurfaceVariant).toColor()
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .paddingHorizontal(SizeTokens.Level24)
                        .paddingBottom(SizeTokens.Level12),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level4)
                ) {
                    Spacer(modifier = Modifier.paddingStart(SizeTokens.Level68))
                    FilledIconButton(
                        enabled = true,
                        icon = ImageVectorToken.fromDrawable(R.drawable.ic_rounded_folder_open),
                        containerColor = ColorSchemeKeyTokens.BluePrimaryContainer,
                        contentColor = ColorSchemeKeyTokens.BlueOnPrimaryContainer
                    ) {
                        viewModel.emitIntentOnIO(IndexUiIntent.SetPath(context = context, mediaEntity = media))
                    }
                    FilledIconButton(
                        enabled = media.preserveId == 0L,
                        icon = ImageVectorToken.fromVector(Icons.Outlined.Shield),
                        containerColor = ColorSchemeKeyTokens.YellowPrimaryContainer,
                        contentColor = ColorSchemeKeyTokens.YellowOnPrimaryContainer
                    ) {
                        viewModel.launchOnIO {
                            if (dialogState.confirm(title = StringResourceToken.fromStringId(R.string.protect), text = StringResourceToken.fromStringId(R.string.protect_desc))) {
                                viewModel.emitIntent(IndexUiIntent.Preserve(mediaEntity = media))
                                withMainContext {
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                    FilledIconButton(
                        enabled = true,
                        icon = ImageVectorToken.fromVector(Icons.Outlined.Delete),
                        containerColor = ColorSchemeKeyTokens.ErrorContainer,
                        contentColor = ColorSchemeKeyTokens.OnErrorContainer
                    ) {
                        viewModel.launchOnIO {
                            if (dialogState.confirm(title = StringResourceToken.fromStringId(R.string.delete), text = StringResourceToken.fromStringId(R.string.delete_desc))) {
                                viewModel.emitIntent(IndexUiIntent.Delete(mediaEntity = media))
                                withMainContext {
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                }
                Title(title = StringResourceToken.fromStringId(R.string.backup_parts)) {
                    Switchable(
                        enabled = media.extraInfo.existed && media.path.isNotEmpty(),
                        checked = media.extraInfo.activated,
                        icon = ImageVectorToken.fromDrawable(R.drawable.ic_rounded_image),
                        title = StringResourceToken.fromString(DataType.MEDIA_MEDIA.type.uppercase()),
                        checkedText = if (uiState.isCalculating)
                            StringResourceToken.fromStringArgs(
                                StringResourceToken.fromString(media.displayStatsBytes.formatSize()),
                                StringResourceToken.fromString(SymbolUtil.DOT.toString()),
                                StringResourceToken.fromStringId(R.string.calculating),
                            )
                        else
                            StringResourceToken.fromString(media.displayStatsBytes.formatSize()),
                    ) {
                        viewModel.emitIntentOnIO(IndexUiIntent.UpdateMedia(media.copy(extraInfo = media.extraInfo.copy(activated = media.extraInfo.activated.not()))))
                    }
                }
            }
        }
    }
}
