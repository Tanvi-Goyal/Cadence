package com.mindset

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Open-data attribution for the bundled/enriched exercise content (CC-BY-SA requires this). */
@Composable
fun CreditsScreen(onBack: () -> Unit) {
    MindSetTheme {
        Scaffold(containerColor = Background) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Background),
                contentPadding = PaddingValues(
                    start = 20.dp, end = 20.dp,
                    top = padding.calculateTopPadding() + 12.dp,
                    bottom = padding.calculateBottomPadding() + 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "‹",
                            color = TextPrimary,
                            fontSize = 28.sp,
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onBack),
                        )
                        Spacer(Modifier.size(12.dp))
                        Text("Open data & credits", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
                item {
                    CreditCard(
                        title = "Exercise data & photos",
                        source = "free-exercise-db",
                        license = "Public domain (Unlicense)",
                        url = "github.com/yuhonas/free-exercise-db",
                    )
                }
                item {
                    CreditCard(
                        title = "Muscle diagrams",
                        source = "wger contributors",
                        license = "CC-BY-SA 4.0 / 3.0",
                        url = "wger.de",
                    )
                }
            }
        }
    }
}

@Composable
private fun CreditCard(title: String, source: String, license: String, url: String) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, color = TextSecondary, fontSize = 12.sp)
        Text(source, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Text(license, color = TextPrimary, fontSize = 14.sp)
        Text(url, color = Accent, fontSize = 13.sp)
    }
}
