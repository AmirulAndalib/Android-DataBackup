package com.xayah.feature.main.home.common.model

import com.xayah.core.ui.model.ImageVectorToken
import com.xayah.core.ui.model.StringResourceToken
import kotlinx.coroutines.flow.Flow


data class MapItem(
    val key: StringResourceToken,
    val value: Flow<String>,
)

data class FossActivityCardItem(
    val label: StringResourceToken,
    val icon: ImageVectorToken,
    val details: List<MapItem>,
    val onClick: () -> Unit,
)

data class PremiumActivityCardItem(
    val label: StringResourceToken,
    val icon: ImageVectorToken,
    val details: List<MapItem>,
    val onClick: () -> Unit,
    val onCloudBtnClick: () -> Unit,
)

data class UtilityChipItem(
    val label: StringResourceToken,
    val icon: ImageVectorToken,
    val onClick: () -> Unit,
)
