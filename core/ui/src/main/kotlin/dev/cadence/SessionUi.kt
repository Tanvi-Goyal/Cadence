package dev.cadence

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import dev.cadence.domain.Units
import dev.cadence.domain.WeightUnit
import dev.cadence.model.Session
import dev.cadence.model.SessionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/*
 * Shared session-list UI + formatting used across the home, history, stats and templates features.
 * Promoted here from HomeScreen (B11) so features can share them without depending on each other.
 * The type helpers key off `model.SessionType` names so this module needs no dependency on the
 * database layer.
 */

/** A tappable session row: type medallion + name + relative date, with optional volume. */
@OptIn(kotlin.time.ExperimentalTime::class)
@Composable
fun SessionRow(session: Session, volumeKg: Double, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(MaterialTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
            IconMedallion(icon = typeIcon(session.type.name))
            Column {
                Text(
                    text = session.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = relativeDate(session.startedAt.toEpochMilliseconds()),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                )
            }
        }
        if (volumeKg > 0.0) {
            val unit = LocalWeightUnit.current
            Text(
                text = "${formatVolume(volumeKg, unit)} ${Units.label(unit)}",
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface,
            )
        }
    }
}

/** Compact volume in the user's [unit]: 12,400 → "12.4k", 850 → "850". */
fun formatVolume(kg: Double, unit: WeightUnit): String {
    val v = Units.toDisplay(kg, unit).toInt()
    return if (v >= 1000) "${(v / 100) / 10.0}k" else v.toString()
}

/** Readable session-type label (AccentPill uppercases it). */
fun typeLabel(type: String): String = when (type) {
    SessionType.CONDITIONING.name -> "Conditioning"
    SessionType.HYROX.name -> "Hyrox"
    SessionType.MIXED.name -> "Mixed"
    else -> "Strength"
}

/** Single-letter session-type badge (still used by TemplatesScreen). */
fun typeBadge(type: String): String = when (type) {
    SessionType.CONDITIONING.name -> "C"
    SessionType.HYROX.name -> "H"
    SessionType.MIXED.name -> "M"
    else -> "S"
}

/** Session-type medallion icon. */
fun typeIcon(type: String): ImageVector = when (type) {
    SessionType.CONDITIONING.name -> CadenceIcons.Run
    SessionType.HYROX.name -> CadenceIcons.Lightning
    SessionType.MIXED.name -> CadenceIcons.Bolt
    else -> CadenceIcons.Dumbbell
}

/** "Today" / "Yesterday" / "Sun, 12 Jul" — day-relative, computed in the Android UI layer. */
fun relativeDate(epochMillis: Long): String {
    val cal = Calendar.getInstance()
    val startOfToday = cal.apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val dayMs = 86_400_000L
    return when (((startOfToday - epochMillis) / dayMs)) {
        in Long.MIN_VALUE..-1L, 0L -> "Today"
        1L -> "Yesterday"
        else -> SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date(epochMillis))
    }
}
