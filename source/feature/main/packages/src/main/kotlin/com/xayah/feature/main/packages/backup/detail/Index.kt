package com.xayah.feature.main.packages.backup.detail

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.xayah.core.ui.component.Clickable
import com.xayah.core.ui.component.PackageIconImage
import com.xayah.core.ui.component.Switchable
import com.xayah.core.ui.component.Title
import com.xayah.core.ui.component.TitleLargeText
import com.xayah.core.ui.component.paddingHorizontal
import com.xayah.core.ui.component.paddingVertical
import com.xayah.core.ui.material3.toColor
import com.xayah.core.ui.material3.tokens.ColorSchemeKeyTokens
import com.xayah.core.ui.model.ImageVectorToken
import com.xayah.core.ui.model.StringResourceToken
import com.xayah.core.ui.token.SizeTokens
import com.xayah.core.ui.util.fromDrawable
import com.xayah.core.ui.util.fromString
import com.xayah.core.ui.util.fromStringArgs
import com.xayah.core.ui.util.fromStringId
import com.xayah.core.util.DateUtil
import com.xayah.core.util.SymbolUtil
import com.xayah.feature.main.packages.ListScaffold
import com.xayah.feature.main.packages.R
import com.xayah.feature.main.packages.countItems
import com.xayah.feature.main.packages.reversePermission
import com.xayah.feature.main.packages.reverseSsaid
import com.xayah.feature.main.packages.reversedPackage

@ExperimentalFoundationApi
@ExperimentalLayoutApi
@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@Composable
fun PagePackagesBackupDetail() {
    val context = LocalContext.current
    val viewModel = hiltViewModel<IndexViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val packageState by viewModel.packageState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(null) {
        viewModel.emitIntentOnIO(IndexUiIntent.OnRefresh)
    }

    ListScaffold(
        scrollBehavior = scrollBehavior,
        title = StringResourceToken.fromStringId(R.string.details),
        actions = {}
    ) {
        packageState?.also { pkg ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                Row(
                    modifier = Modifier
                        .paddingHorizontal(SizeTokens.Level24)
                        .paddingVertical(SizeTokens.Level12),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level24)
                ) {
                    PackageIconImage(
                        packageName = uiState.packageName,
                        label = "${pkg.packageInfo.label.firstOrNull() ?: ""}",
                        textStyle = MaterialTheme.typography.titleLarge,
                        size = SizeTokens.Level64
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        TitleLargeText(text = pkg.packageInfo.label, color = ColorSchemeKeyTokens.OnSurface.toColor())
                        BodyMediumText(text = uiState.packageName, color = ColorSchemeKeyTokens.OnSurfaceVariant.toColor())
                    }
                }
                Title(title = StringResourceToken.fromStringId(R.string.info)) {
                    Clickable(
                        title = StringResourceToken.fromStringId(R.string.user),
                        value = StringResourceToken.fromString(uiState.userId.toString()),
                    )
                    Clickable(
                        title = StringResourceToken.fromStringId(R.string.uid),
                        value = StringResourceToken.fromString(pkg.extraInfo.uid.toString())
                    )
                    Clickable(
                        title = StringResourceToken.fromStringId(R.string.version),
                        value = StringResourceToken.fromString(pkg.packageInfo.versionName),
                    )
                    Clickable(
                        title = StringResourceToken.fromStringId(R.string.first_install),
                        value = StringResourceToken.fromString(
                            DateUtil.formatTimestamp(
                                pkg.packageInfo.firstInstallTime,
                                "yyyy-MM-dd"
                            )
                        ),
                    )
                }
                Title(title = StringResourceToken.fromStringId(R.string.backup_parts)) {
                    Switchable(
                        checked = pkg.permissionSelected,
                        title = StringResourceToken.fromStringId(R.string.permissions),
                        checkedText = StringResourceToken.fromString(countItems(context, pkg.extraInfo.permissions.size)),
                    ) {
                        viewModel.emitIntentOnIO(IndexUiIntent.UpdatePackage(pkg.reversePermission()))
                    }
                    Switchable(
                        enabled = pkg.extraInfo.ssaid.isNotEmpty(),
                        checked = pkg.ssaidSelected,
                        title = StringResourceToken.fromStringId(R.string.ssaid),
                        checkedText = if (pkg.extraInfo.ssaid.isNotEmpty()) StringResourceToken.fromString(pkg.extraInfo.ssaid) else StringResourceToken.fromStringId(R.string.none),
                    ) {
                        viewModel.emitIntentOnIO(IndexUiIntent.UpdatePackage(pkg.reverseSsaid()))
                    }
                    Switchable(
                        checked = pkg.apkSelected,
                        icon = ImageVectorToken.fromDrawable(R.drawable.ic_rounded_android),
                        title = StringResourceToken.fromString(DataType.PACKAGE_APK.type.uppercase()),
                        checkedText = if (uiState.isCalculating)
                            StringResourceToken.fromStringArgs(
                                StringResourceToken.fromString(pkg.displayStats.apkBytes.toDouble().formatSize()),
                                StringResourceToken.fromString(SymbolUtil.DOT.toString()),
                                StringResourceToken.fromStringId(R.string.calculating),
                            )
                        else
                            StringResourceToken.fromString(pkg.displayStats.apkBytes.toDouble().formatSize()),
                    ) {
                        viewModel.emitIntentOnIO(IndexUiIntent.UpdatePackage(pkg.reversedPackage(DataType.PACKAGE_APK)))
                    }
                    Switchable(
                        checked = pkg.userSelected,
                        icon = ImageVectorToken.fromDrawable(R.drawable.ic_rounded_person),
                        title = StringResourceToken.fromString(DataType.PACKAGE_USER.type.uppercase()),
                        checkedText = if (uiState.isCalculating)
                            StringResourceToken.fromStringArgs(
                                StringResourceToken.fromString(pkg.displayStats.userBytes.toDouble().formatSize()),
                                StringResourceToken.fromString(SymbolUtil.DOT.toString()),
                                StringResourceToken.fromStringId(R.string.calculating),
                            )
                        else
                            StringResourceToken.fromString(pkg.displayStats.userBytes.toDouble().formatSize()),
                    ) {
                        viewModel.emitIntentOnIO(IndexUiIntent.UpdatePackage(pkg.reversedPackage(DataType.PACKAGE_USER)))
                    }
                    Switchable(
                        checked = pkg.userDeSelected,
                        icon = ImageVectorToken.fromDrawable(R.drawable.ic_rounded_manage_accounts),
                        title = StringResourceToken.fromString(DataType.PACKAGE_USER_DE.type.uppercase()),
                        checkedText = if (uiState.isCalculating)
                            StringResourceToken.fromStringArgs(
                                StringResourceToken.fromString(pkg.displayStats.userDeBytes.toDouble().formatSize()),
                                StringResourceToken.fromString(SymbolUtil.DOT.toString()),
                                StringResourceToken.fromStringId(R.string.calculating),
                            )
                        else
                            StringResourceToken.fromString(pkg.displayStats.userDeBytes.toDouble().formatSize()),
                    ) {
                        viewModel.emitIntentOnIO(IndexUiIntent.UpdatePackage(pkg.reversedPackage(DataType.PACKAGE_USER_DE)))
                    }
                    Switchable(
                        checked = pkg.dataSelected,
                        icon = ImageVectorToken.fromDrawable(R.drawable.ic_rounded_database),
                        title = StringResourceToken.fromString(DataType.PACKAGE_DATA.type.uppercase()),
                        checkedText = if (uiState.isCalculating)
                            StringResourceToken.fromStringArgs(
                                StringResourceToken.fromString(pkg.displayStats.dataBytes.toDouble().formatSize()),
                                StringResourceToken.fromString(SymbolUtil.DOT.toString()),
                                StringResourceToken.fromStringId(R.string.calculating),
                            )
                        else
                            StringResourceToken.fromString(pkg.displayStats.dataBytes.toDouble().formatSize()),
                    ) {
                        viewModel.emitIntentOnIO(IndexUiIntent.UpdatePackage(pkg.reversedPackage(DataType.PACKAGE_DATA)))
                    }
                    Switchable(
                        checked = pkg.obbSelected,
                        icon = ImageVectorToken.fromDrawable(R.drawable.ic_rounded_stadia_controller),
                        title = StringResourceToken.fromString(DataType.PACKAGE_OBB.type.uppercase()),
                        checkedText = if (uiState.isCalculating)
                            StringResourceToken.fromStringArgs(
                                StringResourceToken.fromString(pkg.displayStats.obbBytes.toDouble().formatSize()),
                                StringResourceToken.fromString(SymbolUtil.DOT.toString()),
                                StringResourceToken.fromStringId(R.string.calculating),
                            )
                        else
                            StringResourceToken.fromString(pkg.displayStats.obbBytes.toDouble().formatSize()),
                    ) {
                        viewModel.emitIntentOnIO(IndexUiIntent.UpdatePackage(pkg.reversedPackage(DataType.PACKAGE_OBB)))
                    }
                    Switchable(
                        checked = pkg.mediaSelected,
                        icon = ImageVectorToken.fromDrawable(R.drawable.ic_rounded_image),
                        title = StringResourceToken.fromString(DataType.PACKAGE_MEDIA.type.uppercase()),
                        checkedText = if (uiState.isCalculating)
                            StringResourceToken.fromStringArgs(
                                StringResourceToken.fromString(pkg.displayStats.mediaBytes.toDouble().formatSize()),
                                StringResourceToken.fromString(SymbolUtil.DOT.toString()),
                                StringResourceToken.fromStringId(R.string.calculating),
                            )
                        else
                            StringResourceToken.fromString(pkg.displayStats.mediaBytes.toDouble().formatSize()),
                    ) {
                        viewModel.emitIntentOnIO(IndexUiIntent.UpdatePackage(pkg.reversedPackage(DataType.PACKAGE_MEDIA)))
                    }
                }
            }
        }
    }
}
