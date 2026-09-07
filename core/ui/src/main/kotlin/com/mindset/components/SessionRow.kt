package com.mindset.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mindset.MindSetIcons
import com.mindset.domain.Units
import com.mindset.domain.WeightUnit
import com.mindset.glassSurface
import com.mindset.icons.Bolt
import com.mindset.icons.ChevronRight
import com.mindset.icons.Dumbbell
import com.mindset.icons.Lightning
import com.mindset.icons.Run
import com.mindset.model.Session
import com.mindset.model.SessionType
import com.mindset.spacing
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun SessionRow(
    session: Session,
    volumeKg: Double,
    durationSec: Int?,
    onClick: () -> Unit,
    isPb: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.spacing.xs)
            .glassSurface()
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
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(MaterialTheme.spacing.sm),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    typeIcon(session.type.name),
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
            ) {
                Text(
                    text = session.name,
                    style = MaterialTheme.typography.bodyMedium,
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

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            if (durationSec != null && durationSec > 0) {
                Text(
                    text = formatDuration(durationSec * 1000L),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface,
                )
            }

            if (isPb) PbTag()
        }

        IconButton(
            onClick = onClick,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                MindSetIcons.ChevronRight,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PbTag() {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier
            .clip(CircleShape)
            .background(colors.primary.copy(alpha = 0.15f))
            .padding(horizontal = MaterialTheme.spacing.sm, vertical = MaterialTheme.spacing.xs),
    ) {
        Text(
            "PB",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = colors.primary,
        )
    }
}


/**
 * Live count-up clock from **milliseconds**: "mm:ss", or "h:mm:ss" past an hour. Used by the race
 * timer sheet, the minimized pill, the Home card and the notification.
 *
 * Note the sibling [com.mindset.domain.formatClockSec], which takes **seconds** and renders an
 * unpadded "m:ss" for read-only summaries. Both used to be called `formatClock` and both take a
 * `Long`, so picking the wrong import compiled silently and rendered times 1000x off — hence the
 * unit in each name.
 */
fun formatClockMs(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) {
        h.toString() + ":" + m.toString().padStart(2, '0') + ":" + s.toString().padStart(2, '0')
    } else {
        m.toString().padStart(2, '0') + ":" + s.toString().padStart(2, '0')
    }
}

/** Compact workout duration: "1h 25m" past an hour, else "25m 10s". */
fun formatDuration(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m ${s}s"
}

/** Compact volume in the user's [unit]: 12,400 → "12.4k", 850 → "850". */
fun formatVolume(kg: Double, unit: WeightUnit): String {
    val v = Units.toDisplay(kg, unit).toInt()
    return if (v >= 1000) "${(v / 100) / 10.0}k" else v.toString()
}

/** Readable session-type label (AccentPill uppercases it). */
fun typeLabel(type: String): String = when (type) {
    SessionType.CONDITIONING.name -> "Conditioning"
    SessionType.HYROX.name -> "Hyrox Simulation"
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
    SessionType.CONDITIONING.name -> MindSetIcons.Run
    SessionType.HYROX.name -> MindSetIcons.Lightning
    SessionType.MIXED.name -> MindSetIcons.Bolt
    else -> MindSetIcons.Dumbbell
}

/** "Today" / "Yesterday" / "Sun, 12 Jul" — day-relative, computed in the Android UI layer. */
fun relativeDate(epochMillis: Long): String {
    val cal = Calendar.getInstance()
    val startOfToday = cal.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val dayMs = 86_400_000L
    return when (((startOfToday - epochMillis) / dayMs)) {
        in Long.MIN_VALUE..-1L, 0L -> "Today"
        1L -> "Yesterday"
        else -> SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date(epochMillis))
    }
}
