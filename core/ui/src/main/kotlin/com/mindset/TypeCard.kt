package com.mindset

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindset.model.SessionType

/*
 * Session-type picker card + its option model, shared by the New Session (logging) and New Template
 * (templates) screens. Promoted here from NewSessionScreen (B11). `type` stays a String (the value
 * the screens hand to their ViewModels), seeded from `model.SessionType` names.
 */

data class TypeOption(val type: String, val badge: String, val title: String, val subtitle: String)

val typeOptions = listOf(
    TypeOption(SessionType.STRENGTH.name, "S", "Strength", "Sets · reps · load"),
    TypeOption(SessionType.CONDITIONING.name, "C", "Conditioning", "Time · distance"),
    TypeOption(SessionType.HYROX.name, "H", "Hyrox", "8 stations + runs"),
    TypeOption(SessionType.MIXED.name, "M", "Mixed", "Strength + cardio"),
)

@Composable
fun TypeCard(option: TypeOption, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .border(2.dp, if (selected) Accent else Color.Transparent, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (selected) Accent else SurfaceHi),
            contentAlignment = Alignment.Center,
        ) {
            Text(option.badge, color = if (selected) OnAccent else Accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        Text(option.title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Text(option.subtitle, color = TextSecondary, fontSize = 12.sp)
    }
}
