package com.mindset.presentation

import androidx.compose.runtime.Immutable


@Immutable
data class TemplateHyroxDetailUiState(
    val title: String,
    val badge: String,
    val duration: String,
    val description: String,
    val division: HyroxDivision,
    val variant: HyroxVariant,
    val showVariantSelector: Boolean,
    val blocks: List<HyroxBlock>,
    val finishLabel: String,
)

enum class HyroxGlyph {
    SKI_ERG,
    SLED_PUSH,
    SLED_PULL,
    BURPEE,
    ROWING,
    FARMERS_CARRY,
    SANDBAG_LUNGES,
    WALL_BALLS,
    RUN,
}

enum class HyroxRowKind { STATION, RUN }

@Immutable
data class HyroxRow(
    val kind: HyroxRowKind,
    val glyph: HyroxGlyph,
    val title: String,
    val detail: String,
    val value: String,
)

@Immutable
data class HyroxBlock(val label: String, val rows: List<HyroxRow>)
