package com.mindset.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mindset.MindSetIcons
import com.mindset.icons.Add
import com.mindset.icons.Grid
import com.mindset.icons.NavAccount
import com.mindset.icons.NavHome
import com.mindset.model.BottomNavTab

private fun BottomNavTab.icon(): ImageVector = when (this) {
    BottomNavTab.Home -> NavHome
    BottomNavTab.Log -> MindSetIcons.Add
    BottomNavTab.Stations -> MindSetIcons.Grid
    BottomNavTab.Profile -> NavAccount
}

@Composable
fun BottomNavBar(
    current: BottomNavTab,
    onTabClick: (BottomNavTab) -> Unit,
) {
    NavigationBar(
        modifier = Modifier,
        windowInsets = NavigationBarDefaults.windowInsets,
    ) {
        val colors = MaterialTheme.colorScheme
        BottomNavTab.entries.forEachIndexed { index, tab ->
            val isSelected = current == tab
            val contentColor = if (isSelected) colors.primaryContainer else colors.onSecondaryContainer

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabClick(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon(),
                        contentDescription = tab.label,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp),
                    )
                },
                alwaysShowLabel = true,
                label = {
                    Text(
                        text = tab.label,
                        color = contentColor,
                    )
                },
            )
        }
    }
}
