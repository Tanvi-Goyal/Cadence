package dev.cadence

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Stub tab so the bottom nav is honest; a real profile is out of scope for v1. */
@Composable
fun ProfileScreen(onTab: (String) -> Unit) {
    CadenceTheme {
        Scaffold(
            containerColor = Background,
            bottomBar = { CadenceBottomBar(current = Tab.Profile.route, onTab = onTab) },
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().background(Background).padding(padding).padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Profile", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Coming soon", color = TextSecondary, fontSize = 14.sp)
            }
        }
    }
}
