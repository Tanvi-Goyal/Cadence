package dev.cadence

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cadence.presentation.HistoryViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HistoryScreen(
    onOpenDetail: (String) -> Unit,
    onTab: (String) -> Unit,
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val rows by viewModel.rows.collectAsStateWithLifecycle()
    CadenceTheme {
        Scaffold(
            containerColor = Background,
            bottomBar = { CadenceBottomBar(current = Tab.History.route, onTab = onTab) },
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Background),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = padding.calculateTopPadding() + 16.dp,
                    bottom = padding.calculateBottomPadding() + 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text("History", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                if (rows.isEmpty()) {
                    item {
                        Text("No sessions logged yet.", color = TextSecondary, fontSize = 14.sp)
                    }
                } else {
                    items(rows, key = { it.session.id }) { row ->
                        SessionRow(
                            session = row.session,
                            volumeKg = row.volumeKg,
                            onClick = { onOpenDetail(row.session.id) },
                        )
                    }
                }
            }
        }
    }
}
